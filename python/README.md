# Python Money Transfer Worker
A money transfer demo worker written using the Temporal Python SDK, which is compatable with the Java UI.

See the main [README](../README.md) for instructions on how to use the UI.

## Prerequisties

* [Poetry](https://python-poetry.org/docs/) - Python Dependency Management

## Set up Python Environment
```bash
poetry install
```

## Run Worker Locally
Be sure you set up your Python Virtual Environment before running the worker

```bash
./startlocalworker.sh
```

## Start Worker on Temporal Cloud
If you haven't updated the setcloudenv.sh file, see the main [README](../README.md) for instructions

```bash
./startcloudworker.sh
```
