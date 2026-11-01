@echo off
echo =======================================================================
echo     STARTING PERSONAL EXPENSE TRACKER - SPRING BOOT BACKEND (PORT 8080)
echo =======================================================================

cd /d "%~dp0backend"

if exist "target\personal-expense-tracker-backend-1.0.0.jar" (
    echo Launching pre-built Spring Boot JAR...
    java -jar target\personal-expense-tracker-backend-1.0.0.jar
) else (
    echo Building and launching Spring Boot application with Maven...
    call "..\..\.tools\maven\bin\mvn.cmd" spring-boot:run
)
pause
