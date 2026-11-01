@echo off
echo =======================================================================
echo          COMPILING PERSONAL EXPENSE TRACKER (JAVA SWING)               
echo =======================================================================

if not exist bin mkdir bin

echo Finding source files...
dir /s /b src\*.java > sources.txt

echo Compiling Java source files with javac...
javac -d bin @sources.txt
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    exit /b %ERRORLEVEL%
)

echo Packaging executable PersonalExpenseTracker.jar...
jar --create --file PersonalExpenseTracker.jar --main-class expense.Main -C bin .

if exist sources.txt del sources.txt

echo =======================================================================
echo [SUCCESS] Build completed successfully!
echo Executable JAR: PersonalExpenseTracker.jar
echo Binaries Class folder: bin/
echo =======================================================================
pause
