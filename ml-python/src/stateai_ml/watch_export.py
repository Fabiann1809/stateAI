"""Reads the segment summaries exported by the watch app (see SegmentCsv in core-domain)."""

from pathlib import Path

import pandas as pd

TIME_COLUMNS = ["start", "end"]
SECONDS_COLUMNS = ["planned_s", "low_s", "medium_s", "high_s", "unknown_s", "restless_s"]


def load_segments(path: Path | str) -> pd.DataFrame:
    """Loads segments with parsed UTC times and a derived duration in seconds."""
    frame = pd.read_csv(path, keep_default_na=False, na_values={"feedback": [""]})
    for column in TIME_COLUMNS:
        frame[column] = pd.to_datetime(frame[column], utc=True)
    frame[SECONDS_COLUMNS] = frame[SECONDS_COLUMNS].astype("int64")
    frame["duration_s"] = (frame["end"] - frame["start"]).dt.total_seconds().astype("int64")
    return frame


def focus_share(frame: pd.DataFrame) -> pd.Series:
    """Share of estimated time at low activation per segment, as in the watch scorer."""
    known = frame["low_s"] + frame["medium_s"] + frame["high_s"]
    return (frame["low_s"] / known.where(known > 0)).fillna(0.0)
