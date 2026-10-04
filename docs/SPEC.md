# stateAI

> Focus coach for smartwatches (Wear OS) that learns at which times of the day you perform best and tells you how much estimated energy you have left to use them. Everything runs on the watch, with no phone and no cloud.

This document is the **project's source of truth**. It is meant to be executed **task by task** (see section 12).

---

## 1. Summary

**Name**: stateAI (former names: "NeuroFocus" and "FlowState AI"). Always use **stateAI** in code, packages, README and UI.

**Problem**: Traditional productivity apps (Pomodoro) are static: they use fixed times and do not consider whether the person is exhausted, overloaded or focused.

**Solution**: A smartwatch-only app that:
1. Asks at the start "what are you going to do?": you pick a recent activity or create a new one (optional free name on top of one of 5 base categories). Each activity learns its own profile over time.
2. Estimates the person's activation level from heart rate, an HR variability proxy and movement (real HRV is not available, see `docs/sensors.md`).
3. Learns over the days in which time slots and with which cycles the person performs best.
4. Signals with **haptic micro-pulses** when to breathe or pause, without needing to look at the screen.
5. Shows an **estimated energy battery** and a daily score.

**Product statement (honest version)**:
> stateAI learns at which times of the day you perform best and tells you how much estimated energy you have left to use them.

**Hierarchy decision**: learning focus windows is the main product. Energy-Budget is **a feature inside it**, a single indicator derived from the same data, not a second model.

---

## 2. Constraints and principles (non-negotiable)

- **Smartwatch only** (Wear OS). No phone app, no backend, no accounts, no cloud.
- **Everything local**: data, learning and inference live on the watch.
- **No physical watch**: development and testing use an **emulator**. Sensors are abstracted behind an interface and simulated.
- **Scientific honesty**: the app **estimates** physiological state (load and recovery). It does not measure real productivity and is not a medical device. Avoid clinical language ("neuro", "diagnosis").
- **Do not assume 90-minute cycles**: the app *discovers* whether the user has cycles and of what length; if there is no clear pattern, it says so.
- **Do not interrupt focus**: no vibration during deep focus, except for sustained fatigue or overload.
- **Recommend, do not impose**: no locks and no "mandatory" breaks.
- **Minimum scope first**: rules before model, model before routine learning.
- **Validation**: with simulated data, clearly documented in the README. Evaluation with simulated data shows that **the system works end to end**, not that stateAI is better than another technique on real people.
- **Verify before building**: the real availability of signals (T-0.4) is resolved **before** fixing features and states. The state model adapts to what the watch can measure, not the other way around.

---

## 3. Stack

**Watch (Kotlin)**
- Jetpack Compose for Wear OS (UI)
- Health Services API (heart rate, BPM only) and SensorManager (accelerometer), always behind `SensorSource`
- `Vibrator` / `VibrationEffect` with waveforms (haptics)
- Foreground service + Ongoing Activity (long sessions)
- Room (segment summaries) and DataStore (settings and baselines)
- Coroutines + Flow
- MVVM, separate modules, optional Hilt
- Tests: JUnit for the domain module (no Android)

**Offline (Python)**
- scikit-learn or TensorFlow for the state classifier
- Export to **TensorFlow Lite** (its main value is demonstrating the Python → TFLite → Kotlin pipeline with verified parity; trained on synthetic data it is not expected to beat the rule engine)
- Synthetic week generator
- Evaluation notebooks

---

## 4. Architecture

Gradle modules:

| Module | Content | Depends on Android |
|---|---|---|
| `:core-domain` | Models, rule-based state engine, learning, energy, scoring | **No** (pure Kotlin) |
| `:sensors` | `SimulatedSensorSource`, `HealthServicesSensorSource` (the `SensorSource` interface lives in `:core-domain`) | Yes |
| `:haptics` | Patterns and rate limiter | Yes |
| `:data` | Room + DataStore | Yes |
| `:ml` | TFLite wrapper | Yes |
| `:app-wear` | Compose UI, ViewModels, session service | Yes |

Rule: `:core-domain` **imports nothing from Android**, so it can be tested with JVM tests.

Data rule: the domain only knows **repository interfaces** (implemented in `:data`). Room and DataStore are swappable implementations. The MVP is 100 % local, but export, a phone or sync could be added without rewriting the logic.

Separate folder in the repo: `/ml-python` (training, synthetic data, evaluation).

---

## 5. Domain model

- **ActivityCategory**: 5 base categories: `DEEP_WORK`, `STUDY`, `READING`, `COLLAB` (meetings/collaborative), `OTHER`. This is the only definition; the rest of the document references it.
- **Activity**: optional normalized free name + category + status (`ACTIVE` / `ARCHIVED`). Without a name, the activity is the category itself.
- **CategoryProfile**: default values of a category (target block duration, state thresholds, "normal" movement, maximum vibrations per hour).
- **ActivityProfile**: the same parameters, but **learned** for a specific activity and blended with its category's according to the number of sessions (see 6.7).
- **SensorSample**: timestamp, heart rate (BPM), movement magnitude. No RR intervals (not exposed by Health Services).
- **FeatureWindow**: ~3 min window computed every 60 s: mean HR, HR variability proxy (standard deviation and mean successive difference of BPM), mean movement, number of micro-fidgets.
- **ActivationLevel**: `LOW`, `MEDIUM`, `HIGH` (decided in 6.4.1). `LOW` during a session counts as focus time.
- **StateEstimate**: activation level + restlessness flag (high fidgeting after a long time in session, replaces the former "exhausted" state).
- **Segment**: one continuous activity within the day (start, end, activity with its category and normalized name, time per activation level, restless time, score, feedback).
- **DayRecord**: list of segments, daily score, final energy.
- **UserBaseline**: resting HR and HR variability proxy **per person**, optionally per time slot (moving averages). What is learned per activity are the profile's thresholds and parameters, not the resting baseline.
- **FocusProfile**: focus profile per time slot and detected cycle (if any).
- **EnergyBudget**: value 0 to 100, daily capacity, consumption and recovery.

---

## 6. Product rules

### 6.1 Screen flow (maximum 4)
1. **Activity picker**: routine-suggested activity first, then recent ones (max. 10 active) and the "New" option (base category + optional name via the standard Wear OS text input). One tap to start.
2. **Session**: time, state icon/color, energy battery. Ambient mode almost empty.
3. **Guided pause**: circle that expands/contracts at breathing pace, synchronized with vibration.
4. **Daily summary**: score, short timeline, best time slot, final battery.

When a segment ends: one-tap feedback "how did you feel?" (good / okay / bad).

### 6.2 Haptic language
| Event | Pattern |
|---|---|
| Block start / focus window | 1 short pulse |
| Breathe | slow pulses at inhale 4 s / exhale 6 s |
| Suggested pause | 1 long soft pulse |
| Overload alert | 2 short pulses |

Rules:
- At most 1 vibration every 5 minutes, excluding start events.
- In focus (`LOW` during a session): no vibration, except sustained restlessness or `HIGH` (3+ min).
- The hourly limit depends on the `ActivityProfile`.

### 6.3 Category profiles (initial values, configurable)
These are the default values of each base category. Each activity gradually replaces them with its own learned values (see 6.7).

| Category | Target block | Sensitivity | Normal movement |
|---|---|---|---|
| Deep work | 60-90 min | low to interruptions | low |
| Study | 25-50 min | medium | low |
| Reading | 30-45 min | high | very low |
| Meetings / collaborative | 30-60 min | medium | medium-high |
| Other | 45 min | medium | medium |

### 6.4 Rule-based state engine
- Computed every 60 s over a ~3 min `FeatureWindow`.
- First session: 2 minutes at rest to set the personal baseline (only once, not per activity; refined with use).
- Thresholds **relative to the user's baseline**, not absolute.
- `LOW`: HR close to baseline, stable HR variability proxy, little movement.
- `MEDIUM`: moderate HR elevation or mixed signals.
- `HIGH`: HR above baseline in a sustained way.
- **Restlessness flag**: a lot of fidgeting after a long session time (fatigue hint).
- Windows with a lot of movement: lower their weight or discard them.

### 6.4.1 Decision: number of states (resolved)
The original 4 states (`DEEP_FOCUS`, `NORMAL`, `OVERLOADED`, `EXHAUSTED`) assumed real HRV (RR intervals). Health Services does not expose RR intervals (see `docs/sensors.md`), and with wrist HR only, distinguishing deep focus from normal is very weak (cognitive load moves HR by a few bpm, the same as coffee, posture or talking).

**Decision**: the domain uses 3 activation levels (`LOW`, `MEDIUM`, `HIGH`) plus a restlessness flag. The UI shows them with friendly Spanish names. Energy, focus windows and scoring work over those levels.

### 6.5 Energy (simple indicator)
- Initial value 100 at the start of the day; optional morning input to adjust it (does not depend on sleep data).
- **Consumes** according to time in session per activation level and while restless (rates per level).
- **Recovers** with pauses that actually improve the state afterwards.
- Daily capacity adjusted by the `FocusProfile` (good/bad time slots).
- Suggestions, not locks: "you have little energy left, it may be better to leave this for later".
- It is called "estimated energy", never "real biological energy".

### 6.6 Score 0-100 (per segment and per day)
Initial weights (adjustable):
- Time in focus: **40 %**
- Recovery (pauses taken that improved the state): **25 %**
- Sustainable load (penalizes prolonged time in `HIGH` or restless): **20 %**
- Consistency (meeting the planned duration): **15 %**

The daily score is the duration-weighted average. Always show it **broken down**, not just the number. It is an indicator of focus quality and load, **not** of work produced.

### 6.7 Free activities and hierarchical learning

Users can create their own activities. Each one hangs from a **base category**, so it never starts from zero.

**Structure**
- Base categories: the 5 defined in `ActivityCategory` (section 5).
- Activity = optional free name (normalized: lowercase, no accents, no extra spaces) + category.

**What is in the MVP and what is postponed**
- MVP: categories, activities with optional name (standard text input), name normalization, learned profile with `n/(n+K)` blending, valid session, cap of 10 active and "learning" indicator.
- Post-MVP (Phase 9): automatic archiving, reactivation with history, similar-name detection ("is it the same?") and dedicated dictation.
- While archiving does not exist, when 10 active activities are reached the "New" option is disabled with a short message.

**Gradual category → activity blending** (avoids an abrupt change in behavior):
```
own_weight = n / (n + K)
value = own_weight * activity_profile + (1 - own_weight) * category_profile
```
- `n` = valid sessions of that activity; `K = 5` (with n=0 it uses 100 % category; with n=5, half and half; with n=20, ~80 % own).
- Applied to each learned parameter: target block, thresholds and normal movement (the resting baseline is personal, not per activity).

**Valid session**: lasts at least 10 min and at least 60 % of its windows are "clean" (not discarded for excess movement). A cut or very noisy session does not add to `n`.

**Limit and archiving**
- Maximum **10 active activities** (the ones in the picker). The reason is usability on a small screen and avoiding statistical dilution, not storage.
- Picker order: routine-suggested, recent, most used.
- *(Post-MVP)* When creating number 11, the least recently used one is **archived**.
- *(Post-MVP)* Archiving **does not delete learning**: its data is kept aggregated in its category, and if the activity is reactivated it comes back with its history.

**Against name fragmentation**: in the MVP normalization is enough ("BD" and "bd " are the same). *(Post-MVP)* When creating a name very similar to an existing one, ask with one tap "is it the same?" before duplicating.

**UX**: while `n` is low, show a discreet "learning" indicator in the summary, without numbers, so as not to suggest precision that does not exist yet.

**Parameters (all configurable constants)**

| Parameter | Initial value |
|---|---|
| `K` (category/activity blending) | 5 |
| Minimum valid session duration | 10 min |
| Minimum % of clean windows | 60 % |
| Maximum active activities | 10 |
| Archiving criterion (post-MVP) | least recently used |
| Base categories | 5 |
| Threshold to show "learning" | n < 5 |

---

## 7. AI: what it learns and how

1. **State classifier** (offline in Python, on-watch inference with TFLite). Input: `FeatureWindow`; output: `ActivationLevel`. The rule engine remains as fallback. Trained on synthetic data, the model essentially re-learns the generator's rules: it is presented as a **pipeline demonstration** (training, export, Kotlin/Python parity, fallback), not as an accuracy improvement.
2. **Personal baseline**: exponential moving averages of resting HR and HR variability proxy, per person and optionally per time slot. Profile parameters (target block, thresholds, normal movement) are **learned per activity**, blended with their category's according to the number of sessions (6.7).
3. **Focus windows per time slot**: mean focus score per hour of the day and day of the week.
4. **Cycle detection** (the core of stateAI): search the user's focus series for periodicity (autocorrelation or periodogram). Report the cycle length **only with enough confidence**; otherwise "no clear pattern".
   - **Self-induced periodicity**: the app itself vibrates when the block is reached and suggests pauses, which can create an artificial cycle. Mitigation: tag app-generated events, discard or exclude the minutes around them from the series, and check that the detected cycle does not simply match the profile's target block.
   - **Series with gaps**: sessions last 25-90 min with pauses between them. The method must tolerate gaps (e.g. Lomb-Scargle periodogram) and "no clear pattern" is expected to be a frequent answer.
   - Implemented method and results on synthetic users: `docs/cycles.md`.
5. **Routine prediction**: frequencies per hour and day to suggest the next activity in the picker.
6. **Feedback personalization**: "how did you feel?" is a label per whole segment, not per window, so it does not tell which windows were misclassified. It is used for a **slow adjustment of the activity's global sensitivity** (small, bounded steps), not to readjust fine thresholds.
7. **Optional (final phase)**: contextual bandit to decide when to suggest a pause.

Out of the MVP: on-watch LLM, natural-language summaries via API.

---

## 8. Data and evaluation

- **Synthetic data**: generator of users with 2 to 3 week routines, with cycles of known length (e.g. 60, 90, 110 min and one user without a cycle), sensor noise and day-to-day variation.
- **Optional public dataset** for the base classifier (e.g. WESAD, **check license and labels**). It measures stress, not productivity: document it as a proxy.
- **End-to-end evaluation**: simulate the same day with (a) a fixed timer and (b) stateAI, and compare time in focus, time in overload and pauses taken.
  - **Circularity warning**: if the simulator generates states with the same logic stateAI uses to classify them, stateAI wins by construction. To reduce this, the generator models physiology with a **different** mechanism from the rule engine (e.g. a latent state with its own transitions and noise), and the report presents the result as proof that the integrated system works, **not** as evidence of superiority.
- **Cycle detection test**: does it recover the known length? Does it say "no pattern" when there is none?
- Limitation to declare: learning is validated with simulated data; it does not show accuracy on real people.

---

## 9. Scope

**In (MVP)**
- Free activities: 5 base categories + optional name, up to 10 active, with a learned profile per activity (6.7). Archiving, reactivation and similar-name detection are left for after the MVP (Phase 9)
- Abstract repositories in `:data` and manual summary export (leaves the door open to a phone or cloud without building them)
- Session with state, haptics and guided pause
- Simulated sensors with a debug panel
- Rule-based state engine, then TFLite model
- Segments, scoring and daily summary
- Baseline, time-slot windows, cycle detection, simple energy
- Synthetic generator and comparative evaluation

**Out of scope**
- Phone app, cloud, accounts, sync
- Long-term history with charts
- Other watches (Apple Watch, etc.)
- Metabolic cost per task, real sleep data
- LLM, diagnostics or any medical use

---

## 10. Risks

- **HRV on Wear OS**: confirmed that Health Services does not expose RR intervals (Samsung's SDK does, but needs a physical Galaxy Watch and partner approval). An "RMSSD" computed over 1 Hz bpm HR is not HRV: stateAI uses an HR variability proxy and calls it that (see `docs/sensors.md`).
- **Weak signals**: wrist HR separates focus from normal poorly. Mitigation: 3 activation levels instead of 4 states (6.4.1).
- **Self-induced periodicity** in cycle detection: see section 7, item 4.
- **Circular evaluation**: see section 8.
- **Watch battery**: a foreground service with continuous HR for hours drains a real watch noticeably. Measure consumption if tested on hardware (T-9.1) and consider intermittent sampling outside sessions.
- **Noisy wrist signal** with movement: weight by movement.
- **Circular synthetic data**: the model may "discover" what the generator put in. Declare it.
- **Large scope for one person**: respect the phase order, finish each before the next.
- **Emulator**: vibration cannot be felt; verify it via log and visual indicator.
- **Cold start and activity fragmentation**: few sessions per activity do not allow learning; mitigated with category/activity blending, the shared personal baseline, the cap of 10 active and name normalization (6.7).

---

## 11. Tasks per phase

Convention: each task is **short**, with a "Done when" criterion. Mark `[x]` when complete.

### Phase 0: Setup
**T-0.4 goes first**: its result decides the features and the number of states (6.4.1). The other tasks of the phase can be done afterwards.

- [x] **T-0.1** Create the Wear OS project (Kotlin, Compose) with the modules of section 4. *Done when:* it builds and runs on the emulator showing an empty screen.
- [x] **T-0.2** Set up the Wear OS emulator and document the steps in `docs/emulator-setup.md`. *Done when:* another person can reproduce it.
- [x] **T-0.3** Create the `/ml-python` folder with a virtual environment and `requirements.txt`. *Done when:* `python -c "import sklearn"` works.
- [x] **T-0.4** Research whether Health Services exposes RR intervals/HRV and how to inject synthetic data into the emulator. Write conclusions in `docs/sensors.md`. *Done when:* the document answers both questions with sources, records the decision of 6.4.1 (4 states or 3 levels) and this document is updated with it.
- [x] **T-0.5** Create the README with the "Limitations and scientific honesty" section (sections 2 and 10 of this document). *Done when:* the README includes them.

### Phase 1: Picker, session and basic haptics
- [x] **T-1.1** Define `ActivityCategory` (5 base), `Activity` (normalized name + category) and `CategoryProfile` with the values of 6.3, in `:core-domain`. *Done when:* there are tests for the category profiles and for name normalization.
- [x] **T-1.2** Picker screen: suggested first, then recent (max. 10 active) and "New" option (disabled with a message when 10 is reached). *Done when:* tapping an activity navigates to the session.
- [x] **T-1.3** Session screen with a profile-based timer (elapsed time and target). *Done when:* time advances and survives rotation/ambient.
- [x] **T-1.4** Foreground service + Ongoing Activity for the session. *Done when:* the session continues with the screen off on the emulator.
- [x] **T-1.5** `:haptics` module with the 4 patterns of 6.2. *Done when:* each pattern is triggered from a debug button and logged.
- [x] **T-1.6** Vibration rate limiter (max. 1 every 5 min, hourly cap per profile). *Done when:* tests show that out-of-rule vibrations are blocked.
- [x] **T-1.7** End-of-block vibration when the profile's target is reached. *Done when:* it fires once when the time is met.
- [x] **T-1.8** "New activity" flow: choose category and optional name (standard Wear OS text input). *Done when:* the activity is created, appears in the picker, and "BD" and "bd " are unified as the same.

### Phase 2: Sensors and simulator
- [x] **T-2.1** Define `SensorSample` and the `SensorSource` interface (sample flow). *Done when:* it compiles and is documented.
- [x] **T-2.2** Implement `SimulatedSensorSource` that plays scripted scenarios. *Done when:* it emits samples at 1 Hz according to the scenario.
- [x] **T-2.3** Scenarios: deep focus, overload, fatigue and mixed session. *Done when:* each has signal-shape tests (HR and movement trends).
- [x] **T-2.4** Debug panel to choose a scenario and see live values (debug builds only). *Done when:* changing the scenario changes the live signal.
- [x] **T-2.5** Implement `HealthServicesSensorSource` (HR and movement) as the real adapter, per the findings of T-0.4. *Done when:* it compiles and can be switched by configuration, even if not tested on a real watch.

### Phase 3: Rule-based state engine
- [x] **T-3.1** Compute `FeatureWindow` (mean HR, HR variability proxy, movement, fidgeting) from samples. *Done when:* tests with known signals give expected values.
- [x] **T-3.2** Personal baseline calibration (2 min, once) and persistence. *Done when:* the baseline is saved and reused across all activities.
- [x] **T-3.3** Rule classifier with thresholds relative to the baseline (6.4). *Done when:* the simulated scenarios produce the expected activation levels and restlessness flag.
- [x] **T-3.4** Reduced weight or discarding of high-movement windows. *Done when:* a test shows high movement does not trigger false states.
- [x] **T-3.5** State smoothing (hysteresis, at least 2 equal windows to change). *Done when:* there is no state flickering in the mixed scenario.
- [x] **T-3.6** Haptic policy: in focus no vibration except sustained restlessness/`HIGH`; suggested pause when leaving focus. *Done when:* tests cover each case.
- [x] **T-3.7** Show the activation level on the session screen (icon and color). *Done when:* it changes live with the simulator.

### Phase 4: Segments, scoring and summary
- [x] **T-4.1** `Segment` and `DayRecord` models (with category and normalized name); close a segment when the activity changes or ends. *Done when:* segment lifecycle tests.
- [x] **T-4.2** Room persistence of segments (summaries only, no raw signal). *Done when:* data survives an app restart.
- [x] **T-4.3** Compute the 0-100 score per segment (6.6). *Done when:* tests with edge cases (all focus, all `HIGH`).
- [x] **T-4.4** Compute the daily score (duration-weighted average). *Done when:* test with 3 segments of different durations.
- [x] **T-4.5** One-tap feedback when closing a segment, saved. *Done when:* it is attached to the segment.
- [x] **T-4.6** Daily summary screen with a **broken-down** score. *Done when:* it shows the 4 components.
- [x] **T-4.7** Guided pause: breathing screen synchronized with vibration (4 s / 6 s). *Done when:* circle and haptics keep the same pace.
- [x] **T-4.8** Repository interfaces (segments, baselines, profiles); `:core-domain` only knows the interfaces. *Done when:* the domain is tested with in-memory repositories.
- [x] **T-4.9** Export summaries (segments and profiles) to JSON or CSV from the watch, for analysis in Python. *Done when:* the file is generated and read from `/ml-python`.

### Phase 5: Learning (in `:core-domain`)
- [x] **T-5.1** Personal baseline with exponential moving average (optionally per time slot), updated after each session. *Done when:* a test shows convergence on new data.
- [x] **T-5.2** `FocusProfile`: mean focus score per time slot and day of the week. *Done when:* tests with several days of data.
- [x] **T-5.3** "Focus window" notice when the current time slot is good according to the profile. *Done when:* it fires only with minimum confidence (e.g. 5+ days).
- [x] **T-5.4** Cycle detection: build the focus series and search for periodicity (autocorrelation/periodogram). *Done when:* it recovers 60/90/110 min cycles in synthetic data with gaps between sessions, and a test confirms end-of-block vibrations do not create a false cycle.
- [x] **T-5.5** Confidence threshold: report "no clear pattern" when there is no periodicity. *Done when:* a synthetic user without a cycle yields "no pattern".
- [x] **T-5.6** Next-activity prediction per hour and day; preselect it in the picker. *Done when:* the picker shows the suggested one first with enough data.
- [ ] **T-5.7** Slow adjustment of the activity's global sensitivity from the "how did you feel?" feedback (small, bounded steps). *Done when:* a test shows sensitivity moves in the right direction and a single feedback does not change it abruptly.
- [ ] **T-5.8** Energy engine (6.5): consumption per state, recovery per useful pause, capacity per time slot. *Done when:* consumption and recovery tests.
- [ ] **T-5.9** Show the battery in the session and summary, with suggestions (never locks). *Done when:* it is visible and changes during a simulated session.
- [ ] **T-5.10** Learned profile per activity with `n / (n + K)` blending towards its category's profile (6.7). *Done when:* tests with n=0, 5 and 20 give the expected weights.
- [x] **T-5.11** Valid session criterion (≥ 10 min and ≥ 60 % clean windows). *Done when:* a short or noisy session does not increase `n`.
- [ ] **T-5.12** Learn target block and normal movement of each activity from its sessions. *Done when:* with simulated sessions the value approaches the synthetic user's.
- [ ] **T-5.13** Cap of 10 active activities (no automatic archiving in the MVP). *Done when:* a test confirms number 11 cannot be created.
- [ ] **T-5.14** "Learning" indicator while `n < 5`. *Done when:* it appears in the summary with low n and disappears afterwards.

### Phase 6: Offline ML (Python)
- [ ] **T-6.1** Synthetic user generator: multi-day routine with known cycles and noise. Physiology is generated with a mechanism **different** from the rule engine (latent state with its own transitions), so the rules are not validated against themselves. *Done when:* it produces reproducible CSVs with a seed and the generator's README explains how it differs from the rule engine.
- [ ] **T-6.2** Include a user without a cycle and one with an irregular cycle. *Done when:* they are in the evaluation dataset.
- [ ] **T-6.3** (Optional) Pipeline for a public stress dataset (check license and labels). *Done when:* it produces the same feature format as `FeatureWindow`.
- [ ] **T-6.4** Feature engineering identical to Kotlin's (same definition of mean HR, HR variability proxy, etc.). *Done when:* there is a parity test with shared sample values.
- [ ] **T-6.5** Train a simple classifier with the states decided in 6.4.1 and evaluate it (confusion matrix). *Done when:* there is a report with metrics comparing it with the rule engine and stating that, with synthetic data, a similar result is expected.
- [ ] **T-6.6** Export to TFLite and verify the model with a script. *Done when:* Python inference with the `.tflite` matches the original model.
- [ ] **T-6.7** Include custom activities with few and many sessions in the generator. *Done when:* the dataset allows testing category/activity blending.

### Phase 7: Model integration
- [ ] **T-7.1** `:ml` module that loads the `.tflite` and classifies a `FeatureWindow`. *Done when:* it returns an `ActivationLevel` on the emulator.
- [ ] **T-7.2** `StateClassifier` interface with two implementations: rules and model. *Done when:* switchable by configuration.
- [ ] **T-7.3** Automatic fallback to rules if the model fails or its confidence is low. *Done when:* a test with a "broken" model uses rules.
- [ ] **T-7.4** Kotlin/Python parity test on sample features. *Done when:* predictions match within tolerance.

### Phase 8: Evaluation and demo
- [ ] **T-8.1** Script that simulates a day with a fixed timer (baseline). *Done when:* it produces metrics of time in focus, overload and pauses.
- [ ] **T-8.2** The same day with stateAI. *Done when:* it produces the same metrics.
- [ ] **T-8.3** End-to-end report with charts in a notebook. *Done when:* the notebook is in the repo and states that the data is simulated, that the result shows the integrated system works and that it does **not** prove superiority on real people.
- [ ] **T-8.4** Cycle detection test report (T-5.4 and T-5.5). *Done when:* table of real vs detected length.
- [ ] **T-8.5** Prepare the demo: 14-day synthetic user, simulation panel and full flow. *Done when:* it can be shown end to end on the emulator.
- [ ] **T-8.6** Screenshots or a short video for the portfolio. *Done when:* they are in `/docs/media`.
- [ ] **T-8.7** Polish README: problem, architecture, decisions, limitations, how to run. *Done when:* an outside person can understand it in 5 minutes.

### Phase 9: Post-MVP / optional
- [ ] **T-9.1** Test with a real watch, even a borrowed one, and record 3 to 5 sessions, including battery consumption. *Done when:* there are notes on what worked, what did not and how much battery a session uses.
- [ ] **T-9.2** Contextual bandit to decide when to suggest pauses. *Done when:* it improves time in focus in simulation versus fixed rules.
- [ ] **T-9.3** Automatic archiving when creating activity number 11 (least recently used). *Done when:* creating number 11 archives the right one.
- [ ] **T-9.4** Archiving without losing learning: aggregate into the category and reactivate with history. *Done when:* a test confirms data is preserved after archiving and reactivating.
- [ ] **T-9.5** Similar-name detection on creation ("is it the same?"). *Done when:* a name similar to an existing one triggers the question.
- [ ] **T-9.6** Dedicated dictation to name activities. *Done when:* an activity can be created with voice only.

---

## 12. Working rules

1. **Work one task at a time**, in phase order, starting with **T-0.4**. Do not move to the next phase without closing the current one. Phase 9 is not touched until Phase 8 is done.
2. Before starting a task, read it together with its "Done when" and outline a 3-5 line plan.
3. **Do not expand scope**: anything under "Out of scope" (section 9) is not implemented.
4. Everything in `:core-domain` must have **unit tests** and must not import Android.
5. Keep functions and classes small and single-responsibility. Code, folder names, comments and documentation are in **English**; only user-facing UI text is in **Spanish** (string resources).
6. Any threshold, weight or duration must be a **configurable constant**, not a magic number.
7. If a task requires a decision not covered by this document, **ask** before assuming.
8. If a technical assumption may be false (e.g. whether emulator synthetic data reaches `MeasureClient`), **verify and document it** before building on top of it.
9. When finishing a task: mark `[x]` and make one Conventional Commit describing what was done (no task numbers in the message). See `CONTRIBUTING.md`.
10. Keep product language honest: "estimate", "estimated energy", "focus and load indicator". Never "measures your brain" or medical claims.
