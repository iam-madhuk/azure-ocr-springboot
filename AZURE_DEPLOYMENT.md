# Azure App Service Deployment Guide

## Prerequisites

1. Azure Subscription
2. Azure CLI installed (`az --version`)
3. Azure Computer Vision Resource (with endpoint and key)
4. Logged into Azure (`az login`)

## Option 1: Deploy JAR Directly (Recommended)

### Step 1: Create Resource Group
```bash
az group create \
  --name myResourceGroup \
  --location eastus
```

### Step 2: Create App Service Plan
```bash
az appservice plan create \
  --name myAppServicePlan \
  --resource-group myResourceGroup \
  --sku B1 \
  --is-linux
```

### Step 3: Create Web App
```bash
az webapp create \
  --resource-group myResourceGroup \
  --plan myAppServicePlan \
  --name azure-ocr-app-<random> \
  --runtime "JAVA|17-java17"
```

### Step 4: Configure App Settings
```bash
az webapp config appsettings set \
  --resource-group myResourceGroup \
  --name azure-ocr-app-<random> \
  --settings \
    AZURE_VISION_ENDPOINT="https://<your-resource>.cognitiveservices.azure.com/" \
    AZURE_VISION_KEY="<your-api-key>"
```

### Step 5: Upload JAR File
```bash
az webapp deployment source config-zip \
  --resource-group myResourceGroup \
  --name azure-ocr-app-<random> \
  --src <path-to-jar>
```

Or use FTP:
```bash
# Get FTP credentials
az webapp deployment list-publishing-credentials \
  --resource-group myResourceGroup \
  --name azure-ocr-app-<random>
```

## Option 2: Deploy with Docker

### Step 1: Create Container Registry
```bash
az acr create \
  --resource-group myResourceGroup \
  --name mycontainerregistry \
  --sku Basic
```

### Step 2: Build and Push Image
```bash
az acr build \
  --registry mycontainerregistry \
  --image azure-ocr-app:latest .
```

### Step 3: Create App Service
```bash
az webapp create \
  --resource-group myResourceGroup \
  --plan myAppServicePlan \
  --name azure-ocr-app-docker \
  --deployment-container-image-name mycontainerregistry.azurecr.io/azure-ocr-app:latest \
  --docker-registry-server-url https://mycontainerregistry.azurecr.io
```

### Step 4: Configure Container Settings
```bash
az webapp config container set \
  --resource-group myResourceGroup \
  --name azure-ocr-app-docker \
  --docker-custom-image-name mycontainerregistry.azurecr.io/azure-ocr-app:latest \
  --docker-registry-server-url https://mycontainerregistry.azurecr.io \
  --docker-registry-server-user <username> \
  --docker-registry-server-password <password>
```

## Option 3: Deploy from GitHub Actions

See `GITHUB_SETUP.md` for GitHub Actions deployment.

## Verify Deployment

```bash
# Get app URL
az webapp show \
  --resource-group myResourceGroup \
  --name azure-ocr-app-<name> \
  --query defaultHostName

# Test health endpoint
curl https://<app-url>/api/ocr/health
```

## Troubleshooting

### Check logs
```bash
az webapp log tail \
  --resource-group myResourceGroup \
  --name azure-ocr-app-<name>
```

### Restart app
```bash
az webapp restart \
  --resource-group myResourceGroup \
  --name azure-ocr-app-<name>
```

### View app settings
```bash
az webapp config appsettings list \
  --resource-group myResourceGroup \
  --name azure-ocr-app-<name>
```
