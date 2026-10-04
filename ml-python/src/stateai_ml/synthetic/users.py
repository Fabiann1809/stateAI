"""The synthetic users of the evaluation dataset."""

from stateai_ml.synthetic.spec import RhythmSpec, UserSpec

USERS: dict[str, UserSpec] = {
    "cycle60": UserSpec("cycle60", RhythmSpec(60.0), seed=60, resting_heart_rate=63.0),
    "cycle90": UserSpec("cycle90", RhythmSpec(90.0), seed=90, resting_heart_rate=66.0),
    "cycle110": UserSpec("cycle110", RhythmSpec(110.0), seed=110, resting_heart_rate=68.0),
}
