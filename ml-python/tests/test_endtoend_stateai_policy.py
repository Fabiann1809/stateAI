"""stateAI as a policy: it pauses on its cues, only as often as the limiter allows and the person agrees."""

import numpy as np

from stateai_ml.endtoend.stateai_policy import StateAIPolicy
from stateai_ml.endtoend.world import WorkMinute
from stateai_ml.synthetic.latent import CALM, STRAINED
from stateai_ml.synthetic.routine import PLANNED_MINUTES

RESTING = 65.0


def minute(heart_rate: float, mode: int) -> WorkMinute:
    return WorkMinute(mode, False, np.full(60, heart_rate), np.full(60, 0.05))


def minutes_until_pause(policy: StateAIPolicy, work: WorkMinute, category: str, limit: int = 120) -> int | None:
    policy.start_block(category, 600)
    for elapsed in range(1, limit + 1):
        if policy.wants_pause(work, 600 + elapsed):
            return elapsed
    return None


def test_calm_work_pauses_when_the_target_block_is_reached() -> None:
    policy = StateAIPolicy(RESTING, seed=1, compliance=1.0)

    assert minutes_until_pause(policy, minute(RESTING + 1, CALM), "STUDY") == PLANNED_MINUTES["STUDY"]


def test_sustained_overload_pauses_early() -> None:
    policy = StateAIPolicy(RESTING, seed=1, compliance=1.0)

    assert minutes_until_pause(policy, minute(RESTING + 20, STRAINED), "STUDY") == 3


def test_cues_are_only_suggestions() -> None:
    policy = StateAIPolicy(RESTING, seed=1, compliance=0.0)

    assert minutes_until_pause(policy, minute(RESTING + 20, STRAINED), "STUDY") is None
