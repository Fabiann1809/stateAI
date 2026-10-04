"""The synthetic dataset has activities with many and few sessions, enough to test blending."""

import pandas as pd
import pytest

from stateai_ml.profiles import blend, own_weight
from stateai_ml.synthetic.generator import generate_user
from stateai_ml.synthetic.routine import PLANNED_MINUTES
from stateai_ml.synthetic.users import USERS

VALID_SESSION_MINUTES = 10


@pytest.fixture(scope="module")
def sessions() -> pd.DataFrame:
    frame = generate_user(USERS["cycle90"]).sessions
    frame["minutes"] = (pd.to_datetime(frame["end"]) - pd.to_datetime(frame["start"])).dt.total_seconds() / 60
    return frame[frame["minutes"] >= VALID_SESSION_MINUTES]


def test_some_activities_have_many_sessions_and_some_few(sessions) -> None:
    counts = sessions["activity"].value_counts()

    assert counts["tesis"] >= 30
    assert counts.get("idiomas", 0) <= 5


def test_blending_follows_n_over_n_plus_k() -> None:
    assert own_weight(0) == 0.0
    assert own_weight(5) == 0.5
    assert own_weight(20) == 0.8


def test_many_sessions_reach_the_activity_value_and_few_stay_near_the_category(sessions) -> None:
    blended = {}
    for activity, group in sessions.groupby("activity"):
        category_block = PLANNED_MINUTES[group["category"].iloc[0]]
        blended[activity] = (
            category_block,
            group["minutes"].mean(),
            blend(category_block, group["minutes"].mean(), len(group)),
        )

    category, learned, value = blended["tesis"]
    assert abs(value - learned) < abs(category - learned) * 0.2
    category, learned, value = blended["idiomas"]
    assert abs(value - category) < abs(learned - category) * 0.6
