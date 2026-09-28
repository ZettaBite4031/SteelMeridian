#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

TOOLING_DIR="${ROOT_DIR}/.tooling"
JAVA_HOME="${TOOLING_DIR}/jdk-21"
DOWNLOAD_DIR="${TOOLING_DIR}/downloads"

JDK_ARCHIVE="${DOWNLOAD_DIR}/jdk-21.tar.gz"

required_commands=(
    curl
    tar
)

for command in "${required_commands[@]}"; do
    if ! command -v "${command}" >/dev/null 2>&1; then
        echo "Missing required command: ${command}"
        exit 1
    fi
done

ARCH="$(uname -m)"

case "${ARCH}" in
    x86_64)
        JDK_URL="https://aka.ms/download-jdk/microsoft-jdk-21-linux-x64.tar.gz"
        ;;
    aarch64|arm64)
        JDK_URL="https://aka.ms/download-jdk/microsoft-jdk-21-linux-aarch64.tar.gz"
        ;;
    *)
        echo "Unsupported architecture: ${ARCH}"
        exit 1
        ;;
esac

mkdir -p "${TOOLING_DIR}" "${DOWNLOAD_DIR}"

if [[ ! -x "${JAVA_HOME}/bin/java" ]]; then
    echo "Downloading Microsoft OpenJDK 21..."

    rm -f "${JDK_ARCHIVE}"

    curl \
        --fail \
        --location \
        --progress-bar \
        "${JDK_URL}" \
        --output "${JDK_ARCHIVE}"

    echo "Extracting JDK..."

    rm -rf "${JAVA_HOME}"
    mkdir -p "${JAVA_HOME}"

    tar \
        --extract \
        --gzip \
        --file "${JDK_ARCHIVE}" \
        --directory "${JAVA_HOME}" \
        --strip-components=1

    rm -f "${JDK_ARCHIVE}"
else
    echo "Project-local JDK already exists."
fi

source "${SCRIPT_DIR}/env.sh"

echo
echo "Java:"
java -version

echo
echo "Gradle:"
"${ROOT_DIR}/gradlew" --version

echo
echo "Bootstrap complete."
