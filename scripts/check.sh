#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

source "${SCRIPT_DIR}/env.sh"

echo "== Java =="
java -version

echo
echo "== Gradle =="
"${ROOT_DIR}/gradlew" --version

echo
echo "== Project build =="
"${ROOT_DIR}/gradlew" clean build

echo
echo "Environment looks healthy."
