# Focus cycle detection

stateAI does not assume 90-minute cycles. It looks for a rhythm in the person's focus and reports
one only when the evidence is strong; otherwise it says "no clear pattern" (SPEC 7.4).

## Method (`core-domain`, package `cycles`)

1. **Series.** Every usable window of every segment becomes one observation at its absolute minute:
   1 when the estimate is `LOW` (focus), 0 otherwise. Windows without an estimate and windows within
   ±2 minutes of a cue the app played are dropped.
2. **Remove session-locked effects.** The mean focus at each minute-since-session-start, across all
   sessions, is subtracted from every observation. Warm-up, the end-of-block cue and any other effect
   tied to the session itself disappear. This matters: when sessions start at similar times every
   day, a session-locked drop (e.g. after the end-of-block vibration) lines up with the clock and
   looks like a cycle.
3. **Periodogram.** A Schuster periodogram over periods from 40 to 150 minutes, evaluated on the
   unevenly sampled series (sessions with gaps between them). Per-session sums are precomputed.
4. **Significance by permutation.** Each session is shifted to a random time 1,000 times. Shifts
   keep each session's internal shape but break any rhythm shared across sessions, so only a rhythm
   of the person (independent of when sessions start) beats the null. The p-value is the share of
   permutations whose highest peak reaches the observed one.
5. **Guards.** A period is reported only if p ≤ 0.01, the first and second halves of the history
   find the same peak (±10 %, rules out aliases of a regular daily schedule), and it does not match
   the typical planned block (±10 %, the app's own rhythm). It also needs at least 6 sessions and
   300 usable windows.

## Results on synthetic users

Synthetic users (`SyntheticFocusUser` in the tests): three weeks, four sessions a day of 50-80
minutes at jittered times, focus probability `0.5 + 0.35 cos(2πt/P + φ_day)` with a new phase every
day, and focus sampled independently every minute.

| User | Result over fixed seeds |
|---|---|
| Cycle of 60 min | detected in 8 of 10, all within ±10 % |
| Cycle of 90 min | detected in 9 of 10, all within ±10 % |
| Cycle of 110 min | detected in 6 of 10, all within ±10 % |
| No cycle (sticky random focus stretches) | 0 false detections in 20 |
| No cycle, focus drops after the end-of-block cue | 0 false detections in 5 |

Before adding the split-half guard, one 60-minute user was reported with a wrong period (93 min) at
p = 0.006, an alias of the daily schedule; the guard removed it.

## Limitations

- These results come from simulated data whose rhythm was put there by the generator. They show the
  detector can recover a rhythm of this strength with three weeks of data; they say nothing about
  whether real people have such rhythms or how strong they are.
- With two weeks of data, detection rates drop noticeably. "No clear pattern" is expected to be the
  most common answer for real users, especially early on.
- The tests take about 45 seconds because of the permutations.
