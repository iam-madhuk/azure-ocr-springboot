# Azure Setup Guide

This guide walks you through setting up Azure Computer Vision resource and configuring the application.

## Prerequisites

- Azure subscription (free trial available at https://azure.microsoft.com/en-us/free/)
- Azure portal access

## Step 1: Create Azure Computer Vision Resource

### 1.1 Access Azure Portal

1. Go to [Azure Portal](https://portal.azure.com)
2. Sign in with your Microsoft account

### 1.2 Create a New Resource

1. Click "Create a resource" button (+ icon in top left)
2. Search for "Computer Vision" in the search box
3. Click on "Computer Vision" from the search results
4. Click the "Create" button

### 1.3 Configure Resource Details

Fill in the following information:

**Basics Tab:**
- **Subscription**: Select your subscription
- **Resource group**: 
  - Create new: Enter a name like "ocr-resources"
  - Or select an existing resource group
- **Region**: Select a region close to your location (e.g., East US, West Europe)
- **Name**: Provide a unique name (e.g., `my-ocr-service-001`)
- **Pricing tier**: 
  - Select "Standard S0" or higher for OCR capabilities
  - Free tier has limited requests

**Review + Create:**
- Review all settings
- Accept terms and conditions
- Click "Create"

### 1.4 Wait for Deployment

The resource deployment typically takes 2-5 minutes. You'll see:
```
Deployment in progress...
```

Once completed:
```
Your deployment is complete
```

## Step 2: Get API Credentials

### 2.1 Navigate to Your Resource

1. Click "Go to resource" button
2. Or search for "Computer Vision" in the search box and select your resource

### 2.2 Get Endpoint and API Key

1. In the left sidebar, click **"Keys and Endpoint"**
2. You'll see:
   - **Endpoint**: URL format like `https://myresource.cognitiveservices.azure.com/`
   - **Key 1**: Your primary API key
   - **Key 2**: Your secondary API key (for rotation)

### 2.3 Copy Your Credentials

Copy the following values somewhere safe:
```
Endpoint: https://myresource.cognitiveservices.azure.com/
API Key: a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
```

## Step 3: Configure the Application

### 3.1 Update application.properties

Open `src/main/resources/application.properties` and replace:

```properties
# BEFORE:
azure.vision.endpoint=https://<your-resource-name>.cognitiveservices.azure.com/
azure.vision.key=<your-api-key>

# AFTER (example):
azure.vision.endpoint=https://myocr-service.cognitiveservices.azure.com/
azure.vision.key=a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
```

**Important:** Make sure the endpoint URL ends with a trailing slash `/`

### 3.2 Verify Configuration

1. Ensure the file is saved
2. Do NOT commit this file to version control (it contains secrets)
3. For production, use environment variables instead

## Step 4: Test the Configuration

### 4.1 Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

### 4.2 Test Health Endpoint

```bash
curl http://localhost:8080/api/ocr/health
```

Expected response:
```
Azure OCR API is running
```

### 4.3 Test OCR with Sample Image

```bash
curl -X POST -F "file=@sample_image.jpg" http://localhost:8080/api/ocr/extract
```

Expected response (JSON):
```json
{
  "extractedText": "extracted text from image",
  "status": "SUCCESS",
  "message": "Text extraction completed successfully",
  "filename": "sample_image.jpg",
  "processedAt": "2025-11-27T10:30:00",
  "processingTimeMs": 1234
}
```

## Step 5: Understanding Azure Pricing

### Cost Breakdown

**Computer Vision API - Standard S0 Pricing:**

- **Read API (OCR)**:
  - First 1 free request per month
  - Then $1-2 per 1,000 API calls (varies by region)

**Estimated Monthly Costs:**
- 1,000 requests: ~$1-2
- 10,000 requests: ~$10-20
- 100,000 requests: ~$100-200

**Monitor Usage:**
1. Go to Azure Portal
2. Navigate to your Computer Vision resource
3. Click "Metrics" in left sidebar
4. View API call counts and costs

### Cost Optimization

1. **Use Batch Operations**: Process multiple images efficiently
2. **Cache Results**: Store extracted text locally when possible
3. **Monitor Quotas**: Set up alerts for usage thresholds
4. **Upgrade as Needed**: Start with Standard S0, upgrade if needed

## Troubleshooting Azure Configuration

### Issue: 401 Unauthorized

**Symptoms:**
- Error message: "Unauthorized" or "Invalid credentials"

**Causes:**
- Incorrect API key
- Wrong endpoint URL
- API key has been regenerated

**Solutions:**
1. Double-check the API key in Azure Portal
2. Verify the endpoint URL format (should end with `/`)
3. If key was rotated, update with the new key
4. Restart the application

### Issue: 403 Forbidden

**Symptoms:**
- Error message: "Access denied"

**Causes:**
- API key is incorrect
- Resource is restricted by IP/network policy
- Pricing tier doesn't support OCR

**Solutions:**
1. Verify pricing tier is "Standard S0" or higher
2. Check network/firewall settings
3. Verify API key is still active

### Issue: 429 Too Many Requests

**Symptoms:**
- Error message: "Rate limit exceeded"

**Causes:**
- Sending too many requests too quickly
- Exceeded monthly quota

**Solutions:**
1. Implement rate limiting in your application
2. Add delays between requests
3. Check Azure Portal for usage metrics
4. Upgrade pricing tier if needed

### Issue: 500 Internal Server Error

**Symptoms:**
- Error message: "Internal server error" from Azure

**Causes:**
- Azure service temporary issue
- Unsupported image format
- Image is corrupted

**Solutions:**
1. Retry the request
2. Check image format (JPEG, PNG, BMP, GIF, WEBP)
3. Verify image file is not corrupted
4. Check Azure status page: https://status.azure.com/

## Advanced Configuration

### Using Environment Variables (Recommended for Production)

**Windows PowerShell:**
```powershell
$env:AZURE_VISION_ENDPOINT="https://myresource.cognitiveservices.azure.com/"
$env:AZURE_VISION_KEY="your-api-key-here"
mvn spring-boot:run
```

**Linux/Mac Bash:**
```bash
export AZURE_VISION_ENDPOINT="https://myresource.cognitiveservices.azure.com/"
export AZURE_VISION_KEY="your-api-key-here"
mvn spring-boot:run
```

### Using Azure Key Vault (For Enterprise)

For production deployments, store secrets in Azure Key Vault:

1. Create Key Vault in Azure Portal
2. Add your credentials as secrets
3. Update application to use Key Vault credentials
4. Benefits:
   - Secure credential storage
   - Audit logging
   - Automatic rotation support
   - Access control

Reference: [Azure Key Vault Integration](https://learn.microsoft.com/en-us/azure/key-vault/general/overview)

## Monitoring and Management

### Set Up Alerts

1. Go to your Computer Vision resource
2. Click "Alerts" in the sidebar
3. Set thresholds for:
   - API call counts
   - Error rates
   - Cost limits

### View Metrics

1. Go to "Metrics" in the sidebar
2. Monitor:
   - Total API calls
   - Success/failure rates
   - Response times

### View Logs

1. Go to "Diagnostic settings"
2. Enable logging to:
   - Azure Monitor
   - Log Analytics Workspace
   - Storage Account

## Additional Resources

- [Azure Computer Vision Documentation](https://learn.microsoft.com/en-us/azure/cognitive-services/computer-vision/)
- [Azure Computer Vision Pricing](https://azure.microsoft.com/en-us/pricing/details/cognitive-services/computer-vision/)
- [Azure SDK for Java - Computer Vision](https://learn.microsoft.com/en-us/java/api/overview/azure/cognitiveservices/client/computervision)
- [Azure Portal](https://portal.azure.com)
- [Azure Subscription Management](https://account.azure.com/)

## FAQ

**Q: Can I use the free tier?**
A: The free tier has very limited requests (1 free request/month). For regular use, upgrade to Standard S0.

**Q: How do I change regions?**
A: You need to create a new resource in the desired region. You cannot change the region of an existing resource.

**Q: Can I delete and recreate my resource?**
A: Yes, but you'll lose your endpoint and API keys. Create a new resource with new credentials.

**Q: How do I scale to handle more requests?**
A: The Standard pricing tier automatically scales. If you hit limits, contact Azure support for quota increases.

**Q: Can I use multiple regions?**
A: Yes, create multiple Computer Vision resources in different regions and load balance between them.
