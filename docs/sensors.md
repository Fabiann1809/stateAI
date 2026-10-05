# Sensores: qué puede medir el reloj

Investigación hecha el 2026-10-04 para responder dos preguntas antes de diseñar las características y
los estados (ver `SPEC.md`, 6.4.1).

## 1. ¿Health Services ofrece intervalos RR o la VFC?

**No.** El cliente de Health Services (`androidx.health:health-services-client`, última versión estable **1.1.0**, septiembre de 2026) solo ofrece el pulso como `DataType.HEART_RATE_BPM` (un `Double` en latidos por minuto). No existe ningún tipo de dato para intervalos RR, intervalos entre latidos (IBI) ni variabilidad de la frecuencia cardíaca. La versión 1.1.0 añadió objetivos con antirrebote, eventos de ejercicio, dinámica de carrera y largos de natación; nada relacionado con datos latido a latido.

Otros datos relevantes para el diseño:

- Durante el ejercicio, todos los dispositivos muestrean el pulso una vez por segundo, pero algunos solo informan un valor cuando cambia. **No está garantizado un valor de lpm cada segundo**, así que el cálculo de las características debe tolerar muestras irregulares.
- El **Samsung Health Sensor SDK** sí ofrece IBI (0 a 4 valores por evento de pulso) en el Galaxy Watch4 y posteriores. No es una opción para stateAI:
  - no funciona en el emulador y requiere un Galaxy Watch físico;
  - solo es compatible con Wear OS de Samsung;
  - la distribución pública requiere aprobación a través del Samsung Partner Program.
- El movimiento viene del acelerómetro a través de `SensorManager`, disponible en todos los relojes y en el emulador.

### Consecuencia: un "RMSSD" calculado con lpm a 1 Hz no es VFC

El RMSSD necesita intervalos latido a latido. Aplicar la misma fórmula a muestras de lpm mide cuánto se mueve el pulso promediado entre segundos. Por eso stateAI usa un **indicador aproximado de variabilidad del pulso** (desviación estándar y diferencias sucesivas de los lpm dentro de una ventana) y siempre lo llama aproximación, nunca VFC.

## 2. ¿Cómo se inyectan datos sintéticos en el emulador?

Hay dos capas, y stateAI usa ambas con fines distintos:

1. **Simulador propio (principal).** `SimulatedSensorSource`, detrás de la interfaz `SensorSource`, reproduce escenarios programados (foco profundo, sobrecarga, fatiga, mixto) a 1 Hz. Es determinista, se puede probar en la JVM y no depende del emulador. Es lo que usan el motor de reglas, los tests y la demo.
2. **Datos sintéticos de Health Services (comprobación del adaptador).** Solo se usan para verificar que `HealthServicesSensorSource` compila, se conecta y recibe valores:
   - **Wear OS 4+**: el panel de sensores *Wear Health Services* del emulador (Android Studio) permite activar capacidades y sobrescribir métricas como el pulso, y luego *Apply*. No hacen falta comandos de adb.
   - **Wear OS 3**: se activan los proveedores sintéticos por adb:

     ```sh
     adb shell am broadcast -a "whs.USE_SYNTHETIC_PROVIDERS" com.google.android.wearable.healthservices
     adb shell am broadcast -a "whs.synthetic.user.START_EXERCISE" \
         --ei exercise_options_heart_rate 90 com.google.android.wearable.healthservices
     adb shell am broadcast -a "whs.synthetic.user.STOP_EXERCISE" com.google.android.wearable.healthservices
     adb shell am broadcast -a "whs.USE_SENSOR_PROVIDERS" com.google.android.wearable.healthservices
     ```
   - La documentación describe los datos sintéticos en términos de **ejercicios (`ExerciseClient`)**, pero **se verificó el 2026-10-04** que en el emulador de Wear OS 6 `MeasureClient` también recibe valores sintéticos de pulso (entre 105 y 140 lpm por defecto) sin ningún comando de adb.
   - Los valores del acelerómetro se pueden cambiar en *Extended controls → Virtual sensors* del emulador.

## Decisión (SPEC 6.4.1)

No hay RR ni VFC reales, así que **el dominio usa 3 niveles de activación** en lugar de 4 estados:

| Nivel | Significado (respecto a la línea base personal) | Sustituye a |
|---|---|---|
| `LOW` | Pulso cerca de la línea base, poco cambio en el indicador de variabilidad, poco movimiento | `DEEP_FOCUS` (durante una sesión) |
| `MEDIUM` | Pulso moderadamente elevado o señales mixtas | `NORMAL` |
| `HIGH` | Pulso sostenido por encima de la línea base | `OVERLOADED` |

La fatiga (antes `EXHAUSTED`) no se puede inferir de la VFC. Pasa a ser una **marca de inquietud** independiente: muchos movimientos bruscos tras mucho tiempo en sesión. La interfaz muestra nombres amables en español para los niveles.

## Fuentes

- [Compatibilidad de Health Services (muestreo del pulso)](https://developer.android.com/health-and-fitness/health-services/compatibility)
- [Notas de versión del cliente de Health Services](https://developer.android.com/jetpack/androidx/releases/health)
- [Referencia de la API `DataType`](https://developer.android.com/reference/androidx/health/services/client/data/DataType)
- [Simular datos de sensores con Health Services](https://developer.android.com/health-and-fitness/health-services/simulated-data)
- [Descripción del Samsung Health Sensor SDK](https://developer.samsung.com/health/sensor/overview.html)
- [Preguntas frecuentes del Samsung Health Sensor SDK (emulador, alianza)](https://developer.samsung.com/health/sensor/faq.html)
