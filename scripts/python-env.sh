#!/usr/bin/env sh
# Prints the path of the ml-python virtual environment interpreter, or nothing if it does not exist.
for candidate in ml-python/.venv/bin/python ml-python/.venv/Scripts/python.exe; do
    if [ -x "$candidate" ]; then
        echo "$candidate"
        exit 0
    fi
done
