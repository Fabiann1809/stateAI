"""Rule port and learned model behave as documented."""

import numpy as np
import pandas as pd

from stateai_ml import rules
from stateai_ml.features import extract
from stateai_ml.model import CLASSES, INPUT_COLUMNS, predict, to_linear_model, train


def window(heart_rate: float, movement: float = 0.05):
    return extract([heart_rate] * 180, [movement] * 180)


def test_rules_match_the_watch_thresholds_for_study() -> None:
    assert rules.classify(window(67.0), 65.0, "STUDY") == "LOW"
    assert rules.classify(window(73.0), 65.0, "STUDY") == "MEDIUM"
    assert rules.classify(window(80.0), 65.0, "STUDY") == "HIGH"


def test_rules_use_category_sensitivity_and_movement() -> None:
    assert rules.classify(window(75.0), 65.0, "READING") == "HIGH"
    assert rules.classify(window(66.0, movement=0.4), 65.0, "STUDY") == "MEDIUM"
    assert rules.classify(window(66.0, movement=0.4), 65.0, "COLLAB") == "LOW"


def test_rules_need_a_heart_rate() -> None:
    assert rules.classify(extract([None] * 10, [0.1] * 10), 65.0, "STUDY") is None


def test_model_learns_separable_levels_and_exports_identical_predictions() -> None:
    rng = np.random.default_rng(0)
    offsets = {"LOW": 0.0, "MEDIUM": 6.0, "HIGH": 15.0}
    rows = [
        {column: rng.normal(1.0, 0.1) for column in INPUT_COLUMNS}
        | {"heart_rate_delta": offset + rng.normal(0, 1), "label": label}
        for label, offset in offsets.items()
        for _ in range(200)
    ]
    frame = pd.DataFrame(rows)
    pipeline = train(frame)
    linear = to_linear_model(pipeline)

    exported = predict(linear, frame)
    assert (exported == pipeline.predict(frame[INPUT_COLUMNS].to_numpy())).all()
    assert (exported == frame["label"]).mean() > 0.95
    assert linear.weights.shape == (len(CLASSES), len(INPUT_COLUMNS))
