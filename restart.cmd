@echo off
wmic process where "name='javaw.exe' and commandline like '%%com.hexgame.Main%%'" delete >nul 2>&1

cd /d "%~dp0"
call mvn compile -q
if %ERRORLEVEL% neq 0 (
    echo BUILD FAILED
    pause
    exit /b 1
)

start "4X Strategy" javaw -cp target\classes com.hexgame.Main
