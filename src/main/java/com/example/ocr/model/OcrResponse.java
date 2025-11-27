package com.example.ocr.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Response model for OCR API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrResponse {
    private String extractedText;
    private String status;
    private String message;
    private String filename;
    private LocalDateTime processedAt;
    private long processingTimeMs;
}
