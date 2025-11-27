#!/bin/bash

# Quick Start Script for Azure OCR Spring Boot Application (Linux/Mac)
# This script builds and runs the application

echo ""
echo "========================================"
echo "Azure OCR Spring Boot - Quick Start"
echo "========================================"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed"
    echo "Please install Maven from https://maven.apache.org/download.cgi"
    echo ""
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed"
    echo "Please install Java 17 or higher"
    echo ""
    exit 1
fi

echo "Checking Java version..."
java -version

echo ""
echo "========================================"
echo "Step 1: Building the project"
echo "========================================"
echo ""

mvn clean install

if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Build failed!"
    echo "Please check the error messages above"
    echo ""
    exit 1
fi

echo ""
echo "========================================"
echo "Step 2: Running the application"
echo "========================================"
echo ""
echo "Starting Spring Boot application..."
echo "Application will be available at: http://localhost:8080"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

mvn spring-boot:run
