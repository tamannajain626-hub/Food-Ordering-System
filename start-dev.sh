#!/bin/bash
# Safe startup script for Food Ordering System (macOS/zsh compatible)
# Loads environment variables from .env without exposing secrets
# Detects existing process on 8080 and does not kill unrelated apps

cd "$(dirname "$0")"

# Load .env if present (export all variables)
if [ -f .env ]; then
    set -a
    # Source .env (values are quoted in our setup; shellcheck not required)
    # Use . to be compatible with /bin/bash on macOS
    . ./.env
    set +a
fi

# Check port 8080
if command -v lsof >/dev/null 2>&1; then
    if lsof -nP -iTCP:8080 -sTCP:LISTEN >/dev/null 2>&1; then
        echo "NOTICE: Port 8080 already has a listener (may be this project)."
        echo "The app is already running or another process is using it."
        echo "Check with: lsof -nP -iTCP:8080 -sTCP:LISTEN"
        echo "Not killing anything. Starting anyway may fail if it's this same app."
    fi
fi

# Start Spring Boot via Maven
mvn spring-boot:run
