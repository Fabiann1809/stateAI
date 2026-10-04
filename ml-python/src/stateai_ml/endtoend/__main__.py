"""Simulates the evaluation days: ``python -m stateai_ml.endtoend --days 14``.

Writes one row per user, day and policy to ``reports/endtoend_days.csv`` and prints the mean day of
each policy. The data is simulated (see ``world.py``); it shows the integrated system working, not
that one policy is better for real people.
"""

import argparse
from pathlib import Path

from stateai_ml.endtoend.metrics import summarize
from stateai_ml.endtoend.runner import POLICIES, run
from stateai_ml.synthetic.users import USERS


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--days", type=int, default=14)
    parser.add_argument("--users", nargs="*", default=list(USERS), choices=list(USERS))
    parser.add_argument("--policies", nargs="*", default=list(POLICIES), choices=list(POLICIES))
    parser.add_argument("--out", type=Path, default=Path("reports/endtoend_days.csv"))
    arguments = parser.parse_args()
    per_day = run(
        [USERS[name] for name in arguments.users],
        arguments.days,
        {name: POLICIES[name] for name in arguments.policies},
    )
    arguments.out.parent.mkdir(parents=True, exist_ok=True)
    per_day.to_csv(arguments.out, index=False)
    print(f"wrote {arguments.out} ({len(per_day)} simulated days)")
    print(summarize(per_day).to_string())


if __name__ == "__main__":
    main()
