#!/bin/bash
python3 -m venv venv
ENCRYPT_PAYLOADS=$1 python worker.py