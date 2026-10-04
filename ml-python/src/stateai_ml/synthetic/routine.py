"""Daily schedule: when sessions happen, how long they last and which activity they are."""

from dataclasses import dataclass

import numpy as np

from stateai_ml.synthetic.spec import UserSpec

SESSION_BASE_STARTS = (510, 660, 870, 1020, 1200)
START_JITTER_MINUTES = 20
MIN_LENGTH_MINUTES = 20
MAX_LENGTH_MINUTES = 90
LENGTH_SPREAD_MINUTES = 6.0
PLANNED_MINUTES = {"DEEP_WORK": 75, "STUDY": 40, "READING": 40, "COLLAB": 45, "OTHER": 45}


@dataclass(frozen=True)
class PlannedSession:
    session_id: str
    day: int
    start_minute_of_day: int
    length_minutes: int
    activity: str
    category: str

    @property
    def planned_minutes(self) -> int:
        return PLANNED_MINUTES[self.category]


def plan_sessions(spec: UserSpec, rng: np.random.Generator) -> list[PlannedSession]:
    """Sessions at jittered times every day, with gaps between them."""
    weights = np.array([activity.weight for activity in spec.activities])
    probabilities = weights / weights.sum()
    sessions = []
    for day in range(spec.days):
        for index, base in enumerate(SESSION_BASE_STARTS[: spec.sessions_per_day]):
            activity = spec.activities[rng.choice(len(spec.activities), p=probabilities)]
            sessions.append(
                PlannedSession(
                    session_id=f"{spec.name}-d{day:02d}-s{index}",
                    day=day,
                    start_minute_of_day=int(base + rng.integers(-START_JITTER_MINUTES, START_JITTER_MINUTES + 1)),
                    length_minutes=_length(activity.typical_minutes, rng),
                    activity=activity.name,
                    category=activity.category,
                )
            )
    return sessions


def _length(typical_minutes: float, rng: np.random.Generator) -> int:
    length = rng.normal(typical_minutes, LENGTH_SPREAD_MINUTES)
    return int(np.clip(round(length), MIN_LENGTH_MINUTES, MAX_LENGTH_MINUTES))
