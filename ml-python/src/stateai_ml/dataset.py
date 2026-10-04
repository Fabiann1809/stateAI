"""Builds the labeled window dataset from synthetic users, the way the watch would see them."""

from pathlib import Path

import numpy as np
import pandas as pd

from stateai_ml.features import extract, is_clean, sliding_windows

CALIBRATION_SECONDS = 120
MAX_RESTING_MOVEMENT = 1.0
SECONDS_PER_MINUTE = 60


def load_user(directory: Path, name: str) -> tuple[pd.DataFrame, pd.DataFrame, pd.DataFrame]:
    sessions = pd.read_csv(directory / f"{name}_sessions.csv")
    minutes = pd.read_csv(directory / f"{name}_minutes.csv")
    samples = pd.read_csv(directory / f"{name}_samples.csv")
    return sessions, minutes, samples


def calibrate_baseline(samples: pd.DataFrame) -> float:
    """Resting heart rate from the first two still minutes, like the watch's first calibration."""
    first_session = samples[samples["session_id"] == samples["session_id"].iloc[0]]
    still = first_session[
        (first_session["second"] < CALIBRATION_SECONDS) & (first_session["movement"] <= MAX_RESTING_MOVEMENT)
    ]
    return float(still["heart_rate"].mean())


def build_windows(directory: Path, name: str) -> pd.DataFrame:
    """One row per window: features, the latent-majority label, and session context."""
    sessions, minutes, samples = load_user(directory, name)
    baseline = calibrate_baseline(samples)
    categories = sessions.set_index("session_id")["category"]
    truth = minutes.set_index(["session_id", "minute"])
    rows = []
    for session_id, session_samples in samples.groupby("session_id", sort=False):
        seconds = session_samples["second"].to_numpy()
        heart_rates = session_samples["heart_rate"].to_numpy()
        movements = session_samples["movement"].to_numpy()
        for end, window in sliding_windows(seconds):
            features = extract(list(heart_rates[window]), list(movements[window]))
            covered = range(int(seconds[window.start]) // SECONDS_PER_MINUTE, int(end) // SECONDS_PER_MINUTE)
            labels = truth.loc[[(session_id, minute) for minute in covered]]
            rows.append(
                {
                    "user": name,
                    "session_id": session_id,
                    "category": categories[session_id],
                    "end_second": int(end),
                    "baseline": baseline,
                    "clean": is_clean(features),
                    "label": labels["level"].mode().iloc[0],
                    **features.as_dict(),
                }
            )
    frame = pd.DataFrame(rows)
    frame["heart_rate_delta"] = frame["mean_heart_rate"] - frame["baseline"]
    frame["elapsed_minutes"] = frame["end_second"] / SECONDS_PER_MINUTE
    return frame


def build_dataset(directory: Path, names: list[str]) -> pd.DataFrame:
    return pd.concat([build_windows(directory, name) for name in names], ignore_index=True)


def clean_windows(frame: pd.DataFrame) -> pd.DataFrame:
    """Windows the watch would classify: clean and with a heart rate."""
    return frame[frame["clean"] & frame["mean_heart_rate"].notna() & np.isfinite(frame["heart_rate_delta"])]
