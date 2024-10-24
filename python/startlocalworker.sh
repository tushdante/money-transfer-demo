#!/bin/bash
poetry install --no-root
ENCRYPT_PAYLOADS=$1 poetry run python worker.py
