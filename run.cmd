@echo off
REM Run script for SuperNinja

echo =================================
echo   SuperNinja - Starting Game
echo =================================
echo.

set JAR_FILE=target\superninja-1.0.0.jar

if not exist "%JAR_FILE%" (
    echo JAR file not found! Running build first...
    call build.cmd
    if %ERRORLEVEL% neq 0 exit /b 1
)

echo Starting SuperNinja...
echo Press ESC to exit, F2 for debug mode, R to restart
echo.

java -jar "%JAR_FILE%" %*
