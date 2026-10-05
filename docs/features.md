# Definición de las características

Definiciones exactas de las características que se calculan para cada ventana. La implementación en
Kotlin (`core-domain`, paquete `features`) y la de Python (`ml-python/src/stateai_ml/features.py`)
deben coincidir con ellas. Ambas se prueban con los mismos casos de `shared/parity/feature_cases.json`
(`FeatureParityTest` en Kotlin, `test_features.py` en Python). Regenera el archivo con
`python -m stateai_ml.parity` solo cuando una definición cambie a propósito, y actualiza los dos lados.

## Ventana

- Una ventana cubre los últimos **180 s** de muestras y se calcula cada **60 s**.
- Las muestras llegan aproximadamente una vez por segundo. Una muestra puede no tener pulso (`null`).

## Características

| Característica | Definición |
|---|---|
| `sampleCount` | Número de muestras de la ventana. |
| `heartRateCount` | Número de muestras con pulso. |
| `meanHeartRate` | Media aritmética de los pulsos disponibles (lpm). No está definida cuando `heartRateCount` es 0. |
| `heartRateStdDev` | Desviación estándar poblacional de los pulsos disponibles. 0 cuando hay menos de 2 valores. |
| `heartRateMeanAbsDiff` | Media de `abs(hr[i] - hr[i-1])` sobre muestras consecutivas en las que **ambas** tienen pulso. 0 cuando no hay ningún par así. Es el indicador aproximado de variabilidad del pulso; **no** es RMSSD. |
| `meanMovement` | Media aritmética de `movement` (m/s²) sobre todas las muestras. |
| `fidgetCount` | Número de veces que `movement` sube por encima de **1,5 m/s²** desde una muestra igual o inferior (una ráfaga de segundos altos consecutivos cuenta una sola vez; una ventana que empieza por encima del umbral cuenta un movimiento brusco). |
| `highMovementShare` | Fracción de muestras con `movement` por encima de **1,0 m/s²** (actividad sostenida, como caminar). |

## Ventanas limpias

Una ventana está **limpia** cuando `highMovementShare` es menor que **0,3** y `heartRateCount` es al
menos el **50 %** de `sampleCount`. Las ventanas que no están limpias no aportan evidencia fiable sobre
el estado de la persona: el clasificador mantiene el nivel anterior en lugar de reaccionar a ellas.
