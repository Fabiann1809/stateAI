# ml-python

Offline tooling for stateAI: synthetic data generation, classifier training, TFLite export and evaluation.

## Setup

Requires Python 3.12.

```sh
py -3.12 -m venv .venv            # Windows (python3.12 -m venv .venv elsewhere)
.venv/Scripts/python -m pip install -r requirements.txt   # .venv/bin/python on macOS/Linux
.venv/Scripts/python -c "import sklearn"
```

## Checks

```sh
.venv/Scripts/python -m ruff check .
.venv/Scripts/python -m ruff format --check .
.venv/Scripts/python -m pytest
```
