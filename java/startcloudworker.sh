#!/bin/bash
source ../setcloudenv.sh
ENCRYPT_PAYLOADS=$1 ./gradlew run --console=plain
