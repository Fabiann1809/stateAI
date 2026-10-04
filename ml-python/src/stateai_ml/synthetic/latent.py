"""Minute-level latent state: the ground truth that physiology is generated from.

Modes are a sticky Markov chain. When the chain jumps, the new mode is drawn from a softmax whose
scores depend on the user's rhythm (pushes towards calm focus at its peaks) and on fatigue (time in
session pushes towards strain). This is deliberately unlike the rule engine, which thresholds heart
rate against a baseline.
"""

from dataclasses import dataclass

import numpy as np

CALM, ENGAGED, STRAINED = 0, 1, 2
MODE_NAMES = ("LOW", "MEDIUM", "HIGH")

STICKINESS = 0.85
FATIGUE_SCALE_MINUTES = 90.0
CALM_BASE, ENGAGED_BASE, STRAINED_BASE = 1.0, 0.8, -0.6
FATIGUE_WEIGHT = 1.0
RESTLESS_ONSET = 0.6
RESTLESS_STICKINESS = 0.8


@dataclass(frozen=True)
class LatentMinutes:
    """Ground truth of one session, one entry per minute."""

    modes: np.ndarray
    restless: np.ndarray


def rhythm_value(minute_of_day: float, period: float | None, phase: float) -> float:
    """Cosine rhythm in [-1, 1]; 0 when the user has no rhythm."""
    if period is None:
        return 0.0
    return float(np.cos(2 * np.pi * minute_of_day / period + phase))


def simulate_session(
    start_minute_of_day: int,
    length_minutes: int,
    period: float | None,
    phase: float,
    strength: float,
    rng: np.random.Generator,
) -> LatentMinutes:
    """Simulates the latent mode and restlessness of every minute of a session."""
    modes = np.empty(length_minutes, dtype=int)
    restless = np.zeros(length_minutes, dtype=bool)
    mode = _draw_mode(rhythm_value(start_minute_of_day, period, phase) * strength, 0.0, rng)
    for minute in range(length_minutes):
        fatigue = min(1.0, minute / FATIGUE_SCALE_MINUTES)
        if rng.random() > STICKINESS:
            rhythm = rhythm_value(start_minute_of_day + minute, period, phase) * strength
            mode = _draw_mode(rhythm, fatigue, rng)
        modes[minute] = mode
        restless[minute] = _next_restless(restless[minute - 1] if minute else False, fatigue, rng)
    return LatentMinutes(modes, restless)


def _draw_mode(rhythm: float, fatigue: float, rng: np.random.Generator) -> int:
    scores = np.array(
        [
            CALM_BASE + rhythm - FATIGUE_WEIGHT * fatigue,
            ENGAGED_BASE,
            STRAINED_BASE - rhythm + FATIGUE_WEIGHT * fatigue,
        ]
    )
    probabilities = np.exp(scores) / np.exp(scores).sum()
    return int(rng.choice(3, p=probabilities))


def _next_restless(previous: bool, fatigue: float, rng: np.random.Generator) -> bool:
    if previous:
        return bool(rng.random() < RESTLESS_STICKINESS)
    return bool(fatigue > RESTLESS_ONSET and rng.random() < (fatigue - RESTLESS_ONSET))
