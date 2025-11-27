package com.example.ocr.service;

import com.azure.ai.vision.imageanalysis.ImageAnalysisClient;
import com.example.ocr.model.OcrResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service for handling OCR operations using Azure Computer Vision API
 */
@Service
@Slf4j
public class OcrService {

    private final ImageAnalysisClient imageAnalysisClient;
    
    @Value("${azure.vision.endpoint:}")
    private String endpoint;
    
    @Value("${azure.vision.key:}")
    private String apiKey;

    // Configurable properties with sensible defaults
    @Value("${azure.vision.api-version:v3.2}")
    private String apiVersion;

    @Value("${azure.vision.connect-timeout-secs:10}")
    private int connectTimeoutSecs;

    @Value("${azure.vision.request-timeout-secs:30}")
    private int requestTimeoutSecs;

    @Value("${azure.vision.max-retries:3}")
    private int maxRetries;

    @Value("${azure.vision.retry-backoff-millis:500}")
    private int retryBackoffMillis;

    @Value("${azure.vision.poll-interval-millis:1000}")
    private int pollIntervalMillis;

    @Value("${azure.vision.poll-timeout-secs:30}")
    private int pollTimeoutSecs;

    private HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OcrService(ImageAnalysisClient imageAnalysisClient) {
        this.imageAnalysisClient = imageAnalysisClient; // may be null when not configured
    }

    // Lazily initialize HttpClient so @Value fields are injected first
    private HttpClient getHttpClient() {
        if (this.httpClient == null) {
            this.httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(Math.max(1, connectTimeoutSecs)))
                    .build();
        }
        return this.httpClient;
    }

    /**
     * Performs OCR on the provided image file using Azure Computer Vision
     * 
     * @param file The image file to process (supports JPEG, PNG, BMP, GIF, WEBP)
     * @return OcrResponse containing extracted text and metadata
     * @throws IOException If the file cannot be read
     * @throws IllegalArgumentException If the file is not a valid image
     */
    public OcrResponse performOcr(MultipartFile file) throws IOException {
        long startTime = System.currentTimeMillis();
        
        log.info("Starting OCR process for file: {}", file.getOriginalFilename());

        // Diagnostic: log whether Azure client/credentials are available (mask key)
        try {
            boolean clientPresent = imageAnalysisClient != null;
            boolean endpointPresent = endpoint != null && !endpoint.isBlank();
            boolean apiKeyPresent = apiKey != null && !apiKey.isBlank();
            String maskedKey = apiKeyPresent ? ("***" + apiKey.substring(Math.max(0, apiKey.length()-4))) : "(none)";
            // Use INFO so this is always visible in the logs for troubleshooting
            log.info("Azure diagnostic - clientPresent={}, endpointPresent={}, apiKeyPresent={}, endpoint={}, apiKeyMasked={}",
                    clientPresent, endpointPresent, apiKeyPresent, endpoint, maskedKey);
        } catch (Exception ex) {
            log.warn("Failed to evaluate Azure diagnostic fields", ex);
        }
        
        // Validate file
        if (file.isEmpty()) {
            log.error("Uploaded file is empty");
            throw new IllegalArgumentException("File is empty. Please upload a valid image.");
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (!isValidImageType(contentType)) {
            log.error("Invalid file type: {}", contentType);
            throw new IllegalArgumentException("Invalid file type. Supported types: JPEG, PNG, BMP, GIF, WEBP");
        }

        try {
            // Check if Azure credentials are configured and client is available
            if (imageAnalysisClient == null || endpoint == null || endpoint.isBlank() || apiKey == null || apiKey.isBlank()) {
                log.warn("Azure credentials or client not available. Running in demo mode.");
                return getDemoOcrResponse(file);
            }
            
            // Read file bytes
            byte[] imageBytes = file.getBytes();
            log.debug("File read successfully. Size: {} bytes", imageBytes.length);
            
            // Call Azure Computer Vision API
            log.info("Sending image to Azure Computer Vision for OCR");
            String extractedText = callAzureOcr(imageBytes);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            log.info("OCR completed successfully in {} ms", processingTime);
            
            return OcrResponse.builder()
                    .extractedText(extractedText)
                    .status("SUCCESS")
                    .message("Text extraction completed successfully")
                    .filename(file.getOriginalFilename())
                    .processedAt(LocalDateTime.now())
                    .processingTimeMs(processingTime)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error during OCR processing", e);
            long processingTime = System.currentTimeMillis() - startTime;
            
            return OcrResponse.builder()
                    .extractedText("")
                    .status("ERROR")
                    .message("Error processing image: " + e.getMessage())
                    .filename(file.getOriginalFilename())
                    .processedAt(LocalDateTime.now())
                    .processingTimeMs(processingTime)
                    .build();
        }
    }

    /**
     * Calls Azure Computer Vision OCR API
     * 
     * @param imageBytes The image file bytes
     * @return Extracted text from the image
     */
    private String callAzureOcr(byte[] imageBytes) {
        // Use direct REST call to Azure Computer Vision OCR endpoint.
        // We prefer a simple, reliable HTTP call rather than binding to a specific
        // SDK surface here. The application wires credentials in `AzureVisionConfig`.
        if (endpoint == null || endpoint.isBlank() || apiKey == null || apiKey.isBlank()) {
            log.warn("Azure credentials not configured. Cannot call Azure OCR.");
            throw new IllegalStateException("Azure credentials not configured");
        }

        String base = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        String ocrUrl = base + "/vision/" + apiVersion + "/ocr?language=unk&detectOrientation=true";

        int attempt = 0;
        while (true) {
            attempt++;
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(ocrUrl))
                        .timeout(Duration.ofSeconds(Math.max(1, requestTimeoutSecs)))
                        .header("Ocp-Apim-Subscription-Key", apiKey)
                        .header("Content-Type", "application/octet-stream")
                        .POST(HttpRequest.BodyPublishers.ofByteArray(imageBytes))
                        .build();

                HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();
                String body = response.body();

                if (status == 200) {
                    JsonNode root = objectMapper.readTree(body);
                    String result = extractTextFromJson(root);
                    log.debug("Extracted text length: {}", result.length());
                    return result;
                }

                if (status == 202) {
                    // Async analyze/read API - Operation-Location contains the URL to poll
                    String operationUrl = response.headers().firstValue("Operation-Location").orElse(null);
                    if (operationUrl == null) {
                        log.error("202 received but Operation-Location header missing. Body: {}", body);
                        throw new RuntimeException("Operation-Location header missing on 202 response");
                    }
                    return pollForReadResult(operationUrl);
                }

                if (status == 429 || (status >= 500 && status < 600)) {
                    // transient error - retry with backoff
                    if (attempt >= Math.max(1, maxRetries)) {
                        log.error("Azure OCR retries exhausted (status {}). Body: {}", status, body);
                        throw new RuntimeException("Azure OCR returned status " + status);
                    }
                    int backoff = computeBackoffMillis(attempt);
                    log.warn("Transient error from Azure OCR (status {}). Retrying in {} ms (attempt {}/{})", status, backoff, attempt, maxRetries);
                    Thread.sleep(backoff);
                    continue;
                }

                // Other non-success codes -> fail fast
                log.error("Azure OCR returned non-OK status: {} - body: {}", status, body);
                throw new RuntimeException("Azure OCR returned status " + status + ": " + body);

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while calling Azure OCR", ie);
            } catch (Exception e) {
                // If we've exhausted retries, rethrow
                if (attempt >= Math.max(1, maxRetries)) {
                    log.error("Failed to call Azure OCR endpoint after {} attempts", attempt, e);
                    throw new RuntimeException("Azure OCR failed: " + e.getMessage(), e);
                }
                int backoff = computeBackoffMillis(attempt);
                log.warn("Error calling Azure OCR (attempt {}/{}). Will retry in {} ms. Error: {}", attempt, maxRetries, backoff, e.getMessage());
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry backoff", ie);
                }
            }
        }
    }

    private int computeBackoffMillis(int attempt) {
        // exponential backoff with jitter
        int base = Math.max(1, retryBackoffMillis);
        int exponential = base * (1 << Math.max(0, attempt - 1));
        int jitter = ThreadLocalRandom.current().nextInt(Math.max(1, base));
        return Math.min(exponential + jitter, 30_000); // cap at 30s
    }

    // Poll the operation URL returned by the Read API (Operation-Location)
    private String pollForReadResult(String operationUrl) throws Exception {
        long deadline = System.currentTimeMillis() + (Math.max(1, pollTimeoutSecs) * 1000L);
        while (System.currentTimeMillis() < deadline) {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(operationUrl))
                    .timeout(Duration.ofSeconds(Math.max(1, requestTimeoutSecs)))
                    .header("Ocp-Apim-Subscription-Key", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> resp = getHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
            int status = resp.statusCode();
            String body = resp.body();

            if (status >= 200 && status < 300) {
                JsonNode root = objectMapper.readTree(body);
                // Check for "status" (Read API returns a status field)
                JsonNode statusNode = root.get("status");
                if (statusNode != null) {
                    String s = statusNode.asText().toLowerCase();
                    if ("succeeded".equals(s)) {
                        // extract from analyzeResult.readResults or readResults
                        JsonNode analyze = root.get("analyzeResult");
                        if (analyze != null) {
                            JsonNode readResults = analyze.get("readResults");
                            if (readResults != null) {
                                return extractTextFromJson(objectMapper.createObjectNode().set("analyzeResult", analyze));
                            }
                        }
                        // fallback to top-level readResults
                        JsonNode readResults = root.get("readResults");
                        if (readResults != null) {
                            return extractTextFromJson(objectMapper.createObjectNode().set("readResults", readResults));
                        }
                        return "";
                    }
                    if ("failed".equals(s)) {
                        throw new RuntimeException("Read operation failed: " + body);
                    }
                    // else still running
                }
            } else if (status == 429 || (status >= 500 && status < 600)) {
                // transient - we'll wait then retry polling
                log.warn("Polling operation returned transient status {}. Body: {}", status, body);
            } else {
                log.error("Polling operation returned non-success status {}. Body: {}", status, body);
                throw new RuntimeException("Polling operation failed with status " + status + ": " + body);
            }

            Thread.sleep(Math.max(200, pollIntervalMillis));
        }

        throw new RuntimeException("Timed out waiting for Read operation result");
    }

    // Extract text from known JSON shapes returned by OCR/Read APIs
    private String extractTextFromJson(JsonNode root) {
        StringBuilder extracted = new StringBuilder();

        // Case 1: classic OCR -> regions -> lines -> words
        JsonNode regions = root.get("regions");
        if (regions != null && regions.isArray() && regions.size() > 0) {
            for (JsonNode region : regions) {
                JsonNode lines = region.get("lines");
                if (lines == null || !lines.isArray()) continue;
                for (JsonNode line : lines) {
                    JsonNode words = line.get("words");
                    if (words == null || !words.isArray()) continue;
                    StringJoiner sj = new StringJoiner(" ");
                    for (JsonNode word : words) {
                        JsonNode textNode = word.get("text");
                        if (textNode != null) sj.add(textNode.asText());
                    }
                    extracted.append(sj.toString()).append('\n');
                }
            }
        }

        // Case 2: analyzeResult.readResults -> pages -> lines
        JsonNode readResults = root.at("/analyzeResult/readResults");
        if ((readResults == null || readResults.isMissingNode()) ) {
            readResults = root.get("readResults");
        }
        if (readResults != null && readResults.isArray()) {
            for (JsonNode page : readResults) {
                JsonNode lines = page.get("lines");
                if (lines == null || !lines.isArray()) continue;
                for (JsonNode line : lines) {
                    JsonNode textNode = line.get("text");
                    if (textNode != null) extracted.append(textNode.asText()).append('\n');
                }
            }
        }

        return extracted.toString().trim();
    }

    /**
     * Returns a demo OCR response for testing without Azure credentials
     * 
     * @param file The uploaded file
     * @return Demo OCR response
     */
    private OcrResponse getDemoOcrResponse(MultipartFile file) {
        // Simulate OCR processing delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String demoText = "This is a demo OCR response.\n" +
                        "File: " + file.getOriginalFilename() + "\n" +
                        "Size: " + file.getSize() + " bytes\n" +
                        "Type: " + file.getContentType() + "\n\n" +
                        "To enable real OCR processing:\n" +
                        "1. Create an Azure Computer Vision resource\n" +
                        "2. Add credentials to application.properties:\n" +
                        "   - azure.vision.endpoint=https://your-resource.cognitiveservices.azure.com/\n" +
                        "   - azure.vision.key=your-api-key\n" +
                        "3. Restart the application";
        
        return OcrResponse.builder()
                .extractedText(demoText)
                .status("SUCCESS (DEMO MODE)")
                .message("Text extraction completed in demo mode (Azure credentials not configured)")
                .filename(file.getOriginalFilename())
                .processedAt(LocalDateTime.now())
                .processingTimeMs(500)
                .build();
    }

    /**
     * Validates if the file content type is a supported image format
     * 
     * @param contentType The MIME type of the file
     * @return true if the content type is a supported image format
     */
    private boolean isValidImageType(String contentType) {
        if (contentType == null) {
            return false;
        }
        
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/bmp") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp");
    }
}
