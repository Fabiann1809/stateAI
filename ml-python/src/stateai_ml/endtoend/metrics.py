"""Day metrics compared between policies. Levels are the ground-truth latent modes."""

import pandas as pd

FOCUS, OVERLOAD = "LOW", "HIGH"


def day_metrics(minutes: pd.DataFrame) -> dict:
    work = minutes[minutes["activity"] == "work"]
    pauses = minutes[minutes["activity"] == "pause"]
    starts = (minutes["activity"] == "pause") & (minutes["activity"].shift() != "pause")
    return {
        "work_minutes": len(work),
        "focus_minutes": int((work["level"] == FOCUS).sum()),
        "overload_minutes": int((work["level"] == OVERLOAD).sum()),
        "restless_minutes": int(work["restless"].sum()),
        "pauses": int(starts.sum()),
        "pause_minutes": len(pauses),
    }


def summarize(per_day: pd.DataFrame) -> pd.DataFrame:
    """Mean per day for each policy, plus shares of work time in focus and in overload."""
    means = per_day.groupby("policy").mean(numeric_only=True).drop(columns=["day"], errors="ignore")
    means["focus_share"] = means["focus_minutes"] / means["work_minutes"]
    means["overload_share"] = means["overload_minutes"] / means["work_minutes"]
    return means.round(3)
