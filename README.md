# Azure OCR Spring Boot Application

A Spring Boot REST API application that performs Optical Character Recognition (OCR) on images using Azure Computer Vision service.

## Features

- **Image OCR Processing**: Extract text from images (JPEG, PNG, BMP, GIF, WEBP)
- **Azure Integration**: Uses Azure Computer Vision API for accurate OCR
- **RESTful API**: Simple and intuitive REST endpoints
- **Error Handling**: Comprehensive error handling and validation
- **Logging**: Detailed logging for debugging and monitoring
- **CORS Support**: Cross-Origin Resource Sharing enabled

## Prerequisites

Before running this application, you need:

1. **Java 17+** installed on your system
2. **Maven 3.6+** for building the project
3. **Azure Subscription** with Computer Vision resource
4. **Azure Computer Vision Resource** created in Azure Portal

### Setting up Azure Computer Vision Resource

1. Go to [Azure Portal](https://portal.azure.com)
2. Click "Create a resource"
3. Search for "Computer Vision"
4. Click "Create"
5. Fill in the required details:
   - **Subscription**: Select your subscription
   - **Resource group**: Create new or select existing
   - **Region**: Choose a region (e.g., East US)
   - **Name**: Provide a unique name
   - **Pricing tier**: Select "Standard S0" or higher
6. Click "Review + create" then "Create"
7. Once created, go to the resource and note:
   - **Endpoint**: Found in "Keys and Endpoint" section (URL format)
   - **API Key**: Found in "Keys and Endpoint" section

## Installation & Setup

### 1. Clone or Download the Project
```bash
cd /workspace/azure-ocr-springboot
```

### 2. Configure Azure Credentials

Edit `src/main/resources/application.properties` and replace:

```properties
azure.vision.endpoint=https://<your-resource-name>.cognitiveservices.azure.com/
azure.vision.key=<your-api-key>
```

**Example:**
```properties
azure.vision.endpoint=https://myazureresource.cognitiveservices.azure.com/
azure.vision.key=a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
```

### 3. Build the Project

```bash
mvn clean install
```

This command:
- Cleans previous builds
- Downloads all dependencies
- Compiles the source code
- Runs tests
- Packages the application

### 4. Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR directly:

```bash
mvn clean package
java -jar target/azure-ocr-springboot-1.0.0.jar
```

The application will start on `http://localhost:8080`

## API Documentation

### 1. Health Check Endpoint

**Endpoint:** `GET /api/ocr/health`

**Description:** Check if the API is running

**Response:**
```
Azure OCR API is running
```

**Example using curl:**
```bash
curl http://localhost:8080/api/ocr/health
```

### 2. Extract Text from Image

**Endpoint:** `POST /api/ocr/extract`

**Content-Type:** `multipart/form-data`

**Parameters:**
- `file` (required): Image file to process
  - Supported formats: JPEG, PNG, BMP, GIF, WEBP
  - Max size: 50MB

**Response (Success - 200 OK):**
```json
{
  "extractedText": "Hello World\nThis is text extracted from the image",
  "status": "SUCCESS",
  "message": "Text extraction completed successfully",
  "filename": "document.jpg",
  "processedAt": "2025-11-27T10:30:45",
  "processingTimeMs": 1245
}
```

**Response (Error - 400 Bad Request):**
```json
{
  "status": "ERROR",
  "message": "Invalid file type. Supported types: JPEG, PNG, BMP, GIF, WEBP",
  "errorCode": "INVALID_INPUT"
}
```

**Example using curl:**
```bash
curl -X POST -F "file=@path/to/image.jpg" http://localhost:8080/api/ocr/extract
```

**Example using Python:**
```python
import requests

file_path = 'path/to/image.jpg'
with open(file_path, 'rb') as f:
    files = {'file': f}
    response = requests.post(
        'http://localhost:8080/api/ocr/extract',
        files=files
    )
    print(response.json())
```

**Example using JavaScript/Node.js:**
```javascript
const FormData = require('form-data');
const fs = require('fs');
const axios = require('axios');

async function extractText() {
  const form = new FormData();
  form.append('file', fs.createReadStream('path/to/image.jpg'));
  
  try {
    const response = await axios.post(
      'http://localhost:8080/api/ocr/extract',
      form,
      { headers: form.getHeaders() }
    );
    console.log(response.data);
  } catch (error) {
    console.error(error);
  }
}

extractText();
```

### 3. Extract Text from URL (Not Implemented)

**Endpoint:** `POST /api/ocr/extract-from-url`

**Parameters:**
- `url` (required): URL of the image to process

**Note:** This endpoint returns "Not Implemented" currently. File upload via the `/extract` endpoint is recommended.

## Project Structure

```
azure-ocr-springboot/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/ocr/
│   │   │       ├── AzureOcrApplication.java       # Main Spring Boot application
│   │   │       ├── config/
│   │   │       │   └── AzureVisionConfig.java     # Azure Vision client configuration
│   │   │       ├── controller/
│   │   │       │   └── OcrController.java         # REST API endpoints
│   │   │       ├── service/
│   │   │       │   └── OcrService.java            # OCR business logic
│   │   │       └── model/
│   │   │           ├── OcrResponse.java           # OCR response model
│   │   │           └── ErrorResponse.java         # Error response model
│   │   └── resources/
│   │       └── application.properties             # Configuration file
│   └── test/                                      # Unit tests
├── pom.xml                                        # Maven configuration
└── README.md                                      # This file
```

## Configuration Details

### application.properties

```properties
# Server Port
server.port=8080

# Azure Credentials (MUST be configured)
azure.vision.endpoint=https://<your-resource>.cognitiveservices.azure.com/
azure.vision.key=<your-api-key>

# File Upload Limits
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# Logging
logging.level.root=INFO
logging.level.com.example.ocr=DEBUG
```

## Troubleshooting

### Issue: 401 Unauthorized from Azure

**Cause:** Incorrect API key or endpoint

**Solution:**
1. Verify the API key and endpoint in `application.properties`
2. Check if the key is still active in Azure Portal
3. Ensure the endpoint URL has a trailing slash

### Issue: 400 Bad Request

**Cause:** Invalid file format or no file provided

**Solution:**
1. Ensure the file is one of: JPEG, PNG, BMP, GIF, WEBP
2. Verify the file is not corrupted
3. Check the file size (max 50MB)

### Issue: Connection Timeout

**Cause:** Network issues or Azure service unavailable

**Solution:**
1. Check internet connection
2. Verify Azure resource status in Azure Portal
3. Check firewall/proxy settings

### Issue: Application Won't Start

**Cause:** Missing Azure credentials

**Solution:**
1. Ensure `application.properties` has valid Azure credentials
2. Check Java 17+ is installed: `java -version`
3. Run `mvn clean install` to rebuild

## Performance Considerations

- **Processing Time**: Typically 1-3 seconds per image, depending on image complexity
- **Image Size**: Larger images may take longer to process
- **Azure Limits**: Subject to Azure Computer Vision API quotas (see pricing tier)

## Security Notes

- Never commit API keys to version control
- Store credentials in environment variables for production
- Use HTTPS in production environments
- Implement authentication/authorization as needed

## Supported Image Formats

- **JPEG** (.jpg, .jpeg) - Most common, good compression
- **PNG** (.png) - Lossless compression, supports transparency
- **BMP** (.bmp) - Uncompressed, larger file size
- **GIF** (.gif) - Supports animation (first frame is processed)
- **WEBP** (.webp) - Modern format, good compression

## Dependencies

- **Spring Boot 3.1.5**: Web framework
- **Azure AI Vision 1.0.0**: OCR processing
- **Azure Identity 1.9.2**: Azure authentication
- **Lombok**: Reduces boilerplate code
- **Jackson**: JSON processing

## Testing

Run unit tests:

```bash
mvn test
```

## Production Deployment

For production deployment:

1. **Build Docker Image** (optional)
2. **Configure Environment Variables**
3. **Set up Azure Key Vault** for credential management
4. **Enable HTTPS** and authentication
5. **Configure Logging** and monitoring
6. **Scale** based on load requirements

Example Docker setup (if needed):

```dockerfile
FROM openjdk:17-slim
COPY target/azure-ocr-springboot-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## License

This project is provided as-is for demonstration purposes.

## Support

For issues or questions:
1. Check the Troubleshooting section
2. Review Azure Computer Vision documentation
3. Check Spring Boot documentation

## Additional Resources

- [Azure Computer Vision Documentation](https://learn.microsoft.com/en-us/azure/cognitive-services/computer-vision/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Azure SDK for Java](https://learn.microsoft.com/en-us/java/azure/sdk/overview)
- [RESTful API Best Practices](https://restfulapi.net/)
  
