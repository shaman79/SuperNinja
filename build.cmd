@echo off
REM Build script for SuperNinja

echo =================================
echo   SuperNinja Build Script
echo =================================
echo.

REM Check if JAVA_HOME is set
if "%JAVA_HOME%"=="" (
    echo ERROR: JAVA_HOME is not set.
    echo Please set JAVA_HOME to your JDK 17+ installation.
    echo Example: set JAVA_HOME=C:\Program Files\Java\jdk-17
    exit /b 1
)

echo Using JAVA_HOME: %JAVA_HOME%
echo.

REM Build with Maven wrapper
echo Building SuperNinja...
call mvnw.cmd clean package -DskipTests

if %ERRORLEVEL% neq 0 (
    echo.
    echo BUILD FAILED!
    exit /b 1
)

echo.
echo =================================
echo   Build Successful!
echo =================================
echo.
echo Run with: java -jar target\superninja-1.0.0.jar
echo Windowed: java -jar target\superninja-1.0.0.jar -w
echo.
