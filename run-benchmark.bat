@echo off
setlocal
chcp 65001 > nul
cd /d "%~dp0"

echo ===================================================
echo   🤖 FastDaemon JMH Microbenchmark Suite
echo ===================================================
echo.

echo [1/2] Building FastDaemon locally...
call mvn clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] FastDaemon build failed!
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Packaging JMH uber-jar...
cd examples\Benchmark
call mvn clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] JMH benchmark packaging failed!
    cd ..\..
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Running JMH Benchmarks...
java -jar target\benchmarks.jar -f 1 -wi 2 -i 3 -tu ms -bm thrpt

cd ..\..
pause
