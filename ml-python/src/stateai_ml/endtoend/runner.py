"""Runs every policy over the same simulated days of every synthetic user."""

from collections.abc import Callable

import pandas as pd

from stateai_ml.endtoend.day import plan_day
from stateai_ml.endtoend.metrics import day_metrics
from stateai_ml.endtoend.policy import FixedTimer, Policy
from stateai_ml.endtoend.simulation import DayStart, simulate_day
from stateai_ml.endtoend.stateai_policy import StateAIPolicy
from stateai_ml.synthetic.spec import UserSpec

PolicyFactory = Callable[[DayStart], Policy]

POLICIES: dict[str, PolicyFactory] = {
    "fixed_timer": lambda start: FixedTimer(),
    "stateai": lambda start: StateAIPolicy(start.resting_heart_rate, start.seed),
}


def run(users: list[UserSpec], days: int, policies: dict[str, PolicyFactory]) -> pd.DataFrame:
    """One row per user, day and policy with the day metrics."""
    rows = []
    for user in users:
        for day in range(days):
            plan = plan_day(user, day)
            for name, factory in policies.items():
                metrics = day_metrics(simulate_day(plan, factory))
                rows.append({"user": user.name, "day": day, "policy": name} | metrics)
    return pd.DataFrame(rows)
