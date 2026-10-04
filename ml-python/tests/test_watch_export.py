"""The Python reader understands the CSV written by the watch."""

from pathlib import Path

from stateai_ml.watch_export import focus_share, load_segments

SAMPLE = Path(__file__).parent / "data" / "segments_sample.csv"


def test_loads_rows_with_quoted_names_and_missing_values() -> None:
    frame = load_segments(SAMPLE)

    assert list(frame["segment_id"]) == ["s1", "s2"]
    assert frame.loc[0, "activity_name"] == 'Tesis "cap 2"'
    assert frame.loc[1, "activity_name"] == ""
    assert frame["feedback"].isna().tolist() == [False, True]


def test_parses_times_and_derives_duration() -> None:
    frame = load_segments(SAMPLE)

    assert str(frame.loc[0, "start"].tz) == "UTC"
    assert frame["duration_s"].tolist() == [2400, 1800]


def test_focus_share_matches_the_watch_definition() -> None:
    shares = focus_share(load_segments(SAMPLE))

    assert shares.round(3).tolist() == [1.0, 0.357]
