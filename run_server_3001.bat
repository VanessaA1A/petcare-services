@echo off
cd /d %~dp0
set PORT=3001
node src/index.js
pause
