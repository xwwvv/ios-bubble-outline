@echo off
setlocal

set "APP_HOME=%~dp0"
set "WRAPPER_JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar"
set "GRADLE_VERSION=8.10.2"
set "DIST_DIR=%LOCALAPPDATA%\gradle-dist"

if exist "%WRAPPER_JAR%" (
    java -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
    exit /b %errorlevel%
)

where gradle >nul 2>nul
if %errorlevel%==0 (
    gradle %*
    exit /b %errorlevel%
)

if not exist "%DIST_DIR%\gradle-%GRADLE_VERSION%\bin\gradle.bat" (
    if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%DIST_DIR%\gradle-%GRADLE_VERSION%-bin.zip'"
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%DIST_DIR%\gradle-%GRADLE_VERSION%-bin.zip' -DestinationPath '%DIST_DIR%' -Force"
)

call "%DIST_DIR%\gradle-%GRADLE_VERSION%\bin\gradle.bat" %*
endlocal