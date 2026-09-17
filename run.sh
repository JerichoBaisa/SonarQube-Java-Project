#!/usr/bin/env sh

set -eu

echo "========================================"
echo " DevOps Shack - ProjectOps Studio"
echo "========================================"

# ------------------------------------------------
# Install Java 21 if missing
# ------------------------------------------------

if ! command -v java >/dev/null 2>&1; then
    echo "Java not found."
    echo "Installing OpenJDK 21..."

    sudo apt-get update
    sudo apt-get install -y openjdk-21-jdk
else
    echo "Java found:"
    java -version
fi

# ------------------------------------------------
# Install Maven if missing
# ------------------------------------------------

if ! command -v mvn >/dev/null 2>&1; then
    echo "Maven not found."
    echo "Installing Maven..."

    sudo apt-get update
    sudo apt-get install -y maven
else
    echo "Maven found:"
    mvn -version
fi

# ------------------------------------------------
# Start application
# ------------------------------------------------

echo ""
echo "Starting DevOps Shack ProjectOps Studio..."
echo ""

mvn spring-boot:run
