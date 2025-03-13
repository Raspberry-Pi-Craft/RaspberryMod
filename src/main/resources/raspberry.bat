@echo off

:: Шаг 1: Ждем 1 секунду
timeout /t 1 > nul

:: Шаг 2: Удаляем файлы, указанные в old.txt относительно ../
for /f "delims=" %%i in (./old.txt) do (
    del /f /q "..\%%i"
)

:: Шаг 3: Переносим все файлы из папки ./new в ../
xcopy /e /i /y .\new\* ..\

:: Шаг 4: Запускаем команду из start.txt
for /f "delims=" %%j in (./start.txt) do (
    %%j
    echo %%j
)

pause