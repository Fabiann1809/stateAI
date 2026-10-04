"""Trains the state classifier and compares it with the rule engine on held-out users.

``python -m stateai_ml.evaluate_classifier --data data/generated`` writes ``reports/classifier.md``
and ``models/state_classifier.json``.
"""

import argparse
import json
from pathlib import Path

import pandas as pd

from stateai_ml import rules
from stateai_ml.classifier_report import Comparison, Scores, render
from stateai_ml.dataset import build_dataset, clean_windows
from stateai_ml.features import WindowFeatures
from stateai_ml.model import CLASSES, INPUT_COLUMNS, LinearModel, predict, to_linear_model, train
from stateai_ml.synthetic.users import USERS

TRAIN_USERS = ["cycle60", "cycle110", "irregular"]
TEST_USERS = ["cycle90", "no_cycle"]
FEATURE_FIELDS = list(WindowFeatures.__dataclass_fields__)


def rule_predictions(windows: pd.DataFrame, baselines: pd.Series) -> list[str]:
    return [
        rules.classify(WindowFeatures(**row[FEATURE_FIELDS].to_dict()), baseline, row["category"])
        for (_, row), baseline in zip(windows.iterrows(), baselines, strict=True)
    ]


def save_model(model: LinearModel, path: Path) -> None:
    parameters = {
        "inputs": INPUT_COLUMNS,
        "classes": CLASSES,
        "mean": model.mean.tolist(),
        "scale": model.scale.tolist(),
        "weights": model.weights.tolist(),
        "bias": model.bias.tolist(),
    }
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(parameters, indent=2) + "\n", encoding="utf-8", newline="\n")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--data", type=Path, default=Path("data/generated"))
    parser.add_argument("--reports", type=Path, default=Path("reports"))
    parser.add_argument("--models", type=Path, default=Path("models"))
    arguments = parser.parse_args()

    train_windows = clean_windows(build_dataset(arguments.data, TRAIN_USERS))
    test = clean_windows(build_dataset(arguments.data, TEST_USERS))
    model = to_linear_model(train(train_windows))
    true_resting = {name: USERS[name].resting_heart_rate for name in TEST_USERS}
    comparison = Comparison(
        train_users=TRAIN_USERS,
        test_users=TEST_USERS,
        train_windows=len(train_windows),
        test=test,
        rules=Scores.of(test["label"], rule_predictions(test, test["baseline"])),
        rules_true_baseline=Scores.of(test["label"], rule_predictions(test, test["user"].map(true_resting))),
        model=Scores.of(test["label"], predict(model, test)),
        true_resting=true_resting,
    )
    text = render(comparison)
    arguments.reports.mkdir(parents=True, exist_ok=True)
    (arguments.reports / "classifier.md").write_text(text, encoding="utf-8", newline="\n")
    save_model(model, arguments.models / "state_classifier.json")
    print(text)


if __name__ == "__main__":
    main()
