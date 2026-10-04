"""Smoke tests that the scientific stack is installed and importable."""

import numpy as np
import sklearn


def test_sklearn_is_importable() -> None:
    assert sklearn.__version__


def test_numpy_computes() -> None:
    assert np.mean([1.0, 2.0, 3.0]) == 2.0
