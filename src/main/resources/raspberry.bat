@echo off

timeout /t 1 > nul

for /f "delims=" %%i in (.\old.txt) do (
    del /f /q "../%%i"
)

xcopy /e /i /y .\new\* ..\

for /f "delims=" %%j in (.\start.txt) do (
    %%j
    echo %%j
)