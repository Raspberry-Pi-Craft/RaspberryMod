@echo off

timeout /t 5 > nul

for /f "delims=" %%i in (.\old.txt) do (
    for %%f in ("../%%i") do (
        del /f /q "%%~ff"
        echo Deleting %%~ff
    )
)

xcopy /e /i /y .\new\* ..\

for /f "delims=" %%j in (.\start.txt) do (
    %%j
    echo Running %%j
)