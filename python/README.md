# Python Money Transfer Worker
A money transfer demo worker written using the Temporal Python SDK, which is compatable with the Java UI.

See the main [README](../README.md) for instructions on how to use the UI.

## Set up Python Virtual Environment
```bash
python3 -m venv venv
source venv/bin/activate # On Windows, use `venv\Scripts\activate`
pip install temporalio
```

## Run Worker Locally
Be sure you set up your Python Virtual Environment before running the worker

```bash
python worker.py
```
