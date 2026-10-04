"""The plan of one simulated day: when the work periods are and which category each one is.

The plan depends only on the user and the day, so every policy lives exactly the same day.
"""

from dataclasses import dataclass

import numpy as np

from stateai_ml.synthetic.routine import SESSION_BASE_STARTS
from stateai_ml.synthetic.spec import UserSpec

PERIOD_MINUTES = 90
PERIODS_PER_DAY = 4


@dataclass(frozen=True)
class WorkPeriod:
    start_minute_of_day: int
    length_minutes: int
    category: str


@dataclass(frozen=True)
class DayPlan:
    user: UserSpec
    day: int
    period: float | None
    phase: float
    periods: tuple[WorkPeriod, ...]

    @property
    def world_seed(self) -> int:
        return self.user.seed * 1000 + self.day


def plan_day(user: UserSpec, day: int) -> DayPlan:
    rng = np.random.default_rng([user.seed, day])
    weights = np.array([activity.weight for activity in user.activities])
    rhythm = user.rhythm
    period = None
    if rhythm.period_minutes is not None:
        period = float(rhythm.period_minutes + rng.uniform(-rhythm.drift_minutes, rhythm.drift_minutes))
    phase = float(rng.uniform(0, 2 * np.pi))
    periods = tuple(
        WorkPeriod(
            start_minute_of_day=start,
            length_minutes=PERIOD_MINUTES,
            category=user.activities[rng.choice(len(weights), p=weights / weights.sum())].category,
        )
        for start in SESSION_BASE_STARTS[:PERIODS_PER_DAY]
    )
    return DayPlan(user, day, period, phase, periods)
