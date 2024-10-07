#!/bin/bash
source ../setcloudenv.sh
cd src
ENCRYPT_PAYLOADS=$1 dotnet run
