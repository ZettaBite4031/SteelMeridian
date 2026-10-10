@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..") do set "ROOT_DIR=%%~fI"

set "TOOLING_DIR=%ROOT_DIR%\.tooling"
set "PROJECT_JAVA_HOME=%TOOLING_DIR%\jdk-21"

if not exist "%PROJECT_JAVA_HOME%\bin\java.exe" (
    echo Project-local JDK not found at:
    echo   %PROJECT_JAVA_HOME%
    echo.
    echo Run:
    echo   scripts\bootstrap.bat
    exit /b 1
)

endlocal & (
    set "JAVA_HOME=%PROJECT_JAVA_HOME%"
    set "PATH=%PROJECT_JAVA_HOME%\bin;%PATH%"
)
