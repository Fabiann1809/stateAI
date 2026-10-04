# Architecture

## Modules and dependencies

```
app-wear ──► core-domain ◄── data
   │              ▲   ▲
   ├──► sensors ──┘   │
   ├──► haptics ──────┘
   └──► data
```

| Module | Responsibility | Android |
|---|---|---|
| `core-domain` | Models, rules, learning, scoring and the interfaces the domain needs (`ActivityRepository`, `SegmentRepository`, `BaselineRepository`, `SensorSource`, `HapticPlayer`). | No |
| `sensors` | `SimulatedSensorSource` (scripted scenarios) and `HealthServicesSensorSource`. | Yes |
| `haptics` | Waveforms and `VibratorHapticPlayer`. | Yes |
| `data` | Room and DataStore implementations behind `LocalStorage`, which only exposes domain interfaces. | Yes |
| `ml` | TFLite wrapper (Phase 7). | Yes |
| `app-wear` | Compose UI, ViewModels, the session foreground service and the `AppContainer` (manual DI). | Yes |

Rules:

- `core-domain` imports nothing from Android and every behavior has JVM unit tests.
- The domain owns the interfaces it consumes (dependency inversion). Implementations live in the
  Android modules and are wired in `AppContainer`.
- Domain tests use in-memory fakes (`core-domain/src/test/.../testing`); `data` provides
  `InMemoryActivityRepository` and `InMemorySegmentRepository` for app-level tests.
- Storage keeps summaries only, never the raw signal.

## Session pipeline

```
SensorSource ─► BaselineKeeper (first use: 2-min calibration, saved in DataStore)
             └► FeatureWindowStream (3-min window every 60 s)
                  └► StateEngine (clean-window filter → RuleBasedClassifier → StateSmoother)
                       ├► MonitorStatus ─► session screen
                       ├► SegmentRecorder (time per level, pauses) ─► EndSession ─► SegmentRepository
                       └► HapticPolicy ─► RateLimitedHapticPlayer ─► VibratorHapticPlayer
```

`SessionService` (foreground, Ongoing Activity) runs `SessionMonitor` while a session is active, so
estimation continues with the screen off.

## Repositories

| Interface | Implementation | Notes |
|---|---|---|
| `ActivityRepository` | `RoomActivityRepository` | Ordered by last use; key = category + normalized name. |
| `SegmentRepository` | `RoomSegmentRepository` | Segment summaries with their guided pauses and feedback. |
| `BaselineRepository` | `DataStoreBaselineRepository` | Personal resting baseline. |

Learned activity profiles get their own repository when profile learning arrives (Phase 5).
