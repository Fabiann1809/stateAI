"""Second-level heart rate and movement generated from the latent minutes."""

from dataclasses import dataclass

import numpy as np

from stateai_ml.synthetic.latent import LatentMinutes

SECONDS_PER_MINUTE = 60
MODE_HEART_RATE_OFFSET = np.array([1.5, 6.0, 15.0])
MODE_MOVEMENT = np.array([0.04, 0.08, 0.12])
MODE_FIDGET_RATE = np.array([0.002, 0.004, 0.008])
RESTLESS_FIDGET_RATE = 0.05
FIDGET_MAGNITUDE = (2.0, 3.0)
BEAT_NOISE = 1.2
DRIFT_SIGMA = 1.5
DRIFT_TIME_CONSTANT_SECONDS = 600.0
MISSING_HEART_RATE = 0.05
WALKING_MINUTE_CHANCE = 0.01
WALKING_MOVEMENT = (1.3, 0.2)
WALKING_HEART_RATE = 12.0


@dataclass(frozen=True)
class SessionSignal:
    """One value per second; heart rate is NaN when the sensor gave no value."""

    heart_rate: np.ndarray
    movement: np.ndarray
    walking_minutes: np.ndarray
    final_drift: float


def generate_signal(
    latent: LatentMinutes, resting: float, rng: np.random.Generator, drift_start: float | None = None
) -> SessionSignal:
    """``drift_start`` continues the slow drift of a previous call (minute-by-minute simulation)."""
    seconds = len(latent.modes) * SECONDS_PER_MINUTE
    modes = np.repeat(latent.modes, SECONDS_PER_MINUTE)
    restless = np.repeat(latent.restless, SECONDS_PER_MINUTE)
    walking_minutes = rng.random(len(latent.modes)) < WALKING_MINUTE_CHANCE
    walking = np.repeat(walking_minutes, SECONDS_PER_MINUTE)

    drift = _slow_drift(seconds, rng, drift_start)
    heart_rate = resting + MODE_HEART_RATE_OFFSET[modes] + drift
    heart_rate += rng.normal(0.0, BEAT_NOISE, seconds) + walking * WALKING_HEART_RATE
    heart_rate[rng.random(seconds) < MISSING_HEART_RATE] = np.nan

    movement = MODE_MOVEMENT[modes] * rng.lognormal(0.0, 0.3, seconds)
    fidget_rate = MODE_FIDGET_RATE[modes] + restless * RESTLESS_FIDGET_RATE
    fidgets = rng.random(seconds) < fidget_rate
    movement[fidgets] = rng.uniform(*FIDGET_MAGNITUDE, fidgets.sum())
    movement[walking] = rng.normal(*WALKING_MOVEMENT, walking.sum()).clip(min=0.8)
    return SessionSignal(heart_rate, movement, walking_minutes, float(drift[-1]))


def _slow_drift(seconds: int, rng: np.random.Generator, start: float | None) -> np.ndarray:
    """Ornstein-Uhlenbeck drift: slow heart rate wandering unrelated to the mode."""
    decay = np.exp(-1.0 / DRIFT_TIME_CONSTANT_SECONDS)
    noise_scale = DRIFT_SIGMA * np.sqrt(1 - decay**2)
    drift = np.empty(seconds)
    value = rng.normal(0.0, DRIFT_SIGMA) if start is None else start
    for second in range(seconds):
        value = value * decay + rng.normal(0.0, noise_scale)
        drift[second] = value
    return drift
