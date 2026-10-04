"""Generates a synthetic user and writes it as reproducible CSV files."""

from dataclasses import dataclass
from pathlib import Path

import numpy as np
import pandas as pd

from stateai_ml.synthetic.latent import MODE_NAMES, simulate_session
from stateai_ml.synthetic.physiology import SECONDS_PER_MINUTE, generate_signal
from stateai_ml.synthetic.routine import PlannedSession, plan_sessions
from stateai_ml.synthetic.spec import UserSpec

ORIGIN = pd.Timestamp("2026-09-01T00:00:00Z")


@dataclass(frozen=True)
class SyntheticUser:
    sessions: pd.DataFrame
    minutes: pd.DataFrame
    samples: pd.DataFrame


def generate_user(spec: UserSpec) -> SyntheticUser:
    """Same spec (including seed) always gives exactly the same data."""
    rng = np.random.default_rng(spec.seed)
    periods = _daily_periods(spec, rng)
    phases = rng.uniform(0, 2 * np.pi, spec.days)
    sessions, minutes, samples = [], [], []
    for planned in plan_sessions(spec, rng):
        latent = simulate_session(
            planned.start_minute_of_day,
            planned.length_minutes,
            periods[planned.day],
            phases[planned.day],
            spec.rhythm.strength,
            rng,
        )
        signal = generate_signal(latent, spec.resting_heart_rate, rng)
        start = ORIGIN + pd.Timedelta(days=planned.day, minutes=planned.start_minute_of_day)
        sessions.append(_session_row(planned, start))
        minutes.append(_minute_rows(planned, start, latent.modes, latent.restless, signal.walking_minutes))
        samples.append(_sample_rows(planned, start, signal.heart_rate, signal.movement))
    return SyntheticUser(pd.DataFrame(sessions), pd.concat(minutes), pd.concat(samples))


def write_user(user: SyntheticUser, directory: Path, name: str) -> None:
    directory.mkdir(parents=True, exist_ok=True)
    user.sessions.to_csv(directory / f"{name}_sessions.csv", index=False)
    user.minutes.to_csv(directory / f"{name}_minutes.csv", index=False)
    user.samples.to_csv(directory / f"{name}_samples.csv", index=False, float_format="%.3f")


def _daily_periods(spec: UserSpec, rng: np.random.Generator) -> list[float | None]:
    period = spec.rhythm.period_minutes
    if period is None:
        return [None] * spec.days
    drift = spec.rhythm.drift_minutes
    return [float(period + rng.uniform(-drift, drift)) for _ in range(spec.days)]


def _session_row(planned: PlannedSession, start: pd.Timestamp) -> dict:
    return {
        "session_id": planned.session_id,
        "activity": planned.activity,
        "category": planned.category,
        "start": start.isoformat(),
        "end": (start + pd.Timedelta(minutes=planned.length_minutes)).isoformat(),
        "planned_minutes": planned.planned_minutes,
    }


def _minute_rows(planned, start, modes, restless, walking) -> pd.DataFrame:
    return pd.DataFrame(
        {
            "session_id": planned.session_id,
            "minute": np.arange(len(modes)),
            "start": [(start + pd.Timedelta(minutes=int(m))).isoformat() for m in range(len(modes))],
            "level": [MODE_NAMES[mode] for mode in modes],
            "restless": restless,
            "walking": walking,
        }
    )


def _sample_rows(planned, start, heart_rate, movement) -> pd.DataFrame:
    seconds = np.arange(len(heart_rate))
    return pd.DataFrame(
        {
            "session_id": planned.session_id,
            "timestamp": (start + pd.to_timedelta(seconds, unit="s")).strftime("%Y-%m-%dT%H:%M:%SZ"),
            "heart_rate": heart_rate,
            "movement": movement,
            "second": seconds,
            "minute": seconds // SECONDS_PER_MINUTE,
        }
    )
