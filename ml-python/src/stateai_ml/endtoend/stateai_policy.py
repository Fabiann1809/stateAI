"""stateAI as a policy: the watch's estimation and haptic pipeline, with a person who follows cues.

Each minute the policy builds the latest window from the block's samples (up to 3 minutes, like the
watch's window stream), classifies clean windows with the rules against the calibrated baseline,
smooths them and asks the haptic policy for a cue. The target block of the category also suggests a
pause. Cues go through the rate limiter, and the person follows a played cue (a suggested pause or an
overload alert) with probability ``compliance``: the watch suggests, it never forces.
"""

import numpy as np

from stateai_ml.endtoend.haptics import PAUSE_SUGGESTED, HapticPolicy, RateLimiter
from stateai_ml.endtoend.smoother import StateSmoother
from stateai_ml.endtoend.world import WorkMinute
from stateai_ml.features import WINDOW_SECONDS, extract, is_clean
from stateai_ml.rules import classify
from stateai_ml.synthetic.routine import PLANNED_MINUTES

RESTLESS_MIN_FIDGETS = 5
RESTLESS_MIN_ELAPSED_MINUTES = 15
DEFAULT_COMPLIANCE = 0.8


class StateAIPolicy:
    def __init__(self, resting_heart_rate: float, seed: int, compliance: float = DEFAULT_COMPLIANCE) -> None:
        self.name = "stateai"
        self._resting = resting_heart_rate
        self._compliance = compliance
        self._rng = np.random.default_rng(seed)
        self._limiter = RateLimiter()
        self.start_block("OTHER", 0)

    def start_block(self, category: str, minute_of_day: int) -> None:
        self._category = category
        self._elapsed = 0
        self._heart_rate: list[np.ndarray] = []
        self._movement: list[np.ndarray] = []
        self._smoother = StateSmoother()
        self._policy = HapticPolicy()
        self._block_cue_played = False

    def wants_pause(self, minute: WorkMinute, minute_of_day: int) -> bool:
        self._elapsed += 1
        self._heart_rate.append(minute.heart_rate)
        self._movement.append(minute.movement)
        cues = [self._state_cue(), self._block_cue()]
        played = [cue for cue in cues if cue and self._limiter.try_acquire(minute_of_day, self._category)]
        return bool(played) and bool(self._rng.random() < self._compliance)

    def _block_cue(self) -> str | None:
        """Fires once when the target block is reached, even if the limiter then mutes it (like the watch)."""
        if self._block_cue_played or self._elapsed < PLANNED_MINUTES[self._category]:
            return None
        self._block_cue_played = True
        return PAUSE_SUGGESTED

    def _state_cue(self) -> str | None:
        heart_rate = np.concatenate(self._heart_rate)[-WINDOW_SECONDS:]
        movement = np.concatenate(self._movement)[-WINDOW_SECONDS:]
        features = extract([None if np.isnan(rate) else float(rate) for rate in heart_rate], movement)
        if not is_clean(features):
            return None
        level = classify(features, self._resting, self._category)
        if level is None:
            return None
        restless = features.fidget_count >= RESTLESS_MIN_FIDGETS and self._elapsed >= RESTLESS_MIN_ELAPSED_MINUTES
        return self._policy.on_estimate(*self._smoother.update(level, restless))
