#!/bin/bash
source ../setcloudenv.sh
ENCRYPT_PAYLOADS=$1 poetry run python worker.py
