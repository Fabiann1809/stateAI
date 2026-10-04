"""The learned state classifier: standardized window features into a multinomial logistic regression."""

from dataclasses import dataclass

import numpy as np
import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler

INPUT_COLUMNS = [
    "heart_rate_delta",
    "heart_rate_std_dev",
    "heart_rate_mean_abs_diff",
    "mean_movement",
    "fidget_count",
    "elapsed_minutes",
]
CLASSES = ["LOW", "MEDIUM", "HIGH"]
MAX_ITERATIONS = 1000


@dataclass(frozen=True)
class LinearModel:
    """Everything needed to reproduce the classifier elsewhere (TFLite, Kotlin)."""

    mean: np.ndarray
    scale: np.ndarray
    weights: np.ndarray
    bias: np.ndarray

    def probabilities(self, inputs: np.ndarray) -> np.ndarray:
        logits = ((inputs - self.mean) / self.scale) @ self.weights.T + self.bias
        logits -= logits.max(axis=1, keepdims=True)
        exponentials = np.exp(logits)
        return exponentials / exponentials.sum(axis=1, keepdims=True)


def train(windows: pd.DataFrame) -> Pipeline:
    pipeline = Pipeline(
        [("scale", StandardScaler()), ("classify", LogisticRegression(max_iter=MAX_ITERATIONS))],
    )
    pipeline.fit(windows[INPUT_COLUMNS].to_numpy(), windows["label"].to_numpy())
    return pipeline


def to_linear_model(pipeline: Pipeline) -> LinearModel:
    """Exports parameters with classes in the fixed CLASSES order."""
    scaler: StandardScaler = pipeline.named_steps["scale"]
    classifier: LogisticRegression = pipeline.named_steps["classify"]
    order = [list(classifier.classes_).index(name) for name in CLASSES]
    return LinearModel(
        mean=scaler.mean_.copy(),
        scale=scaler.scale_.copy(),
        weights=classifier.coef_[order].copy(),
        bias=classifier.intercept_[order].copy(),
    )


def predict(model: LinearModel, windows: pd.DataFrame) -> np.ndarray:
    probabilities = model.probabilities(windows[INPUT_COLUMNS].to_numpy())
    return np.array(CLASSES)[probabilities.argmax(axis=1)]
