@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..") do set "ROOT_DIR=%%~fI"

call "%SCRIPT_DIR%env.bat"
if errorlevel 1 exit /b 1

echo == Java ==
"%JAVA_HOME%\bin\java.exe" -version
if errorlevel 1 exit /b 1

echo.
echo == Gradle ==
call "%ROOT_DIR%\gradlew.bat" --version
if errorlevel 1 exit /b 1

echo.
echo == Project build ==
call "%ROOT_DIR%\gradlew.bat" clean build
if errorlevel 1 exit /b 1

echo.
echo Environment looks healthy.
