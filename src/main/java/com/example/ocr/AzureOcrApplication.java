package com.example.ocr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application for Azure OCR processing
 * 
 * This application provides REST API endpoints to upload images and extract text
 * using Azure Computer Vision's OCR capabilities.
 * 
 * Prerequisites:
 * 1. Azure Computer Vision resource created in Azure Portal
 * 2. Azure Vision endpoint and API key configured in application.properties
 * 
 * Run the application:
 * mvn spring-boot:run
 * 
 * Access the API:
 * - Health check: GET http://localhost:8080/api/ocr/health
 * - Extract text: POST http://localhost:8080/api/ocr/extract (multipart file upload)
 */
@SpringBootApplication
public class AzureOcrApplication {

    public static void main(String[] args) {
        SpringApplication.run(AzureOcrApplication.class, args);
    }

}
