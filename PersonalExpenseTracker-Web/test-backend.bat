@echo off
echo =======================================================================
echo          RUNNING SPRING BOOT BACKEND AUTOMATED TEST SUITE
echo =======================================================================

cd /d "%~dp0backend"
call "..\..\.tools\maven\bin\mvn.cmd" test
pause
