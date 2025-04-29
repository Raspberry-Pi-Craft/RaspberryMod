#!/bin/bash

sleep 1

shopt -s nullglob
while IFS= read -r pattern; do
    for file in ../$pattern; do
        rm -f "$file"
        echo "Deleting $file"
    done
done < ./old.txt

cp -r ./new/* ../

while IFS= read -r command; do
    $command
    echo "Running $command"
done < ./start.txt