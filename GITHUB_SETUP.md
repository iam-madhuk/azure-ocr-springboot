# GitHub Setup and Deployment Guide

This guide explains how to set up GitHub Actions for CI/CD and automated deployment to Azure.

## Overview

The project includes automated workflows that:
- **Build & Test**: Runs on every push to `main` or `develop` branches and PRs
- **Deploy**: Automatically deploys to Azure App Service when code is pushed to `main`

## Prerequisites

1. GitHub repository (already set up at `github.com/iam-madhuk/azure-ocr-springboot`)
2. Azure Subscription
3. Azure Computer Vision Resource
4. Azure App Service Plan

## Step 1: Set Up GitHub Secrets

GitHub Secrets store sensitive credentials securely. Configure them in your repository.

### Access GitHub Secrets

1. Go to your GitHub repository
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Add the following secrets:

### Required Secrets

#### For Azure Deployment

**AZURE_CREDENTIALS**
- Contains Azure authentication credentials
- Generate using Azure CLI:

```bash
az ad sp create-for-rbac --name "github-actions-sp" --role contributor \
  --scopes /subscriptions/<SUBSCRIPTION_ID> \
  --json-auth
```

This command returns JSON that should be stored as `AZURE_CREDENTIALS`.

Example format:
```json
{
  "clientId": "...",
  "clientSecret": "...",
  "subscriptionId": "...",
  "tenantId": "..."
}
```

**AZURE_APP_SERVICE_NAME**
- Your Azure App Service name (e.g., `azure-ocr-app-prod`)
- Create this app service first (see AZURE_DEPLOYMENT.md)

**AZURE_VISION_ENDPOINT**
- Azure Computer Vision endpoint URL
- Format: `https://<resource-name>.cognitiveservices.azure.com/`

**AZURE_VISION_KEY**
- Azure Computer Vision API key

## Step 2: Create Azure App Service

Before deploying, create the Azure App Service:

```bash
# Login to Azure
az login

# Create resource group
az group create \
  --name myResourceGroup \
  --location eastus

# Create App Service Plan
az appservice plan create \
  --name myAppServicePlan \
  --resource-group myResourceGroup \
  --sku B1 \
  --is-linux

# Create Web App with Java 17
az webapp create \
  --resource-group myResourceGroup \
  --plan myAppServicePlan \
  --name azure-ocr-app-prod \
  --runtime "JAVA|17-java17"

# Configure app settings
az webapp config appsettings set \
  --resource-group myResourceGroup \
  --name azure-ocr-app-prod \
  --settings \
    AZURE_VISION_ENDPOINT="https://<your-resource>.cognitiveservices.azure.com/" \
    AZURE_VISION_KEY="<your-api-key>" \
    PORT=8080
```

Save the App Service name (`azure-ocr-app-prod` in the example) as `AZURE_APP_SERVICE_NAME` secret.

## Step 3: Configure Secrets in GitHub

1. Go to repository **Settings** → **Secrets and variables** → **Actions**

2. Click **New repository secret** and add:

| Secret Name | Value |
|------------|-------|
| `AZURE_CREDENTIALS` | JSON from `az ad sp create-for-rbac` |
| `AZURE_APP_SERVICE_NAME` | Your App Service name |
| `AZURE_VISION_ENDPOINT` | Azure Computer Vision endpoint |
| `AZURE_VISION_KEY` | Azure Computer Vision API key |

## Step 4: GitHub Actions Workflows

The project includes two workflows:

### 1. Build & Test (`build.yml`)

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop`

**Steps:**
1. Checkout code
2. Set up JDK 17
3. Build with Maven (skip tests)
4. Run tests
5. Package as JAR
6. Upload artifact (5-day retention)

### 2. Deploy to Azure (`deploy-azure.yml`)

**Triggers:**
- Push to `main` branch only
- Manual trigger via GitHub UI

**Steps:**
1. Checkout code
2. Set up JDK 17
3. Build with Maven (skip tests)
4. Login to Azure
5. Deploy to App Service
6. Logout from Azure

## Workflow Status

View workflow runs in your GitHub repository:
1. Click **Actions** tab
2. Select a workflow to see details
3. Click a run to see step-by-step execution

## Manual Workflow Trigger

To manually trigger deployment (useful for re-deployment):

1. Go to **Actions** tab
2. Click **Deploy to Azure** workflow
3. Click **Run workflow** dropdown
4. Select branch (usually `main`)
5. Click **Run workflow**

## Troubleshooting Workflows

### Workflow Failed to Run

**Check:**
1. File syntax: `.github/workflows/*.yml` files must be valid YAML
2. Event triggers: Check if the trigger event matches your action
3. Permissions: Repository should allow GitHub Actions

### Build Fails

**Check:**
1. Java version requirement (17+)
2. Maven dependencies: Run locally first
3. Azure credentials are valid

### Deployment Fails

**Check:**
1. `AZURE_CREDENTIALS` secret is valid JSON
2. Service principal has contributor role on subscription
3. App Service name is correct
4. Azure credentials are not expired

### View Workflow Logs

1. Go to **Actions** tab
2. Click on the failed workflow run
3. Click on the failed job/step
4. Review detailed logs

## Local Testing

### Test Build Locally
```bash
mvn clean install
```

### Test Docker Build Locally
```bash
# Build JAR first
mvn clean package

# Build Docker image
docker build -t azure-ocr-app:latest .

# Run Docker container
docker run -p 8080:8080 \
  -e AZURE_VISION_ENDPOINT="https://your-resource.cognitiveservices.azure.com/" \
  -e AZURE_VISION_KEY="your-api-key" \
  azure-ocr-app:latest

# Test health endpoint
curl http://localhost:8080/api/ocr/health
```

### Using Docker Compose
```bash
# Copy example env file
cp .env.example .env

# Edit .env with your Azure credentials
nano .env

# Start services
docker-compose up -d

# Test health endpoint
curl http://localhost:8080/api/ocr/health

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

## Deployment Verification

After deployment, verify the app is running:

```bash
# Test the health endpoint
curl https://azure-ocr-app-prod.azurewebsites.net/api/ocr/health

# Test OCR extraction
curl -X POST \
  -F "file=@image.jpg" \
  https://azure-ocr-app-prod.azurewebsites.net/api/ocr/extract
```

## Environment Variables

The following environment variables are available during deployment:

### For GitHub Actions
- `GITHUB_REPOSITORY`: Repository name
- `GITHUB_REF`: Git reference
- `GITHUB_SHA`: Commit SHA
- `GITHUB_WORKFLOW`: Workflow name

### For Docker/App Service
- `AZURE_VISION_ENDPOINT`: Azure Computer Vision endpoint
- `AZURE_VISION_KEY`: Azure Computer Vision API key
- `PORT`: Application port (default: 8080)

## Security Best Practices

1. **Never commit secrets** to the repository
2. **Rotate API keys** regularly
3. **Use service principals** for Azure authentication
4. **Review permissions** for service principals
5. **Enable audit logs** in Azure and GitHub
6. **Use branch protection rules** for `main` branch

### Branch Protection Rules

Recommended settings for `main` branch:
1. Go to **Settings** → **Branches**
2. Add rule for `main` branch
3. Enable:
   - Require pull request reviews before merging
   - Require status checks to pass (select "Build and Test" workflow)
   - Require branches to be up to date before merging
   - Include administrators

## Advanced Configuration

### Custom Deploy Slots

To deploy to different slots (staging, production):

Edit `.github/workflows/deploy-azure.yml`:
```yaml
- name: Deploy to Azure App Service
  uses: azure/webapps-deploy@v2
  with:
    app-name: ${{ secrets.AZURE_APP_SERVICE_NAME }}
    package: target/azure-ocr-springboot-1.0.0.jar
    slot-name: staging  # Change to staging or production
```

### Notifications

Configure notifications for workflow status:
1. Go to **Settings** → **Notifications**
2. Configure email or third-party integrations
3. Choose which events trigger notifications

### Matrix Builds

To test on multiple Java versions, modify `.github/workflows/build.yml`:
```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        java-version: [17, 21]
    steps:
      - uses: actions/setup-java@v3
        with:
          java-version: ${{ matrix.java-version }}
```

## Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Azure App Service Documentation](https://learn.microsoft.com/en-us/azure/app-service/)
- [Azure CLI Reference](https://learn.microsoft.com/en-us/cli/azure/reference-index)
- [Maven Documentation](https://maven.apache.org/guides/index.html)

## Support

For issues or questions:
1. Check workflow logs in GitHub Actions
2. Review Azure App Service logs
3. Check application logs locally
4. Open an issue in the repository

---

**Last Updated**: November 27, 2025
**GitHub Repository**: https://github.com/iam-madhuk/azure-ocr-springboot
