# Synthetic users

Generates reproducible users for evaluation: `python -m stateai_ml.synthetic --out data/generated`.
Each user produces three CSV files:

| File | Content |
|---|---|
| `<user>_sessions.csv` | One row per session: activity, category, start, end, planned block. |
| `<user>_minutes.csv` | Ground truth per minute: latent level (`LOW`/`MEDIUM`/`HIGH`), restlessness, walking. |
| `<user>_samples.csv` | One sample per second: heart rate (empty when missing) and movement (m/s²). |

The same user spec, including its seed, always produces identical files.

## How it differs from the rule engine

The watch's rule engine estimates the activation level by **thresholding the mean heart rate of a
3-minute window against a baseline**, plus a movement limit. If the generator worked the same way
(e.g. "HIGH means heart rate above baseline + 12"), any evaluation would only prove that the rules
agree with themselves. So the generator uses a different, latent-state mechanism:

1. **Latent mode per minute** (`latent.py`). A sticky Markov chain over calm / engaged / strained.
   When it jumps (15 % of minutes), the next mode is drawn from a softmax whose scores depend on
   the user's rhythm (its peaks favor calm focus) and on fatigue (time in session favors strain).
   Restlessness is its own sticky process that appears late in sessions.
2. **Physiology per second** (`physiology.py`). Heart rate is the resting rate plus a mode offset,
   plus an Ornstein-Uhlenbeck drift (slow wandering of about 1.5 bpm, unrelated to the mode), plus
   beat-to-beat noise, with 5 % of seconds missing. Movement is log-normal noise with Poisson fidget
   spikes (more when strained or restless) and occasional walking minutes (high movement and higher
   heart rate that carry no information about focus).
3. **Ground truth is the latent mode**, not anything derived from heart rate. The rule engine sees
   only the noisy signal and must infer the mode, so disagreements are real classification errors.

Consequences to keep in mind:

- The mapping from mode to heart rate offset (1.5 / 6 / 15 bpm) is still a modeling choice. A
  classifier can learn it; that says nothing about real people.
- Rhythms are put in by construction. Cycle detection results on these users show the detector can
  recover a rhythm of this kind and strength, not that people have one.
