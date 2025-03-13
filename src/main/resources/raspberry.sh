#!/bin/bash

sleep 1

while IFS= read -r line; do
    rm -f "../$line"
done < ./old.txt

cp -r ./new/* ../

while IFS= read -r command; do
    $command
done < ./start.txt