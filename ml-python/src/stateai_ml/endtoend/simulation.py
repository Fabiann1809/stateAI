"""Runs a policy through simulated days and collects what happened, minute by minute."""

from collections.abc import Callable
from dataclasses import dataclass

import numpy as np
import pandas as pd

from stateai_ml.endtoend.day import DayPlan
from stateai_ml.endtoend.policy import PAUSE_MINUTES, Policy
from stateai_ml.endtoend.world import Rhythm, World
from stateai_ml.synthetic.latent import MODE_NAMES

CALIBRATION_MINUTES = 2


@dataclass(frozen=True)
class Calibration:
    """Resting heart rate measured before the first period, available to the policy."""

    resting_heart_rate: float


def simulate_day(plan: DayPlan, make_policy: Callable[[Calibration], Policy]) -> pd.DataFrame:
    """One row per minute of every work period: ``activity`` is ``work`` or ``pause``."""
    world = World(
        plan.user.resting_heart_rate,
        Rhythm(plan.period, plan.phase, plan.user.rhythm.strength),
        np.random.default_rng(plan.world_seed),
    )
    calibration = np.concatenate([world.calibration_minute().heart_rate for _ in range(CALIBRATION_MINUTES)])
    policy = make_policy(Calibration(float(np.nanmean(calibration))))
    rows = []
    for index, period in enumerate(plan.periods):
        world.start_period()
        policy.start_block(period.category, period.start_minute_of_day)
        pause_left = 0
        for offset in range(period.length_minutes):
            minute_of_day = period.start_minute_of_day + offset
            row = {"day": plan.day, "period": index, "category": period.category, "minute_of_day": minute_of_day}
            if pause_left:
                world.pause()
                pause_left -= 1
                rows.append(row | {"activity": "pause", "level": None, "restless": False})
                if not pause_left:
                    policy.start_block(period.category, minute_of_day + 1)
                continue
            minute = world.work(minute_of_day)
            rows.append(row | {"activity": "work", "level": MODE_NAMES[minute.mode], "restless": minute.restless})
            if policy.wants_pause(minute, minute_of_day):
                pause_left = PAUSE_MINUTES
    return pd.DataFrame(rows)
