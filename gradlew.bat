@rem Self-bootstrapping Gradle wrapper for Windows
@echo off
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.

set WRAPPER_JAR=%DIRNAME%gradle\wrapper\gradle-wrapper.jar

if not exist "%WRAPPER_JAR%" (
    echo Gradle wrapper jar not found. Bootstrapping...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('https://raw.githubusercontent.com/gradle/gradle/release/gradle/wrapper/gradle-wrapper.jar', '%WRAPPER_JAR%')"
    if %ERRORLEVEL% neq 0 (
        echo Failed to download gradle-wrapper.jar
        exit /b %ERRORLEVEL%
    )
)

java -jar "%WRAPPER_JAR%" %*
