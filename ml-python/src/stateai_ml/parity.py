"""Shared parity cases between the Kotlin and Python feature implementations.

``python -m stateai_ml.parity`` rewrites ``shared/parity/feature_cases.json`` from the cases below,
with expected values computed by this Python implementation. The Kotlin test reads the same file.
"""

import json
from pathlib import Path

import numpy as np

from stateai_ml.features import extract

CASES_FILE = Path(__file__).resolve().parents[3] / "shared" / "parity" / "feature_cases.json"


def build_cases() -> list[dict]:
    rng = np.random.default_rng(2026)
    noisy_rates = [float(round(r, 3)) for r in 68 + rng.normal(0, 2, 180)]
    noisy_moves = [float(round(m, 3)) for m in rng.lognormal(-2.5, 0.4, 180)]
    with_gaps = [None if index % 7 == 0 else rate for index, rate in enumerate(noisy_rates)]
    return [
        _case("steady", [65.0, 65.0, 66.0, 64.0], [0.05, 0.06, 0.04, 0.05]),
        _case("missing_heart_rate", [60.0, None, 64.0, 65.0], [0.1, 0.1, 0.1, 0.1]),
        _case("no_heart_rate", [None, None, None], [0.1, 0.2, 0.3]),
        _case("fidget_bursts", [70.0] * 7, [0.1, 2.0, 2.4, 0.1, 0.1, 1.9, 0.2]),
        _case("walking", [90.0] * 6, [1.2, 1.3, 1.4, 1.1, 0.2, 0.1]),
        _case("noisy_window", noisy_rates, noisy_moves),
        _case("noisy_window_with_gaps", with_gaps, noisy_moves),
    ]


def _case(name: str, heart_rates: list, movements: list) -> dict:
    return {
        "name": name,
        "heart_rates": heart_rates,
        "movements": movements,
        "expected": extract(heart_rates, movements).as_dict(),
    }


def main() -> None:
    CASES_FILE.parent.mkdir(parents=True, exist_ok=True)
    CASES_FILE.write_text(json.dumps(build_cases(), indent=2) + "\n", encoding="utf-8", newline="\n")
    print(f"wrote {CASES_FILE}")


if __name__ == "__main__":
    main()
