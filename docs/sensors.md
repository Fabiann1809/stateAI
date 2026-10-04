# Sensors: what the watch can measure

Research done on 2026-10-04 to answer two questions before designing features and states (see `SPEC.md`, 6.4.1).

## 1. Does Health Services expose RR intervals / HRV?

**No.** The Health Services client (`androidx.health:health-services-client`, latest stable **1.1.0**, September 2026) provides heart rate only as `DataType.HEART_RATE_BPM` (a `Double` in beats per minute). There is no RR interval, inter-beat interval (IBI) or heart rate variability data type. The 1.1.0 release added debounced goals, exercise events, running dynamics and swimming laps, nothing related to beat-to-beat data.

Additional facts relevant to the design:

- During exercise all devices sample heart rate once per second, but some report a value only when it changes. **A BPM value every second is not guaranteed**, so feature computation must tolerate irregular samples.
- The **Samsung Health Sensor SDK** does provide IBI (0-4 values per heart rate event) on Galaxy Watch4 and later. It is not an option for stateAI:
  - it does not run on the emulator and requires a physical Galaxy Watch;
  - it only supports Wear OS powered by Samsung;
  - public distribution requires approval through the Samsung Partner Program.
- Movement comes from the accelerometer through `SensorManager`, available on every watch and on the emulator.

### Consequence: an "RMSSD" computed from 1 Hz BPM is not HRV

RMSSD needs beat-to-beat intervals. Applying the same formula to BPM samples measures how much the averaged heart rate moves between seconds. stateAI therefore uses an **HR variability proxy** (standard deviation and successive differences of BPM within a window) and always calls it a proxy, never HRV.

## 2. How can synthetic data be injected into the emulator?

There are two layers, and stateAI uses both for different purposes:

1. **Own simulator (primary).** `SimulatedSensorSource` behind the `SensorSource` interface plays scripted scenarios (deep focus, overload, fatigue, mixed) at 1 Hz. It is deterministic, testable on the JVM, and independent of the emulator. This is what the rule engine, tests and demo use.
2. **Health Services synthetic data (adapter check).** Used only to verify that `HealthServicesSensorSource` compiles, connects and receives values:
   - **Wear OS 4+**: the emulator's *Wear Health Services* sensor panel (Android Studio) lets you toggle capabilities and override metrics such as heart rate, then *Apply*. No adb commands are needed.
   - **Wear OS 3**: enable synthetic providers by adb:

     ```sh
     adb shell am broadcast -a "whs.USE_SYNTHETIC_PROVIDERS" com.google.android.wearable.healthservices
     adb shell am broadcast -a "whs.synthetic.user.START_EXERCISE" \
         --ei exercise_options_heart_rate 90 com.google.android.wearable.healthservices
     adb shell am broadcast -a "whs.synthetic.user.STOP_EXERCISE" com.google.android.wearable.healthservices
     adb shell am broadcast -a "whs.USE_SENSOR_PROVIDERS" com.google.android.wearable.healthservices
     ```
   - The documentation describes synthetic data in terms of **exercises (`ExerciseClient`)**, but **verified on 2026-10-04**: on the Wear OS 6 emulator, `MeasureClient` also receives synthetic heart rate values (about 105-140 bpm by default) without any adb command.
   - Accelerometer values can be changed from the emulator's *Extended controls → Virtual sensors*.

## Decision (SPEC 6.4.1)

Real RR/HRV is not available, so **the domain uses 3 activation levels** instead of 4 states:

| Level | Meaning (relative to the personal baseline) | Replaces |
|---|---|---|
| `LOW` | HR close to baseline, low HR variability proxy change, little movement | `DEEP_FOCUS` (when in a session) |
| `MEDIUM` | Moderate HR elevation or mixed signals | `NORMAL` |
| `HIGH` | HR sustained above baseline | `OVERLOADED` |

Fatigue (former `EXHAUSTED`) cannot be inferred from HRV. It becomes a separate **restlessness flag**: high fidgeting after a long time in session. The UI shows friendly Spanish names for the levels.

## Sources

- [Health Services compatibility (heart rate sampling)](https://developer.android.com/health-and-fitness/health-services/compatibility)
- [Health Services client release notes](https://developer.android.com/jetpack/androidx/releases/health)
- [`DataType` API reference](https://developer.android.com/reference/androidx/health/services/client/data/DataType)
- [Simulate sensor data with Health Services](https://developer.android.com/health-and-fitness/health-services/simulated-data)
- [Samsung Health Sensor SDK overview](https://developer.samsung.com/health/sensor/overview.html)
- [Samsung Health Sensor SDK FAQ (emulator, partnership)](https://developer.samsung.com/health/sensor/faq.html)
