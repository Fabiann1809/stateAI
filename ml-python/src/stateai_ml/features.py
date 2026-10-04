"""Window features, identical to the watch's FeatureExtractor (definitions in docs/features.md)."""

from collections.abc import Iterator, Sequence
from dataclasses import asdict, dataclass

import numpy as np

WINDOW_SECONDS = 180
STEP_SECONDS = 60
FIDGET_THRESHOLD = 1.5
HIGH_MOVEMENT_THRESHOLD = 1.0
MAX_HIGH_MOVEMENT_SHARE = 0.3
MIN_HEART_RATE_COVERAGE = 0.5


@dataclass(frozen=True)
class WindowFeatures:
    sample_count: int
    heart_rate_count: int
    mean_heart_rate: float | None
    heart_rate_std_dev: float
    heart_rate_mean_abs_diff: float
    mean_movement: float
    fidget_count: int
    high_movement_share: float

    def as_dict(self) -> dict:
        return asdict(self)


def extract(heart_rates: Sequence[float | None], movements: Sequence[float]) -> WindowFeatures:
    """Features of one window of chronological samples; ``None`` or NaN means no heart rate."""
    rates = [_value_or_none(rate) for rate in heart_rates]
    available = np.array([rate for rate in rates if rate is not None], dtype=float)
    moves = np.asarray(movements, dtype=float)
    diffs = [abs(b - a) for a, b in zip(rates, rates[1:], strict=False) if a is not None and b is not None]
    return WindowFeatures(
        sample_count=len(moves),
        heart_rate_count=len(available),
        mean_heart_rate=float(available.mean()) if len(available) else None,
        heart_rate_std_dev=float(available.std()) if len(available) >= 2 else 0.0,  # noqa: PLR2004
        heart_rate_mean_abs_diff=float(np.mean(diffs)) if diffs else 0.0,
        mean_movement=float(moves.mean()),
        fidget_count=_rising_edges(moves, FIDGET_THRESHOLD),
        high_movement_share=float((moves > HIGH_MOVEMENT_THRESHOLD).mean()),
    )


def is_clean(features: WindowFeatures) -> bool:
    coverage = features.heart_rate_count / features.sample_count if features.sample_count else 0.0
    return features.high_movement_share < MAX_HIGH_MOVEMENT_SHARE and coverage >= MIN_HEART_RATE_COVERAGE


def sliding_windows(seconds: Sequence[int]) -> Iterator[tuple[int, slice]]:
    """Yields (end_second, sample_slice) exactly like FeatureWindowStream on the watch.

    The first window comes one step after the first sample; each covers samples in (end - 180, end].
    ``seconds`` are the sample times in whole seconds, sorted.
    """
    if not len(seconds):
        return
    due = seconds[0] + STEP_SECONDS
    first = 0
    for index, second in enumerate(seconds):
        while seconds[first] <= second - WINDOW_SECONDS:
            first += 1
        if second >= due:
            yield second, slice(first, index + 1)
            due += STEP_SECONDS


def _rising_edges(values: np.ndarray, threshold: float) -> int:
    above = values > threshold
    previous = np.concatenate(([False], above[:-1]))
    return int((above & ~previous).sum())


def _value_or_none(value: float | None) -> float | None:
    if value is None or np.isnan(value):
        return None
    return float(value)
