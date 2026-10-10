@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..") do set "ROOT_DIR=%%~fI"

call "%SCRIPT_DIR%env.bat"
if errorlevel 1 exit /b 1

call "%ROOT_DIR%\gradlew.bat" runClient %*
exit /b %errorlevel%
