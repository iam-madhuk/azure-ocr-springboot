# Application Configuration Examples
# This file shows how to configure the application for different environments

# ============================================
# DEVELOPMENT ENVIRONMENT
# ============================================
# File: application-dev.properties
# Usage: mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# server.port=8080
# azure.vision.endpoint=https://dev-resource.cognitiveservices.azure.com/
# azure.vision.key=YOUR_DEV_API_KEY
# logging.level.com.example.ocr=DEBUG
# logging.level.com.azure=DEBUG


# ============================================
# PRODUCTION ENVIRONMENT
# ============================================
# File: application-prod.properties
# Usage: java -jar app.jar --spring.profiles.active=prod

# server.port=8080
# azure.vision.endpoint=https://prod-resource.cognitiveservices.azure.com/
# azure.vision.key=YOUR_PROD_API_KEY
# logging.level.root=INFO
# logging.level.com.example.ocr=INFO
# logging.level.com.azure=WARN


# ============================================
# USING ENVIRONMENT VARIABLES
# ============================================
# Instead of hardcoding values, use environment variables:
#
# Linux/Mac:
# export AZURE_VISION_ENDPOINT=https://your-resource.cognitiveservices.azure.com/
# export AZURE_VISION_KEY=your-api-key
# mvn spring-boot:run
#
# Windows PowerShell:
# $env:AZURE_VISION_ENDPOINT="https://your-resource.cognitiveservices.azure.com/"
# $env:AZURE_VISION_KEY="your-api-key"
# mvn spring-boot:run


# ============================================
# FILE UPLOAD SIZE LIMITS
# ============================================
# Adjust based on your needs
# spring.servlet.multipart.max-file-size=50MB
# spring.servlet.multipart.max-request-size=50MB


# ============================================
# LOGGING PATTERNS
# ============================================
# Custom logging format
# logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
# logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
