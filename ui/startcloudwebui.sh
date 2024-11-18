#!/bin/bash

source ../setcloudenv.sh

echo "Starting Web UI on http://localhost:7070 ..."
ENCRYPT_PAYLOADS=$1 ./gradlew run --console=plain
