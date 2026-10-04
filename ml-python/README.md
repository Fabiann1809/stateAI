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

## Data exported from the watch

In a debug build, open *Depuración → Exportar resúmenes* on the watch, then pull the file:

```sh
adb exec-out run-as com.stateai cat files/exports/segments.csv > data/segments.csv
```

Load it with `stateai_ml.watch_export.load_segments("data/segments.csv")`. The columns are defined by
`SegmentCsv` in `core-domain`.

## Model pipeline

```sh
python -m stateai_ml.synthetic --out data/generated       # synthetic users (latent-state generator)
python -m stateai_ml.evaluate_classifier                    # trains, compares with the rules, writes
                                                            # reports/classifier.md and models/state_classifier.json
pip install -r requirements-export.txt                     # TensorFlow, only needed to export
python -m stateai_ml.export_tflite                          # writes models/state_classifier.tflite and
                                                            # checks it matches the original model
```

CI installs only `requirements.txt`; the TFLite comparison test is skipped there and runs locally
when TensorFlow is installed.
