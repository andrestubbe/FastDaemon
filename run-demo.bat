@echo off
setlocal
chcp 65001 > nul
cd /d "%~dp0"

echo ===================================================
echo   🤖 FastDaemon Showcase Demo
echo ===================================================
echo.

echo [1/2] Building FastDaemon...
call mvn clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] FastDaemon build failed!
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Building and Launching Demo...
cd examples\Demo
call mvn compile -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Demo compilation failed!
    cd ..\..
    pause
    exit /b %ERRORLEVEL%
)

java -cp "target\classes;..\..\target\classes;..\..\target\*" fastdaemon.demo.Demo

cd ..\..
pause
