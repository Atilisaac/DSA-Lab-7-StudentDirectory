@echo off
REM Compile every source file and run the acceptance runner with assertions on.
setlocal

if exist out rmdir /s /q out
mkdir out

javac -d out src\*.java test\*.java
if errorlevel 1 (
    echo.
    echo COMPILATION FAILED
    exit /b 1
)

java -ea -cp out LabTestRunner
exit /b %errorlevel%
