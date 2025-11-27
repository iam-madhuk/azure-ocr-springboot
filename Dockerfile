# Use official OpenJDK runtime as base image
FROM openjdk:17-slim

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY target/azure-ocr-springboot-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables (override at runtime)
ENV AZURE_VISION_ENDPOINT=""
ENV AZURE_VISION_KEY=""

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/api/ocr/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
