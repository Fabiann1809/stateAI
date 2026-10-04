"""The shared prediction cases are current and the exported TFLite model reproduces them."""

import json
from pathlib import Path

import numpy as np
import pytest

from stateai_ml.export_tflite import TOLERANCE, load_model, tflite_probabilities
from stateai_ml.prediction_parity import CASES_FILE, MODEL_FILE, build_cases

CASES = json.loads(CASES_FILE.read_text(encoding="utf-8"))
TFLITE_FILE = Path(__file__).parents[1] / "models" / "state_classifier.tflite"


def test_shared_cases_match_the_current_model() -> None:
    regenerated = build_cases(load_model(MODEL_FILE))

    assert [case["name"] for case in regenerated] == [case["name"] for case in CASES]
    for expected, actual in zip(CASES, regenerated, strict=True):
        assert np.allclose(expected["inputs"], actual["inputs"], atol=1e-9), expected["name"]
        assert np.allclose(expected["probabilities"], actual["probabilities"], atol=1e-9), expected["name"]


def test_cases_cover_every_level() -> None:
    assert {case["level"] for case in CASES} == {"LOW", "MEDIUM", "HIGH"}


def test_tflite_reproduces_the_shared_cases() -> None:
    pytest.importorskip("tensorflow", reason="export dependencies are in requirements-export.txt")
    inputs = np.array([case["inputs"] for case in CASES])

    actual = tflite_probabilities(TFLITE_FILE.read_bytes(), inputs)

    assert np.abs(actual - np.array([case["probabilities"] for case in CASES])).max() <= TOLERANCE
