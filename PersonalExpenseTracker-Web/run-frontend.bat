@echo off
echo =======================================================================
echo       STARTING PERSONAL EXPENSE TRACKER - REACT FRONTEND (PORT 5173)
echo =======================================================================

cd /d "%~dp0frontend"
set "PATH=%~dp0..\.tools\nodejs;%PATH%"

echo Starting Vite development server...
npm run dev
pause
