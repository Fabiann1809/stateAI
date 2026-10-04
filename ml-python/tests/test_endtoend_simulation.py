"""Simulated days are reproducible and the fixed timer pauses on schedule."""

import pandas as pd

from stateai_ml.endtoend.day import PERIOD_MINUTES, PERIODS_PER_DAY, plan_day
from stateai_ml.endtoend.metrics import day_metrics
from stateai_ml.endtoend.policy import PAUSE_MINUTES, FixedTimer
from stateai_ml.endtoend.runner import POLICIES, run
from stateai_ml.endtoend.simulation import simulate_day
from stateai_ml.synthetic.users import USERS

USER = USERS["cycle90"]


def test_the_same_day_is_planned_identically() -> None:
    assert plan_day(USER, 3) == plan_day(USER, 3)
    assert plan_day(USER, 3) != plan_day(USER, 4)


def test_the_same_day_and_policy_give_the_same_minutes() -> None:
    first = simulate_day(plan_day(USER, 2), lambda calibration: FixedTimer())
    second = simulate_day(plan_day(USER, 2), lambda calibration: FixedTimer())

    pd.testing.assert_frame_equal(first, second)


def test_fixed_timer_pauses_after_every_block() -> None:
    minutes = simulate_day(plan_day(USER, 0), lambda calibration: FixedTimer(work_minutes=25))
    metrics = day_metrics(minutes)

    pauses_per_period = PERIOD_MINUTES // (25 + PAUSE_MINUTES)
    assert len(minutes) == PERIOD_MINUTES * PERIODS_PER_DAY
    assert metrics["pauses"] == pauses_per_period * PERIODS_PER_DAY
    assert metrics["pause_minutes"] == metrics["pauses"] * PAUSE_MINUTES


def test_day_metrics_count_ground_truth_levels_during_work() -> None:
    minutes = pd.DataFrame(
        {
            "activity": ["work", "work", "work", "pause", "pause", "work"],
            "level": ["LOW", "HIGH", "LOW", None, None, "MEDIUM"],
            "restless": [False, True, False, False, False, False],
        }
    )

    assert day_metrics(minutes) == {
        "work_minutes": 4,
        "focus_minutes": 2,
        "overload_minutes": 1,
        "restless_minutes": 1,
        "pauses": 1,
        "pause_minutes": 2,
    }


def test_runner_gives_one_row_per_user_day_and_policy() -> None:
    per_day = run([USER, USERS["no_cycle"]], days=2, policies=POLICIES)

    assert len(per_day) == 2 * 2 * len(POLICIES)
