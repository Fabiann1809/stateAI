# Arquitectura

## Módulos y dependencias

```
app-wear ──► core-domain ◄── data
   │              ▲   ▲
   ├──► sensors ──┘   │
   ├──► haptics ──────┘
   └──► data
```

| Módulo | Responsabilidad | Android |
|---|---|---|
| `core-domain` | Modelos, reglas, aprendizaje, puntuación y las interfaces que necesita el dominio (`ActivityRepository`, `SegmentRepository`, `BaselineRepository`, `SensorSource`, `HapticPlayer`). | No |
| `sensors` | `SimulatedSensorSource` (escenarios programados) y `HealthServicesSensorSource`. | Sí |
| `haptics` | Formas de onda y `VibratorHapticPlayer`. | Sí |
| `data` | Implementaciones con Room y DataStore detrás de `LocalStorage`, que solo expone interfaces del dominio. | Sí |
| `ml` | `TfLiteLevelModel`: envoltorio del clasificador TFLite entrenado con datos sintéticos (Fase 7). Si falla, la app vuelve al clasificador por reglas. | Sí |
| `shared` | Datos generados por `ml-python` (casos de paridad y trazas de ciclos) que leen los tests de Kotlin para comprobar que ambos lados coinciden. | No |
| `app-wear` | Interfaz en Compose, ViewModels, el servicio en primer plano de la sesión, la entrada de voz y el `AppContainer` (inyección de dependencias manual, repartida en módulos: sensores, hápticos, clasificador, insights). | Sí |

Reglas:

- `core-domain` no importa nada de Android y todo su comportamiento tiene tests unitarios en la JVM.
- El dominio es dueño de las interfaces que consume (inversión de dependencias). Las implementaciones
  viven en los módulos de Android y se conectan en `AppContainer`.
- Los tests del dominio usan dobles en memoria (`core-domain/src/test/.../testing`); `data` ofrece
  `InMemoryActivityRepository` e `InMemorySegmentRepository` para los tests de la app.
- El almacenamiento guarda solo resúmenes, nunca la señal cruda.

## Flujo de una sesión

```
SensorSource ─► BaselineKeeper (primer uso: calibración de 2 min, guardada en DataStore)
             └► FeatureWindowStream (ventana de 3 min cada 60 s)
                  └► StateEngine (filtro de ventanas limpias → RuleBasedClassifier → StateSmoother)
                       ├► MonitorStatus ─► pantalla de sesión
                       ├► SegmentRecorder (tiempo por nivel, pausas) ─► EndSession ─► SegmentRepository
                       └► HapticPolicy ─► RateLimitedHapticPlayer ─► VibratorHapticPlayer
```

`SessionService` (en primer plano, con Ongoing Activity) ejecuta `SessionMonitor` mientras hay una
sesión activa, así que la estimación continúa con la pantalla apagada.

## Repositorios

| Interfaz | Implementación | Notas |
|---|---|---|
| `ActivityRepository` | `RoomActivityRepository` | Ordenadas por último uso; clave = categoría + nombre normalizado. |
| `SegmentRepository` | `RoomSegmentRepository` | Resúmenes de segmentos con sus pausas guiadas y su feedback. |
| `BaselineRepository` | `DataStoreBaselineRepository` | Línea base personal en reposo. |
| `LearningRepository` | `RoomLearningRepository` | Perfil aprendido por actividad y progreso de aprendizaje. |
| `PauseResponseRepository` | `DataStorePauseResponseRepository` | Qué avisos aceptas o rechazas; alimenta la sensibilidad de los avisos. |
| `MascotRepository` | `DataStoreMascotRepository` | Nombre de la mascota. |
| `VoiceConsentRepository` | `DataStoreVoiceConsentRepository` | Consentimiento para usar el reconocedor de voz del sistema. |

## Qué calcula el dominio

| Paquete de `core-domain` | Qué hace |
|---|---|
| `features`, `sensing`, `state` | Ventanas de señal, clasificación (reglas o TFLite) y suavizado de estados. |
| `session` | Inicio, seguimiento y fin de sesión; sugerencias de pausa y señales hápticas. |
| `pause` | `PauseAdvisor` decide cuándo sugerir pausa y `GuidedPause` registra la respiración guiada y su efecto. |
| `learning`, `profile` | Perfil por actividad, sensibilidad a los avisos, mejor franja horaria, actividad sugerida y el descubrimiento de en qué momento valoras mejor tus sesiones. |
| `energy`, `score`, `summary` | Energía estimada, puntuación diaria desglosada y resúmenes de sesión y día. |
| `cycles` | Detección de ciclos de concentración con periodograma; solo se afirma con confianza. |
| `voice` | Interpretación de lo que dices (`IntentParser`, `ActivityMatcher`) y la ruta de respaldo a la lista. |

## Interfaz (`app-wear/ui`)

Un paquete por pantalla (`home`, `session`, `pause`, `feedback`, `summary`, `sessionsummary`,
`newactivity`, `voice`, `ambient`, `debug`), más `components` (silueta de energía, indicadores de
estado, botones), `mascot` (Lumi dibujada en Canvas) y `theme` (colores Aurora, tipografía Lexend).
`navigation/Screen.kt` pone el mismo fondo en todas las pantallas. Los resúmenes son páginas
verticales (`VerticalPages`). Las decisiones de diseño y producto están en
[`decisions.md`](decisions.md).

## Voz

`SpeechInput` abstrae el reconocedor: `RecognizerSpeechInput` usa el del sistema y `TextSpeechInput`
(solo debug) lo reemplaza por texto escrito en el emulador, que no tiene reconocedor. Cualquier fallo
lleva a la lista de actividades. Detalles de privacidad en el README.
