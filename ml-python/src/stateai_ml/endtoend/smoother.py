"""Python port of the watch's StateSmoother: a new value must repeat before it replaces the current one."""

REQUIRED_REPEATS = 2


class Debounced:
    def __init__(self, required_repeats: int = REQUIRED_REPEATS) -> None:
        self._required = required_repeats
        self._stable = None
        self._candidate = None
        self._count = 0

    def update(self, value):
        if self._stable is None or value == self._stable:
            self._stable = value
            self._candidate, self._count = None, 0
        elif value == self._candidate:
            self._count += 1
        else:
            self._candidate, self._count = value, 1
        if self._count >= self._required:
            self._stable = self._candidate
            self._candidate, self._count = None, 0
        return self._stable


class StateSmoother:
    def __init__(self) -> None:
        self._level = Debounced()
        self._restless = Debounced()

    def update(self, level: str, restless: bool) -> tuple[str, bool]:
        return self._level.update(level), self._restless.update(restless)
