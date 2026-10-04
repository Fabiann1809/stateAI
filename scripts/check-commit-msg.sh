#!/usr/bin/env sh
# Validates a commit message against Conventional Commits and project rules.
# Usage: check-commit-msg.sh <file-with-message>
set -eu

message_file="$1"
subject=$(grep -v '^#' "$message_file" | head -n 1)

pattern='^(feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert)(\([a-z0-9-]+\))?!?: [^ ].{0,71}$'

if ! printf '%s' "$subject" | grep -Eq "$pattern"; then
    echo "Invalid commit subject: \"$subject\""
    echo "Expected: <type>(<optional-scope>): <description> (max 72 chars)"
    echo "Types: feat fix docs style refactor perf test build ci chore revert"
    exit 1
fi

if printf '%s' "$subject" | grep -Eqi '\bT-[0-9]+\.[0-9]+\b'; then
    echo "Commit subjects must describe the change, not reference task numbers."
    exit 1
fi

if grep -qi '^co-authored-by:' "$message_file"; then
    echo "Co-Authored-By trailers are not allowed in this repository."
    exit 1
fi
