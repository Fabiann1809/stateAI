"""Writes the synthetic evaluation dataset: ``python -m stateai_ml.synthetic --out data/generated``."""

import argparse
from pathlib import Path

from stateai_ml.synthetic.generator import generate_user, write_user
from stateai_ml.synthetic.users import USERS


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--out", type=Path, default=Path("data/generated"))
    parser.add_argument("--users", nargs="*", default=list(USERS), choices=list(USERS))
    arguments = parser.parse_args()
    for name in arguments.users:
        write_user(generate_user(USERS[name]), arguments.out, name)
        print(f"wrote {name} to {arguments.out}")


if __name__ == "__main__":
    main()
