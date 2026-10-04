"""Python ports of the watch's HapticPolicy and HapticRateLimiter (SPEC 6.2)."""

from collections import deque

PAUSE_SUGGESTED, OVERLOAD_ALERT = "PAUSE_SUGGESTED", "OVERLOAD_ALERT"
SUSTAINED_WINDOWS = 3
MIN_FOCUS_WINDOWS_BEFORE_PAUSE = 10
MIN_INTERVAL_MINUTES = 5
CAP_WINDOW_MINUTES = 60
MAX_VIBRATIONS_PER_HOUR = {"DEEP_WORK": 2, "STUDY": 3, "READING": 2, "COLLAB": 4, "OTHER": 3}


class HapticPolicy:
    """Which cue a new smoothed estimate deserves; one instance per block."""

    def __init__(self) -> None:
        self._previous: str | None = None
        self._streak = 0
        self._restless_streak = 0
        self._overload_alerted = False
        self._restless_alerted = False

    def on_estimate(self, level: str, restless: bool) -> str | None:
        focus_streak_before = self._streak if self._previous == "LOW" else 0
        self._streak = self._streak + 1 if level == self._previous else 1
        self._previous = level
        if level != "HIGH":
            self._overload_alerted = False
        self._restless_streak = self._restless_streak + 1 if restless else 0
        if not restless:
            self._restless_alerted = False

        if level == "HIGH" and self._streak >= SUSTAINED_WINDOWS and not self._overload_alerted:
            self._overload_alerted = True
            return OVERLOAD_ALERT
        if self._restless_streak >= SUSTAINED_WINDOWS and not self._restless_alerted:
            self._restless_alerted = True
            return PAUSE_SUGGESTED
        if level != "LOW" and focus_streak_before >= MIN_FOCUS_WINDOWS_BEFORE_PAUSE:
            return PAUSE_SUGGESTED
        return None


class RateLimiter:
    """At most one vibration every 5 minutes and the category's cap per hour; shared by the whole day."""

    def __init__(self) -> None:
        self._played: deque[int] = deque()

    def try_acquire(self, minute: int, category: str) -> bool:
        while self._played and self._played[0] < minute - CAP_WINDOW_MINUTES:
            self._played.popleft()
        spaced = not self._played or minute >= self._played[-1] + MIN_INTERVAL_MINUTES
        allowed = spaced and len(self._played) < MAX_VIBRATIONS_PER_HOUR[category]
        if allowed:
            self._played.append(minute)
        return allowed
