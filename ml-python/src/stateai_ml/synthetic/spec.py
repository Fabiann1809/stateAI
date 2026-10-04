"""Description of a synthetic user."""

from dataclasses import dataclass, field


@dataclass(frozen=True)
class RhythmSpec:
    """Focus rhythm of the user. ``period_minutes`` None means no rhythm.

    With ``drift_minutes`` > 0 the period changes every day (uniformly within +/- drift), which makes
    an irregular cycle.
    """

    period_minutes: float | None
    drift_minutes: float = 0.0
    strength: float = 1.2


@dataclass(frozen=True)
class ActivitySpec:
    """A custom activity and how often the user picks it, relative to the others."""

    name: str
    category: str
    weight: float


@dataclass(frozen=True)
class UserSpec:
    """Everything needed to generate one reproducible user."""

    name: str
    rhythm: RhythmSpec
    days: int = 21
    seed: int = 1
    resting_heart_rate: float = 65.0
    sessions_per_day: int = 4
    activities: tuple[ActivitySpec, ...] = field(
        default=(
            ActivitySpec("tesis", "DEEP_WORK", 6.0),
            ActivitySpec("bases de datos", "STUDY", 3.0),
            ActivitySpec("novela", "READING", 1.0),
            ActivitySpec("idiomas", "STUDY", 0.3),
        )
    )
