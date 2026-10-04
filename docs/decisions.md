# Decision log

Decisions not fully specified by `SPEC.md`, recorded so they can be reviewed later. Newest last.

| # | Decision | Reason |
|---|---|---|
| 1 | The domain uses 3 activation levels (`LOW`, `MEDIUM`, `HIGH`) plus a restlessness flag. | Health Services exposes no RR intervals (see `sensors.md`). |
| 2 | UI names for the levels: **Enfocado** (`LOW`), **Normal** (`MEDIUM`), **Sobrecarga** (`HIGH`). | Chosen by the product owner. Screens and docs still describe them as estimates. |
| 3 | Default target block per category is the midpoint of the spec range: deep work 75, study 40, reading 40, collaborative 45, other 45 min. | Values are configurable constants and are learned per activity anyway. |
| 4 | Manual dependency injection through an `AppContainer`, no Hilt. | Less build overhead and easier to read. |
| 5 | Category "sensitivity" in 6.3 is read as **detection sensitivity** (how readily thresholds react). The hourly vibration cap is a separate value: deep work 2, study 3, reading 2, collaborative 4, other 3. | The spec table does not define the cap; quieter caps protect the categories that need long, uninterrupted blocks. |
| 6 | Haptic policy rules (event type, rate limiting) live in `:core-domain`; `:haptics` only turns events into Android vibrations. | Keeps the rules testable on the JVM and the Android module thin. |
| 7 | When the target block is reached, the "suggested pause" pattern fires once. | 6.2 has no dedicated end-of-block pattern; a pause suggestion is what the event means. |
| 8 | Until Room persistence arrives, activities live in an in-memory repository behind the domain interface. | Lets the picker work now without building persistence ahead of its phase. |
