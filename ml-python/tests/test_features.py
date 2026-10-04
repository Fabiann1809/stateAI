"""Python features match the shared parity cases and the watch's windowing."""

import json
import math

import pytest

from stateai_ml.features import extract, is_clean, sliding_windows
from stateai_ml.parity import CASES_FILE

CASES = json.loads(CASES_FILE.read_text(encoding="utf-8"))


@pytest.mark.parametrize("case", CASES, ids=[case["name"] for case in CASES])
def test_matches_shared_parity_case(case: dict) -> None:
    actual = extract(case["heart_rates"], case["movements"]).as_dict()

    for key, expected in case["expected"].items():
        if expected is None:
            assert actual[key] is None
        else:
            assert math.isclose(actual[key], expected, rel_tol=1e-9, abs_tol=1e-9), key


def test_windows_follow_the_watch_schedule() -> None:
    windows = list(sliding_windows(list(range(600))))

    assert [end for end, _ in windows] == [60, 120, 180, 240, 300, 360, 420, 480, 540]
    first, last = windows[0][1], windows[-1][1]
    assert first.stop - first.start == 61
    assert last.stop - last.start == 180


def test_walking_window_is_not_clean() -> None:
    walking = next(case for case in CASES if case["name"] == "walking")

    assert not is_clean(extract(walking["heart_rates"], walking["movements"]))
