#!/usr/bin/env sh
# Fails if any Kotlin or Python source file exceeds the maximum line count.
# Usage: check-file-size.sh [files...]  (defaults to all tracked source files)
set -eu

max_lines="${MAX_FILE_LINES:-300}"

if [ "$#" -eq 0 ]; then
    set -- $(git ls-files '*.kt' '*.kts' '*.py')
fi

failed=0
for file in "$@"; do
    case "$file" in
        *.kt|*.kts|*.py) ;;
        *) continue ;;
    esac
    [ -f "$file" ] || continue
    lines=$(wc -l < "$file" | tr -d ' ')
    if [ "$lines" -gt "$max_lines" ]; then
        echo "$file has $lines lines (max $max_lines). Split it by responsibility."
        failed=1
    fi
done

exit "$failed"
