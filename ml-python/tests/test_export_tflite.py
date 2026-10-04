"""The TFLite model gives the same probabilities as the original classifier."""

from pathlib import Path

import numpy as np
import pytest

from stateai_ml.export_tflite import TOLERANCE, folded_parameters, load_model, max_difference, to_tflite

MODEL = Path(__file__).parents[1] / "models" / "state_classifier.json"


def test_folding_standardization_keeps_the_logits() -> None:
    model = load_model(MODEL)
    inputs = model.mean + np.random.default_rng(1).normal(0, 1, (50, len(model.mean))) * model.scale
    kernel, bias = folded_parameters(model)

    folded = inputs @ kernel + bias
    original = ((inputs - model.mean) / model.scale) @ model.weights.T + model.bias
    assert np.allclose(folded, original, atol=1e-4)


def test_tflite_matches_the_original_model() -> None:
    pytest.importorskip("tensorflow", reason="export dependencies are in requirements-export.txt")
    model = load_model(MODEL)

    assert max_difference(model, to_tflite(model)) <= TOLERANCE
