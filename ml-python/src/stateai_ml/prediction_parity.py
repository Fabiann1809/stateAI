"""Shared prediction parity cases between the Kotlin and Python classifiers.

``python -m stateai_ml.prediction_parity`` rewrites ``shared/parity/prediction_cases.json``. Each case
takes a raw window from ``feature_cases.json`` with a resting heart rate and elapsed minutes, and
stores the model inputs and the probabilities of the original (non-TFLite) model. Kotlin checks its
inputs on the JVM and the bundled TFLite probabilities on the emulator against the same file.
"""

import json
from pathlib import Path

import numpy as np

from stateai_ml.export_tflite import load_model
from stateai_ml.features import extract
from stateai_ml.model import CLASSES, INPUT_COLUMNS, LinearModel
from stateai_ml.parity import CASES_FILE as FEATURE_CASES_FILE

MODEL_FILE = Path(__file__).resolve().parents[2] / "models" / "state_classifier.json"
CASES_FILE = FEATURE_CASES_FILE.parent / "prediction_cases.json"

# (resting heart rate, elapsed minutes): below, near and well above each window's mean heart rate.
CONTEXTS = [(80.0, 5.0), (66.0, 25.0), (52.0, 60.0)]


def build_cases(model: LinearModel) -> list[dict]:
    windows = json.loads(FEATURE_CASES_FILE.read_text(encoding="utf-8"))
    cases = []
    for window in windows:
        features = extract(window["heart_rates"], window["movements"])
        if features.mean_heart_rate is None:
            continue
        for resting, elapsed in CONTEXTS:
            inputs = model_inputs(features.as_dict(), resting, elapsed)
            probabilities = model.probabilities(np.array([inputs]))[0]
            cases.append(
                {
                    "name": f"{window['name']}_rest{resting:g}_min{elapsed:g}",
                    "window": window["name"],
                    "resting_heart_rate": resting,
                    "elapsed_minutes": elapsed,
                    "inputs": inputs,
                    "probabilities": [float(p) for p in probabilities],
                    "level": CLASSES[int(probabilities.argmax())],
                }
            )
    return cases


def model_inputs(features: dict, resting: float, elapsed: float) -> list[float]:
    """Same order and definitions as ``INPUT_COLUMNS`` and Kotlin's ``ModelInputs``."""
    values = dict(features, heart_rate_delta=features["mean_heart_rate"] - resting, elapsed_minutes=elapsed)
    return [float(values[column]) for column in INPUT_COLUMNS]


def main() -> None:
    cases = build_cases(load_model(MODEL_FILE))
    CASES_FILE.write_text(json.dumps(cases, indent=2) + "\n", encoding="utf-8", newline="\n")
    levels = {level: sum(case["level"] == level for case in cases) for level in CLASSES}
    print(f"wrote {CASES_FILE} ({len(cases)} cases, levels {levels})")


if __name__ == "__main__":
    main()
