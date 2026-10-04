"""The synthetic generator is reproducible and puts the intended structure in the data."""

from dataclasses import replace

import numpy as np
import pytest

from stateai_ml.synthetic.generator import generate_user
from stateai_ml.synthetic.latent import CALM, simulate_session
from stateai_ml.synthetic.spec import RhythmSpec, UserSpec
from stateai_ml.synthetic.users import USERS

SMALL = UserSpec("small", RhythmSpec(90.0), days=2, seed=5)


@pytest.fixture(scope="module")
def small_user():
    return generate_user(SMALL)


def test_same_seed_gives_identical_data(small_user) -> None:
    again = generate_user(SMALL)

    assert small_user.samples.equals(again.samples)
    assert small_user.minutes.equals(again.minutes)


def test_different_seeds_give_different_data(small_user) -> None:
    other = generate_user(replace(SMALL, seed=6))

    assert not small_user.samples["heart_rate"].equals(other.samples["heart_rate"])


def test_one_sample_per_second_and_one_truth_row_per_minute(small_user) -> None:
    sessions = small_user.sessions
    total_minutes = sum(
        int((np.datetime64(end[:19]) - np.datetime64(start[:19])) / np.timedelta64(1, "m"))
        for start, end in zip(sessions["start"], sessions["end"], strict=True)
    )

    assert len(small_user.minutes) == total_minutes
    assert len(small_user.samples) == total_minutes * 60


def test_heart_rate_rises_with_the_latent_level(small_user) -> None:
    merged = small_user.samples.merge(small_user.minutes, on=["session_id", "minute"])
    means = merged.groupby("level")["heart_rate"].mean()

    assert means["LOW"] < means["MEDIUM"] < means["HIGH"]


def test_some_heart_rate_values_are_missing(small_user) -> None:
    missing = small_user.samples["heart_rate"].isna().mean()

    assert 0.02 < missing < 0.08


def test_rhythm_peaks_favor_calm_focus() -> None:
    rng = np.random.default_rng(1)
    calm_at_peak = [simulate_session(0, 10, 90.0, 0.0, 3.0, rng).modes[0] == CALM for _ in range(300)]
    calm_at_trough = [simulate_session(45, 10, 90.0, 0.0, 3.0, rng).modes[0] == CALM for _ in range(300)]

    assert np.mean(calm_at_peak) > np.mean(calm_at_trough) + 0.3


def test_dataset_has_users_with_known_cycles() -> None:
    periods = {spec.rhythm.period_minutes for spec in USERS.values()}

    assert {60.0, 90.0, 110.0} <= periods
