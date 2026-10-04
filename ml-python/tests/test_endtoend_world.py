"""The closed-loop world: effort builds up with work, faster when strained, and pauses recover it."""

import numpy as np

from stateai_ml.endtoend.world import FATIGUE_SCALE_MINUTES, RECOVERY_PER_PAUSE_MINUTE, Rhythm, World
from stateai_ml.synthetic.latent import STRAINED


def world(seed: int = 1) -> World:
    return World(65.0, Rhythm(None, 0.0, 1.2), np.random.default_rng(seed))


def test_work_builds_fatigue_and_pauses_recover_it() -> None:
    simulated = world()
    simulated.start_period()
    minutes = [simulated.work(600 + minute) for minute in range(30)]
    strained = sum(minute.mode == STRAINED for minute in minutes)

    assert simulated.fatigue == (30 + strained) / FATIGUE_SCALE_MINUTES
    simulated.pause()
    assert simulated.fatigue == (30 + strained - RECOVERY_PER_PAUSE_MINUTE) / FATIGUE_SCALE_MINUTES


def test_a_minute_has_one_sample_per_second() -> None:
    minute = world().work(600)

    assert len(minute.heart_rate) == 60
    assert len(minute.movement) == 60


def test_calibration_is_calm_and_near_the_resting_rate() -> None:
    rates = np.concatenate([world(seed).calibration_minute().heart_rate for seed in range(20)])

    assert abs(np.nanmean(rates) - (65.0 + 1.5)) < 1.0
