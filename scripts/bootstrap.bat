@echo off
setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..") do set "ROOT_DIR=%%~fI"

set "TOOLING_DIR=%ROOT_DIR%\.tooling"
set "JAVA_HOME=%TOOLING_DIR%\jdk-21"
set "DOWNLOAD_DIR=%TOOLING_DIR%\downloads"
set "JDK_ARCHIVE=%DOWNLOAD_DIR%\jdk-21.zip"

if not exist "%TOOLING_DIR%" mkdir "%TOOLING_DIR%"
if not exist "%DOWNLOAD_DIR%" mkdir "%DOWNLOAD_DIR%"

if exist "%JAVA_HOME%\bin\java.exe" (
    echo Project-local JDK already exists.
    goto verify
)

if /I "%PROCESSOR_ARCHITECTURE%"=="AMD64" (
    set "JDK_URL=https://aka.ms/download-jdk/microsoft-jdk-21-windows-x64.zip"
) else if /I "%PROCESSOR_ARCHITECTURE%"=="ARM64" (
    set "JDK_URL=https://aka.ms/download-jdk/microsoft-jdk-21-windows-aarch64.zip"
) else (
    echo Unsupported architecture: %PROCESSOR_ARCHITECTURE%
    exit /b 1
)

echo Downloading Microsoft OpenJDK 21...

powershell -NoProfile -ExecutionPolicy Bypass ^
    -Command "Invoke-WebRequest -Uri '%JDK_URL%' -OutFile '%JDK_ARCHIVE%'"

if errorlevel 1 exit /b 1

echo Extracting JDK...

if exist "%JAVA_HOME%" rmdir /s /q "%JAVA_HOME%"

set "TEMP_JDK=%TOOLING_DIR%\jdk-temp"

if exist "%TEMP_JDK%" rmdir /s /q "%TEMP_JDK%"
mkdir "%TEMP_JDK%"

powershell -NoProfile -ExecutionPolicy Bypass ^
    -Command "Expand-Archive -Path '%JDK_ARCHIVE%' -DestinationPath '%TEMP_JDK%' -Force"

if errorlevel 1 exit /b 1

for /d %%D in ("%TEMP_JDK%\*") do (
    move "%%D" "%JAVA_HOME%" >nul
    goto extracted
)

:extracted

rmdir /s /q "%TEMP_JDK%"
del /q "%JDK_ARCHIVE%"

:verify

call "%SCRIPT_DIR%env.bat"
if errorlevel 1 exit /b 1

echo.
echo Java:
"%JAVA_HOME%\bin\java.exe" -version

echo.
echo Gradle:
call "%ROOT_DIR%\gradlew.bat" --version

echo.
echo Bootstrap complete.
