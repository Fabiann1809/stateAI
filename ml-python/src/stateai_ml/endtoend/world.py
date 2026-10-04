"""Closed-loop version of the synthetic user: the latent state reacts to work and pauses.

The offline generator plays whole sessions; here the simulation advances one minute at a time so a
policy can pause based on what it has seen. Modes and restlessness use the same transition rules as
``synthetic.latent`` and the signal the same physiology, but fatigue comes from accumulated effort
instead of time in session:

- every work minute adds one minute of effort, and a strained minute adds ``STRAIN_EXTRA_EFFORT`` more;
- every pause minute removes ``RECOVERY_PER_PAUSE_MINUTE`` minutes of effort;
- resuming after a pause redraws the mode, so a pause in the middle of calm focus can break it.

These recovery rules are modeling choices, not physiology. They decide how much pausing pays off, so
any comparison built on them shows how the policies behave in this world, not in people.
"""

from dataclasses import dataclass

import numpy as np

from stateai_ml.synthetic.latent import CALM, STRAINED, LatentMinutes, draw_mode, next_restless, rhythm_value
from stateai_ml.synthetic.physiology import generate_signal

FATIGUE_SCALE_MINUTES = 90.0
STRAIN_EXTRA_EFFORT = 1.0
RECOVERY_PER_PAUSE_MINUTE = 8.0
STICKINESS = 0.85


@dataclass(frozen=True)
class Rhythm:
    period: float | None
    phase: float
    strength: float


@dataclass(frozen=True)
class WorkMinute:
    """Ground truth and the 60 one-second samples the watch would record."""

    mode: int
    restless: bool
    heart_rate: np.ndarray
    movement: np.ndarray


class World:
    def __init__(self, resting_heart_rate: float, rhythm: Rhythm, rng: np.random.Generator) -> None:
        self._resting = resting_heart_rate
        self._rhythm = rhythm
        self._rng = rng
        self._effort = 0.0
        self._mode: int | None = None
        self._restless = False
        self._drift: float | None = None

    @property
    def fatigue(self) -> float:
        return min(1.0, self._effort / FATIGUE_SCALE_MINUTES)

    def calibration_minute(self) -> WorkMinute:
        """A still, calm minute, like the watch's first calibration."""
        return self._signal(CALM, restless=False)

    def start_period(self) -> None:
        """A new work period after a long gap: rested, mode drawn fresh."""
        self._effort = 0.0
        self._mode = None
        self._restless = False

    def work(self, minute_of_day: int) -> WorkMinute:
        rhythm = rhythm_value(minute_of_day, self._rhythm.period, self._rhythm.phase) * self._rhythm.strength
        if self._mode is None or self._rng.random() > STICKINESS:
            self._mode = draw_mode(rhythm, self.fatigue, self._rng)
        self._restless = next_restless(self._restless, self.fatigue, self._rng)
        self._effort += 1.0 + (STRAIN_EXTRA_EFFORT if self._mode == STRAINED else 0.0)
        return self._signal(self._mode, self._restless)

    def pause(self) -> None:
        """One pause minute: recovers effort; the next work minute redraws the mode."""
        self._effort = max(0.0, self._effort - RECOVERY_PER_PAUSE_MINUTE)
        self._mode = None
        self._restless = False

    def _signal(self, mode: int, restless: bool) -> WorkMinute:
        latent = LatentMinutes(np.array([mode]), np.array([restless]))
        signal = generate_signal(latent, self._resting, self._rng, self._drift)
        self._drift = signal.final_drift
        return WorkMinute(mode, restless, signal.heart_rate, signal.movement)
