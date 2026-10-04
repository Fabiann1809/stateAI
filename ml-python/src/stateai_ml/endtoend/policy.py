"""What a policy sees and decides during a simulated work period."""

from typing import Protocol

from stateai_ml.endtoend.world import WorkMinute

PAUSE_MINUTES = 5


class Policy(Protocol):
    name: str

    def start_block(self, category: str, minute_of_day: int) -> None:
        """A block starts: at the beginning of a period and after every pause."""

    def wants_pause(self, minute: WorkMinute, minute_of_day: int) -> bool:
        """Called after every work minute with the samples the watch recorded in it."""


class FixedTimer:
    """Classic fixed timer: ``work_minutes`` of work, then a pause, whatever the person's state."""

    def __init__(self, work_minutes: int = 25) -> None:
        self.name = f"fixed_timer_{work_minutes}"
        self._work_minutes = work_minutes
        self._worked = 0

    def start_block(self, category: str, minute_of_day: int) -> None:
        self._worked = 0

    def wants_pause(self, minute: WorkMinute, minute_of_day: int) -> bool:
        self._worked += 1
        return self._worked >= self._work_minutes
