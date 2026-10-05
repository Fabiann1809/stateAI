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
| `ml` | Envoltorio de TFLite (Fase 7). | Sí |
| `app-wear` | Interfaz en Compose, ViewModels, el servicio en primer plano de la sesión y el `AppContainer` (inyección de dependencias manual). | Sí |

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

Los perfiles aprendidos de cada actividad tendrán su propio repositorio cuando llegue el aprendizaje de
perfiles (Fase 5).
