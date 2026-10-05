"""Exports what the watch would record for the cycle detection report (T-8.4).

``python -m stateai_ml.cycle_traces`` generates the five synthetic users of the evaluation (cycles of
60, 90 and 110 minutes, no cycle, and an irregular cycle) with three seeds each, and turns every
session into the per-minute level trace the watch stores: 3-minute windows, the rule engine against
the calibrated baseline, and the watch's smoothing; unusable windows are ``-``. It also stores the
"ideal" trace, the generator's true latent level of each window, as if classification were perfect.
The Kotlin test ``CycleDetectionReportTest`` runs the app's real cycle detector on both.

Writes ``shared/cycles/users.csv`` (the true rhythm of each user) and ``shared/cycles/traces.csv``.
"""

import csv
from dataclasses import replace
from pathlib import Path

import numpy as np

from stateai_ml.dataset import CALIBRATION_SECONDS, MAX_RESTING_MOVEMENT
from stateai_ml.endtoend.smoother import Debounced
from stateai_ml.features import extract, is_clean, sliding_windows
from stateai_ml.rules import classify
from stateai_ml.synthetic.generator import generate_user
from stateai_ml.synthetic.spec import UserSpec
from stateai_ml.synthetic.users import USERS

OUT = Path(__file__).resolve().parents[3] / "shared" / "cycles"
SEED_OFFSETS = (0, 1000, 2000)
SYMBOLS = {"LOW": "L", "MEDIUM": "M", "HIGH": "H"}
UNUSABLE = "-"
SECONDS_PER_MINUTE = 60


def users() -> list[UserSpec]:
    return [
        replace(spec, name=f"{spec.name}-s{spec.seed + offset}", seed=spec.seed + offset)
        for spec in USERS.values()
        for offset in SEED_OFFSETS
    ]


def traces(spec: UserSpec) -> list[dict]:
    user = generate_user(spec)
    baseline = calibrated_baseline(user.samples)
    rows = []
    for session in user.sessions.itertuples():
        samples = user.samples[user.samples["session_id"] == session.session_id]
        levels = user.minutes[user.minutes["session_id"] == session.session_id]["level"].tolist()
        length = len(samples) // SECONDS_PER_MINUTE
        cues = [0] + ([session.planned_minutes] if length >= session.planned_minutes else [])
        rows.append(
            {
                "user": spec.name,
                "start": session.start,
                "minutes": length,
                "planned_minutes": session.planned_minutes,
                "category": session.category,
                "cues": ";".join(str(cue) for cue in cues),
                "trace": trace(samples, baseline, session.category),
                "ideal": ideal_trace(levels),
            }
        )
    return rows


def calibrated_baseline(samples) -> float:
    """Resting heart rate from the first two still minutes, like the watch's first calibration."""
    first = samples[samples["session_id"] == samples["session_id"].iloc[0]]
    still = first[(first["second"] < CALIBRATION_SECONDS) & (first["movement"] <= MAX_RESTING_MOVEMENT)]
    return float(still["heart_rate"].mean())


def trace(samples, baseline: float, category: str) -> str:
    seconds = samples["second"].to_numpy()
    heart_rates = samples["heart_rate"].to_numpy()
    movements = samples["movement"].to_numpy()
    smoother = Debounced()
    symbols = []
    for _, window in sliding_windows(seconds):
        rates = [None if np.isnan(rate) else float(rate) for rate in heart_rates[window]]
        features = extract(rates, list(movements[window]))
        level = classify(features, baseline, category) if is_clean(features) else None
        symbols.append(SYMBOLS[smoother.update(level)] if level else UNUSABLE)
    return "".join(symbols)


def ideal_trace(levels: list[str]) -> str:
    """True latent level of every minute, one symbol per minute like the watch's trace."""
    return "".join(SYMBOLS[level] for level in levels)


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    specs = users()
    with (OUT / "users.csv").open("w", newline="", encoding="utf-8") as file:
        writer = csv.writer(file, lineterminator="\n")
        writer.writerow(["user", "kind", "period_minutes", "drift_minutes", "days"])
        for spec in specs:
            kind = spec.name.split("-s")[0]
            writer.writerow([spec.name, kind, spec.rhythm.period_minutes or "", spec.rhythm.drift_minutes, spec.days])
    rows = [row for spec in specs for row in traces(spec)]
    with (OUT / "traces.csv").open("w", newline="", encoding="utf-8") as file:
        writer = csv.DictWriter(file, fieldnames=list(rows[0]), lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)
    print(f"wrote {len(specs)} users and {len(rows)} sessions to {OUT}")


if __name__ == "__main__":
    main()
