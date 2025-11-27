package com.example.ocr.config;

import com.azure.ai.vision.imageanalysis.ImageAnalysisClient;
import com.azure.ai.vision.imageanalysis.ImageAnalysisClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Optional;

/**
 * Configuration class for Azure Computer Vision API
 * 
 * Requires the following environment variables or application.properties:
 * - AZURE_VISION_ENDPOINT: The endpoint for your Azure Computer Vision resource
 * - AZURE_VISION_KEY: The API key for your Azure Computer Vision resource
 */
@Configuration
@Slf4j
public class AzureVisionConfig {

    @Value("${azure.vision.endpoint}")
    private String endpoint;

    @Value("${azure.vision.key}")
    private String apiKey;

    /**
     * Creates and configures the Azure Computer Vision client
     * Returns an empty Optional if credentials are not configured (for demo mode)
     * 
     * @return Optional containing ImageAnalysisClient, or empty for demo mode
     */
    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = {"azure.vision.endpoint", "azure.vision.key"})
    public ImageAnalysisClient imageAnalysisClient() {
        if (endpoint == null || endpoint.isBlank() || apiKey == null || apiKey.isBlank()) {
            log.warn("======================================");
            log.warn("Azure Vision credentials not configured. Running in DEMO MODE.");
            log.warn("To enable real OCR processing:");
            log.warn("1. Create Azure Computer Vision resource");
            log.warn("2. Configure in application.properties:");
            log.warn("   azure.vision.endpoint=https://your-resource.cognitiveservices.azure.com/");
            log.warn("   azure.vision.key=your-api-key");
            log.warn("3. Restart the application");
            log.warn("======================================");
            return null; // bean won't be created due to ConditionalOnProperty, but guard anyway
        }

        log.info("Initializing Azure Computer Vision client with endpoint: {}", endpoint);

        try {
            ImageAnalysisClient client = new ImageAnalysisClientBuilder()
                    .endpoint(endpoint)
                    .credential(new AzureKeyCredential(apiKey))
                    .buildClient();
            return client;
        } catch (Exception e) {
            log.error("Failed to initialize Azure Computer Vision client", e);
            log.warn("Falling back to demo mode");
            return null;
        }
    }
}
