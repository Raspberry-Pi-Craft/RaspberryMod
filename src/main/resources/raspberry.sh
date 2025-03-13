#!/bin/bash

# Шаг 1: Ждем 0.1 секунды
sleep 1

# Шаг 2: Удаляем файлы, указанные в old.txt относительно ../
while IFS= read -r line; do
    rm -f "../$line"
done < ./old.txt

# Шаг 3: Переносим все файлы из папки ./new в ../
cp -r ./new/* ../

# Шаг 4: Запускаем команду из start.txt
while IFS= read -r command; do
    $command
done < ./start.txt

read -p "Press enter to continue"