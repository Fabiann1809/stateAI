# stateAI

[![CI](https://github.com/Fabiann1809/stateAI/actions/workflows/ci.yml/badge.svg)](https://github.com/Fabiann1809/stateAI/actions/workflows/ci.yml)

**stateAI learns at which times of the day you perform best and tells you how much estimated energy you have left to use them.**

A focus coach for Wear OS smartwatches. Everything runs on the watch: no phone app, no backend, no accounts, no cloud.

> Status: early development. The project is built phase by phase following [`docs/SPEC.md`](docs/SPEC.md).

## Why

Pomodoro-style apps use fixed times and ignore whether you are calm, loaded or restless. stateAI:

1. Asks "what are you going to do?" and keeps a learned profile per activity.
2. Estimates your **activation level** from heart rate, an HR variability proxy and movement.
3. Learns over the days which time slots work best for you and whether you have a focus cycle.
4. Nudges you with **haptic micro-pulses** to breathe or pause, without looking at the screen.
5. Shows an **estimated energy** battery and a daily score broken down into its parts.

## Limitations and scientific honesty

stateAI **estimates**; it does not measure. Please read this before drawing conclusions from it.

- **Not a medical device.** It estimates physiological load and recovery. It does not measure productivity, cognition or health, and makes no diagnosis.
- **No real HRV.** Wear OS Health Services exposes heart rate in BPM only, not beat-to-beat (RR) intervals. stateAI uses an HR variability *proxy* and calls it that. Because of this, it uses 3 activation levels instead of claiming to detect "deep focus" or "exhaustion". Details in [`docs/sensors.md`](docs/sensors.md).
- **Wrist signals are weak and noisy.** Cognitive load moves heart rate by a few bpm, about as much as coffee, posture or talking. High-movement windows are down-weighted or discarded.
- **Cycles are discovered, not assumed.** stateAI does not assume 90-minute cycles. It reports a cycle length only with enough confidence and says "no clear pattern" otherwise, which is expected to be common. The app's own vibrations could create an artificial cycle; this is mitigated and tested.
- **Validated with simulated data only.** Development uses an emulator and synthetic users. The evaluation shows that the system works end to end; it does **not** show that stateAI beats other techniques on real people. Synthetic data can be circular: a model may "discover" what the generator put in. The generator uses a different mechanism from the rule engine to reduce this.
- **The ML model is a pipeline demonstration.** A classifier trained on synthetic data mostly re-learns the generator's rules. Its value is the Python → TFLite → Kotlin pipeline with verified parity and a rule-based fallback, not better accuracy.
- **"Estimated energy" is an indicator**, derived from the same data. It is not real biological energy, and it only suggests; it never blocks you.
- **Battery** has not been measured on real hardware yet. Continuous heart rate sampling in a foreground service can drain a watch noticeably.

## Project structure

| Path | Content |
|---|---|
| `core-domain/` | Pure Kotlin domain: models, rule engine, learning, energy, scoring |
| `sensors/` | `SensorSource` abstraction, simulator and Health Services adapter |
| `haptics/` | Haptic patterns and rate limiting |
| `data/` | Room and DataStore implementations of the domain repositories |
| `ml/` | TFLite wrapper |
| `app-wear/` | Compose for Wear OS UI, ViewModels, session service |
| `ml-python/` | Synthetic data, training, export and evaluation |
| `build-logic/` | Gradle convention plugins |
| `docs/` | Specification and technical notes |

## Getting started

- Wear OS app: see [`docs/emulator-setup.md`](docs/emulator-setup.md), then `./gradlew :app-wear:installDebug`.
- Python tooling: see [`ml-python/README.md`](ml-python/README.md).
- Checks: `./gradlew check` and, in `ml-python/`, `pytest`.

## Contributing

See [`CONTRIBUTING.md`](CONTRIBUTING.md) for commit conventions, git hooks and code rules.
