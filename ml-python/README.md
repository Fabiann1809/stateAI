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
python -m stateai_ml.prediction_parity                      # writes shared/parity/prediction_cases.json
```

After exporting, copy `models/state_classifier.tflite` into `ml/src/main/assets` (a unit test in `:ml`
fails if they differ) and regenerate the prediction cases. Kotlin checks them in two places: the model
inputs in `ModelInputsParityTest` (JVM) and the TFLite probabilities in `PredictionParityTest`
(`./gradlew :ml:connectedDebugAndroidTest`, needs an emulator).

CI installs only `requirements.txt`; the TFLite comparison test is skipped there and runs locally
when TensorFlow is installed.

## End-to-end simulation

```sh
python -m stateai_ml.endtoend --days 14     # writes reports/endtoend_days.csv and prints the mean day
```

Simulates the same days of every synthetic user with each policy, minute by minute, in a closed-loop
version of the synthetic user where effort builds up with work and pauses recover it
(`src/stateai_ml/endtoend/world.py`). The fixed timer works 25 minutes and pauses 5. stateAI runs
Python ports of the watch's pipeline (windows, rules, smoother, haptic policy, target-block cue and
rate limiter) and the simulated person follows a played cue 80 % of the time, pausing 5 minutes.
Metrics come from
the ground-truth latent state: minutes worked, in focus (`LOW`), in overload (`HIGH`), restless, and
the pauses taken.

The report with charts is `notebooks/endtoend_report.ipynb` (committed with its outputs). To re-run it:

```sh
pip install -r requirements-notebook.txt
jupyter execute --inplace notebooks/endtoend_report.ipynb   # or open it in any notebook editor
```

All of it is simulated: it shows the integrated system working, not that stateAI beats a fixed timer
for real people.
