@echo off
REM Quick Start Script for Azure OCR Spring Boot Application (Windows)
REM This script builds and runs the application

echo.
echo ========================================
echo Azure OCR Spring Boot - Quick Start
echo ========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven from https://maven.apache.org/download.cgi
    echo.
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher
    echo.
    pause
    exit /b 1
)

echo Checking Java version...
java -version

echo.
echo ========================================
echo Step 1: Building the project
echo ========================================
echo.

call mvn clean install

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Build failed!
    echo Please check the error messages above
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Step 2: Running the application
echo ========================================
echo.
echo Starting Spring Boot application...
echo Application will be available at: http://localhost:8080
echo.
echo Press Ctrl+C to stop the application
echo.

call mvn spring-boot:run

pause
