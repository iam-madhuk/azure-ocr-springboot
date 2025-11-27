package com.example.ocr.controller;

import com.example.ocr.model.ErrorResponse;
import com.example.ocr.model.OcrResponse;
import com.example.ocr.service.OcrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * REST Controller for OCR operations
 * 
 * Endpoints:
 * POST /api/ocr/extract - Upload an image file and extract text
 * GET /api/ocr/health - Health check endpoint
 */
@RestController
@RequestMapping("/api/ocr")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class OcrController {

    private final OcrService ocrService;

    public OcrController(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    /**
     * Health check endpoint
     * 
     * @return Simple message indicating the API is running
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.info("Health check request received");
        return ResponseEntity.ok("Azure OCR API is running");
    }

    /**
     * Extract text from an uploaded image using OCR
     * 
     * Supported image formats: JPEG, PNG, BMP, GIF, WEBP
     * 
     * Example request using curl:
     * curl -X POST -F "file=@image.jpg" http://localhost:8080/api/ocr/extract
     * 
     * @param file The image file to process (multipart form data)
     * @return OcrResponse containing extracted text and metadata
     * 
     * Response examples:
     * 
     * Success (200):
     * {
     *   "extractedText": "Hello World\nThis is text from the image",
     *   "status": "SUCCESS",
     *   "message": "Text extraction completed successfully",
     *   "filename": "image.jpg",
     *   "processedAt": "2025-11-27T10:30:00",
     *   "processingTimeMs": 1234
     * }
     * 
     * Error (400):
     * {
     *   "status": "ERROR",
     *   "message": "File is empty. Please upload a valid image.",
     *   "errorCode": "INVALID_FILE"
     * }
     */
    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> extractText(@RequestParam("file") MultipartFile file) {
        log.info("OCR request received for file: {}", file.getOriginalFilename());
        
        try {
            // Validate file is not null
            if (file == null) {
                log.error("No file provided in request");
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.builder()
                                .status("ERROR")
                                .message("No file provided")
                                .errorCode("NO_FILE")
                                .build());
            }
            
            // Process the image
            OcrResponse response = ocrService.performOcr(file);
            
            // Return success response
            if ("SUCCESS".equals(response.getStatus())) {
                log.info("OCR processing completed successfully for file: {}", file.getOriginalFilename());
                return ResponseEntity.ok(response);
            } else {
                log.error("OCR processing failed: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.builder()
                            .status("ERROR")
                            .message(e.getMessage())
                            .errorCode("INVALID_INPUT")
                            .build());
        } catch (IOException e) {
            log.error("Error reading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .status("ERROR")
                            .message("Error reading file: " + e.getMessage())
                            .errorCode("FILE_READ_ERROR")
                            .build());
        } catch (Exception e) {
            log.error("Unexpected error during OCR processing", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .status("ERROR")
                            .message("An unexpected error occurred: " + e.getMessage())
                            .errorCode("INTERNAL_ERROR")
                            .build());
        }
    }

    /**
     * Alternative endpoint for extracting text from image URL
     * 
     * @param imageUrl The URL of the image to process
     * @return OcrResponse containing extracted text
     */
    @PostMapping("/extract-from-url")
    public ResponseEntity<?> extractTextFromUrl(@RequestParam("url") String imageUrl) {
        log.info("OCR request received for URL: {}", imageUrl);
        
        try {
            if (imageUrl == null || imageUrl.isBlank()) {
                log.error("No URL provided");
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.builder()
                                .status("ERROR")
                                .message("Image URL is required")
                                .errorCode("MISSING_URL")
                                .build());
            }
            
            log.info("Processing image from URL: {}", imageUrl);
            
            // For URL-based processing, we would need to modify the service
            // This is a placeholder for future enhancement
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(ErrorResponse.builder()
                            .status("ERROR")
                            .message("URL-based OCR is not yet implemented. Please use the /extract endpoint with file upload.")
                            .errorCode("NOT_IMPLEMENTED")
                            .build());
                    
        } catch (Exception e) {
            log.error("Error processing URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.builder()
                            .status("ERROR")
                            .message("Error processing image: " + e.getMessage())
                            .errorCode("PROCESSING_ERROR")
                            .build());
        }
    }
}
