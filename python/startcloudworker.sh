#!/bin/bash
source ../setcloudenv.sh
ENCRYPT_PAYLOADS=$1 python worker.py
