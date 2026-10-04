# Feature definitions

Exact definitions of the features computed for every window. The Kotlin implementation
(`core-domain`, package `features`) and the Python one (`ml-python/src/stateai_ml/features.py`)
must match them. Both are tested against the same cases in `shared/parity/feature_cases.json`
(`FeatureParityTest` in Kotlin, `test_features.py` in Python). Regenerate the file with
`python -m stateai_ml.parity` only when a definition changes on purpose, and update both sides.

## Window

- A window covers the last **180 s** of samples and is computed every **60 s**.
- Samples arrive about once per second. A sample may have no heart rate (`null`).

## Features

| Feature | Definition |
|---|---|
| `sampleCount` | Number of samples in the window. |
| `heartRateCount` | Number of samples with a heart rate. |
| `meanHeartRate` | Arithmetic mean of the available heart rates (bpm). Undefined when `heartRateCount` is 0. |
| `heartRateStdDev` | Population standard deviation of the available heart rates. 0 when fewer than 2 values. |
| `heartRateMeanAbsDiff` | Mean of `abs(hr[i] - hr[i-1])` over consecutive samples where **both** have a heart rate. 0 when there are no such pairs. This is the HR variability proxy; it is **not** RMSSD. |
| `meanMovement` | Arithmetic mean of `movement` (m/s²) over all samples. |
| `fidgetCount` | Number of times `movement` rises above **1.5 m/s²** from a sample at or below it (a burst of consecutive high seconds counts once; a window starting above the threshold counts one fidget). |
| `highMovementShare` | Fraction of samples with `movement` above **1.0 m/s²** (sustained activity such as walking). |

## Clean windows

A window is **clean** when `highMovementShare` is below **0.3** and `heartRateCount` is at least
**50 %** of `sampleCount`. Windows that are not clean carry no reliable evidence about the person's
state: the classifier keeps the previous level instead of reacting to them.
