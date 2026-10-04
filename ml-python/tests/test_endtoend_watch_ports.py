"""The Python ports behave like the watch's StateSmoother, HapticPolicy and HapticRateLimiter."""

from stateai_ml.endtoend.haptics import OVERLOAD_ALERT, PAUSE_SUGGESTED, HapticPolicy, RateLimiter
from stateai_ml.endtoend.smoother import Debounced


def test_smoother_accepts_the_first_value_and_needs_two_repeats_to_change() -> None:
    debounced = Debounced()

    assert [debounced.update(value) for value in ["LOW", "HIGH", "LOW", "HIGH", "HIGH"]] == [
        "LOW",
        "LOW",
        "LOW",
        "LOW",
        "HIGH",
    ]


def test_sustained_overload_alerts_once_per_episode() -> None:
    policy = HapticPolicy()

    events = [policy.on_estimate("HIGH", False) for _ in range(5)]

    assert events == [None, None, OVERLOAD_ALERT, None, None]


def test_sustained_restlessness_suggests_a_pause() -> None:
    policy = HapticPolicy()

    assert [policy.on_estimate("MEDIUM", True) for _ in range(3)] == [None, None, PAUSE_SUGGESTED]


def test_leaving_a_long_focus_stretch_suggests_a_pause_but_a_short_one_does_not() -> None:
    long_focus, short_focus = HapticPolicy(), HapticPolicy()
    for _ in range(10):
        long_focus.on_estimate("LOW", False)
    for _ in range(9):
        short_focus.on_estimate("LOW", False)

    assert long_focus.on_estimate("MEDIUM", False) == PAUSE_SUGGESTED
    assert short_focus.on_estimate("MEDIUM", False) is None


def test_rate_limiter_spaces_vibrations_and_caps_them_per_hour() -> None:
    limiter = RateLimiter()

    allowed = [minute for minute in range(0, 120) if limiter.try_acquire(minute, "DEEP_WORK")]

    assert allowed == [0, 5, 61, 66]
