#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

TOOLING_DIR="${ROOT_DIR}/.tooling"
JAVA_HOME="${TOOLING_DIR}/jdk-21"

if [[ ! -x "${JAVA_HOME}/bin/java" ]]; then
  echo "Project-local JDK not found at:"
  echo "  ${JAVA_HOME}"
  echo
  echo "Run:"
  echo "  ./scripts/bootstrap.sh"
  return 1 2>/dev/null || exit 1
fi

export JAVA_HOME
export PATH="${JAVA_HOME}/bin:${PATH}"
