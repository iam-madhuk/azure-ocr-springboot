# API Testing Guide

This document provides examples for testing the Azure OCR API endpoints.

## Prerequisites

- Application running on `http://localhost:8080`
- Sample image files for testing
- curl or Postman installed

## Testing Endpoints

### 1. Health Check

Check if the API is running:

```bash
curl http://localhost:8080/api/ocr/health
```

**Expected Response:**
```
Azure OCR API is running
```

---

### 2. Extract Text from Image File

Upload an image and extract text:

```bash
curl -X POST -F "file=@/path/to/your/image.jpg" http://localhost:8080/api/ocr/extract
```

**With output formatting:**
```bash
curl -X POST -F "file=@/path/to/your/image.jpg" http://localhost:8080/api/ocr/extract | python -m json.tool
```

**Example Response (Success):**
```json
{
  "extractedText": "The quick brown fox jumps over the lazy dog",
  "status": "SUCCESS",
  "message": "Text extraction completed successfully",
  "filename": "test_image.jpg",
  "processedAt": "2025-11-27T10:30:45.123456",
  "processingTimeMs": 1234
}
```

---

### 3. Test with Different Image Formats

**PNG Format:**
```bash
curl -X POST -F "file=@document.png" http://localhost:8080/api/ocr/extract
```

**BMP Format:**
```bash
curl -X POST -F "file=@screenshot.bmp" http://localhost:8080/api/ocr/extract
```

**WEBP Format:**
```bash
curl -X POST -F "file=@image.webp" http://localhost:8080/api/ocr/extract
```

---

### 4. Error Testing

**No file provided:**
```bash
curl -X POST http://localhost:8080/api/ocr/extract
```

**Expected Error Response:**
```json
{
  "status": "ERROR",
  "message": "No file provided",
  "errorCode": "NO_FILE"
}
```

**Invalid file type:**
```bash
curl -X POST -F "file=@document.txt" http://localhost:8080/api/ocr/extract
```

**Expected Error Response:**
```json
{
  "status": "ERROR",
  "message": "Invalid file type. Supported types: JPEG, PNG, BMP, GIF, WEBP",
  "errorCode": "INVALID_INPUT"
}
```

---

## Testing with Postman

### Import Collection

1. Open Postman
2. Click "Import"
3. Create the following requests:

#### Request 1: Health Check
- **Method:** GET
- **URL:** `http://localhost:8080/api/ocr/health`
- **Headers:** None
- **Body:** None

#### Request 2: Extract Text (Form Data)
- **Method:** POST
- **URL:** `http://localhost:8080/api/ocr/extract`
- **Headers:** 
  - Content-Type: multipart/form-data (automatically set)
- **Body:** 
  - Type: form-data
  - Key: `file`
  - Value: Select file from your computer

### Steps to use:
1. Click on the `file` field and select "File" from dropdown
2. Click "Select Files" and choose your test image
3. Click "Send"
4. View the response in the Response panel

---

## Testing with Python

### Basic Test Script

```python
#!/usr/bin/env python3

import requests
import json
import sys

def test_health():
    """Test health check endpoint"""
    print("Testing health check endpoint...")
    try:
        response = requests.get('http://localhost:8080/api/ocr/health')
        print(f"Status Code: {response.status_code}")
        print(f"Response: {response.text}")
        print()
    except Exception as e:
        print(f"Error: {e}\n")

def test_ocr(image_path):
    """Test OCR extraction endpoint"""
    print(f"Testing OCR extraction with file: {image_path}")
    try:
        with open(image_path, 'rb') as f:
            files = {'file': f}
            response = requests.post(
                'http://localhost:8080/api/ocr/extract',
                files=files
            )
        
        print(f"Status Code: {response.status_code}")
        print(f"Response:")
        print(json.dumps(response.json(), indent=2))
        print()
        
        return response.json()
    except Exception as e:
        print(f"Error: {e}\n")

def test_invalid_file():
    """Test with invalid file type"""
    print("Testing with invalid file type...")
    try:
        with open('test.txt', 'rb') as f:
            files = {'file': f}
            response = requests.post(
                'http://localhost:8080/api/ocr/extract',
                files=files
            )
        
        print(f"Status Code: {response.status_code}")
        print(f"Response:")
        print(json.dumps(response.json(), indent=2))
        print()
    except Exception as e:
        print(f"Error: {e}\n")

if __name__ == "__main__":
    print("=" * 60)
    print("Azure OCR API Testing")
    print("=" * 60)
    print()
    
    # Test 1: Health check
    test_health()
    
    # Test 2: OCR extraction (requires valid image)
    if len(sys.argv) > 1:
        test_ocr(sys.argv[1])
    else:
        print("Usage: python test_api.py <path_to_image>")
        print("Example: python test_api.py sample_image.jpg\n")
    
    print("=" * 60)
```

**Usage:**
```bash
python test_api.py /path/to/your/image.jpg
```

---

## Testing with JavaScript/Node.js

### Simple Test Script

```javascript
const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');

async function testHealth() {
  console.log('Testing health check endpoint...');
  try {
    const response = await axios.get('http://localhost:8080/api/ocr/health');
    console.log(`Status: ${response.status}`);
    console.log(`Response: ${response.data}\n`);
  } catch (error) {
    console.error(`Error: ${error.message}\n`);
  }
}

async function testOCR(imagePath) {
  console.log(`Testing OCR extraction with file: ${imagePath}`);
  try {
    const form = new FormData();
    form.append('file', fs.createReadStream(imagePath));
    
    const response = await axios.post(
      'http://localhost:8080/api/ocr/extract',
      form,
      { headers: form.getHeaders() }
    );
    
    console.log(`Status: ${response.status}`);
    console.log('Response:');
    console.log(JSON.stringify(response.data, null, 2));
    console.log();
  } catch (error) {
    console.error(`Error: ${error.message}\n`);
  }
}

(async () => {
  console.log('='.repeat(60));
  console.log('Azure OCR API Testing');
  console.log('='.repeat(60));
  console.log();
  
  await testHealth();
  
  const imagePath = process.argv[2];
  if (imagePath) {
    await testOCR(imagePath);
  } else {
    console.log('Usage: node test_api.js <path_to_image>');
    console.log('Example: node test_api.js sample_image.jpg\n');
  }
  
  console.log('='.repeat(60));
})();
```

**Installation and Usage:**
```bash
npm install axios form-data
node test_api.js /path/to/your/image.jpg
```

---

## Performance Testing

### Load Testing with Apache Bench

```bash
# Single request timing
ab -n 1 -c 1 -p test_payload.bin http://localhost:8080/api/ocr/extract

# Multiple requests (100 total, 10 concurrent)
ab -n 100 -c 10 http://localhost:8080/api/ocr/health
```

---

## Troubleshooting Tests

### Issue: Connection Refused

**Cause:** Application not running

**Solution:**
```bash
# Ensure application is running
mvn spring-boot:run
```

### Issue: 400 Bad Request

**Cause:** File format not supported

**Solution:**
- Use supported formats: JPEG, PNG, BMP, GIF, WEBP
- Verify the file is not corrupted
- Check file size (max 50MB)

### Issue: 401 Unauthorized

**Cause:** Azure credentials incorrect

**Solution:**
- Verify `application.properties` has correct Azure endpoint and key
- Restart the application
- Check Azure resource status

---

## Sample Images for Testing

You can create simple test images using free online tools:
- [Remove.bg](https://www.remove.bg) - Generate test images
- [Lorem Picsum](https://picsum.photos) - Random placeholder images
- Create your own with screenshots or photographs

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: API Integration Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up Java
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Build application
        run: mvn clean package
      - name: Run application
        run: mvn spring-boot:run &
      - name: Wait for startup
        run: sleep 10
      - name: Run API tests
        run: python test_api.py test_image.jpg
```

---

## Notes

- Ensure the application is running before testing
- Use realistic test images for accurate OCR results
- Monitor processing times and Azure API quotas
- Store test images in version control or generate them dynamically
