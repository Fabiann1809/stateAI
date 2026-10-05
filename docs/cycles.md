# Detección de ciclos de foco

stateAI no asume ciclos de 90 minutos. Busca un ritmo en el foco de la persona y solo lo informa
cuando la evidencia es fuerte; si no, dice "sin patrón claro" (SPEC 7.4).

## Método (`core-domain`, paquete `cycles`)

1. **Serie.** Cada ventana utilizable de cada segmento se convierte en una observación en su minuto
   absoluto: 1 cuando la estimación es `LOW` (foco) y 0 en otro caso. Se descartan las ventanas sin
   estimación y las que quedan a ±2 minutos de un aviso que la app hizo vibrar.
2. **Quitar los efectos ligados a la sesión.** A cada observación se le resta el foco medio en ese
   minuto desde el inicio de la sesión, promediado sobre todas las sesiones. Así desaparecen el
   calentamiento, el aviso de fin de bloque y cualquier otro efecto propio de la sesión. Esto importa:
   cuando las sesiones empiezan cada día a horas parecidas, una caída ligada a la sesión (por ejemplo,
   tras la vibración de fin de bloque) se alinea con el reloj y parece un ciclo.
3. **Periodograma.** Periodograma de Schuster sobre periodos de 40 a 150 minutos, evaluado en la serie
   con muestreo irregular (sesiones con huecos entre ellas). Las sumas por sesión se precalculan.
4. **Significancia por permutación.** Cada sesión se desplaza 1.000 veces a una hora aleatoria. Los
   desplazamientos conservan la forma interna de cada sesión pero rompen cualquier ritmo compartido
   entre sesiones, de modo que solo un ritmo de la persona (independiente de cuándo empiezan las
   sesiones) supera a la hipótesis nula. El valor p es la fracción de permutaciones cuyo pico más alto
   alcanza al observado.
5. **Salvaguardas.** Un periodo solo se informa si p ≤ 0,01, si la primera y la segunda mitad del
   historial encuentran el mismo pico (±10 %, descarta alias de un horario diario regular) y si no
   coincide con el bloque planificado típico (±10 %, el ritmo de la propia app). Además necesita al
   menos 6 sesiones y 300 ventanas utilizables.

## Resultados con usuarios sintéticos simples (Fase 5)

Usuarios sintéticos de los tests unitarios (`SyntheticFocusUser`): tres semanas, cuatro sesiones al día
de 50 a 80 minutos a horas con variación, probabilidad de foco `0,5 + 0,35 cos(2πt/P + φ_día)` con una
fase nueva cada día, y foco muestreado de forma independiente cada minuto.

| Usuario | Resultado con semillas fijas |
|---|---|
| Ciclo de 60 min | detectado en 8 de 10, todos dentro de ±10 % |
| Ciclo de 90 min | detectado en 9 de 10, todos dentro de ±10 % |
| Ciclo de 110 min | detectado en 6 de 10, todos dentro de ±10 % |
| Sin ciclo (tramos de foco aleatorios y persistentes) | 0 detecciones falsas en 20 |
| Sin ciclo, el foco cae tras el aviso de fin de bloque | 0 detecciones falsas en 5 |

Antes de añadir la salvaguarda de las dos mitades, un usuario de 60 minutos se informó con un periodo
equivocado (93 min) con p = 0,006, un alias del horario diario; la salvaguarda lo eliminó.

## Informe de detección con usuarios realistas (T-8.4)

Esta segunda prueba usa los usuarios del generador de estado latente de `ml-python` (Fase 6), que son
más realistas que los anteriores: el estado tiene inercia de minuto a minuto, hay fatiga dentro de la
sesión, ruido del sensor, minutos sin pulso, caminatas y sesiones de 20 a 60 minutos según la
actividad. Hay cinco tipos (ciclo de 60, 90 y 110 minutos, sin ciclo y un ciclo irregular que cambia
cada día entre 75 y 115 minutos), con tres semillas cada uno: 15 usuarios de 21 días.

El detector es el **código real de la app**, alimentado de dos formas:

- **Traza del reloj:** lo que el reloj guardaría. Señal → ventanas de 3 minutos → reglas contra la
  línea base calibrada → suavizado; las ventanas no utilizables quedan sin dato.
- **Traza ideal:** el estado latente verdadero de cada minuto, como si la clasificación fuera perfecta.
  Sirve para separar los límites del detector de los de la clasificación.

Cómo reproducirlo:

```sh
cd ml-python && python -m stateai_ml.cycle_traces    # escribe shared/cycles/users.csv y traces.csv
./gradlew :core-domain:test --tests "*CycleDetectionReportTest"   # imprime la tabla
```

| Usuario | Ritmo real | Días | Traza del reloj (reglas) | Traza ideal (estado real) |
|---|---|---|---|---|
| cycle60-s60 | 60 min | 21 | sin patrón claro | sin patrón claro |
| cycle60-s60 | 60 min | 14 | sin patrón claro | sin patrón claro |
| cycle60-s1060 | 60 min | 21 | sin patrón claro | **88,0 min (+46,6 %), p = 0,008** |
| cycle60-s1060 | 60 min | 14 | sin patrón claro | **88,0 min (+46,6 %), p = 0,006** |
| cycle60-s2060 | 60 min | 21 | sin patrón claro | sin patrón claro |
| cycle60-s2060 | 60 min | 14 | sin patrón claro | sin patrón claro |
| cycle90-s90 | 90 min | 21 | sin patrón claro | sin patrón claro |
| cycle90-s90 | 90 min | 14 | sin patrón claro | 88,0 min (−2,2 %), p = 0,005 |
| cycle90-s1090 | 90 min | 21 | sin patrón claro | sin patrón claro |
| cycle90-s1090 | 90 min | 14 | sin patrón claro | sin patrón claro |
| cycle90-s2090 | 90 min | 21 | sin patrón claro | sin patrón claro |
| cycle90-s2090 | 90 min | 14 | sin patrón claro | sin patrón claro |
| cycle110-s110 | 110 min | 21 | sin patrón claro | sin patrón claro |
| cycle110-s110 | 110 min | 14 | sin patrón claro | sin patrón claro |
| cycle110-s1110 | 110 min | 21 | sin patrón claro | sin patrón claro |
| cycle110-s1110 | 110 min | 14 | sin patrón claro | 116,3 min (+5,7 %), p = 0,003 |
| cycle110-s2110 | 110 min | 21 | 110,4 min (+0,3 %), p = 0,001 | 110,4 min (+0,3 %), p = 0,001 |
| cycle110-s2110 | 110 min | 14 | sin patrón claro | sin patrón claro |
| no_cycle (3 semillas) | ninguno | 21 y 14 | sin patrón claro (6 de 6) | sin patrón claro (6 de 6) |
| irregular (3 semillas) | 95 ± 20 min | 21 y 14 | sin patrón claro (6 de 6) | sin patrón claro (6 de 6) |

Todos los "sin patrón claro" de la tabla son por falta de significancia (p > 0,01); ninguno por falta
de datos.

### Qué muestra

- **Con la traza del reloj, el ciclo casi nunca se detecta:** 1 de 9 usuarios con ciclo en 3 semanas
  (110 min, con un error de +0,3 %) y 0 de 9 en 2 semanas. Cuando lo detecta, la duración es correcta.
- **Nunca inventa un ciclo** donde no lo hay: 0 detecciones en los 6 casos sin ciclo, con ambas
  trazas. Tampoco encuentra el ciclo irregular, lo que es razonable porque cambia cada día.
- **Ni siquiera con clasificación perfecta detecta de forma fiable:** 5 detecciones en 18 casos con
  ciclo. Dos de ellas, del mismo usuario con 21 y 14 días, son un **periodo equivocado** (60 min real,
  88 min informado, con p = 0,008 y 0,006) que las salvaguardas no detectaron. Con datos realistas, el
  método puede dar un resultado incorrecto con apariencia de seguro.
- **Las detecciones son frágiles:** varias aparecen con 14 días y desaparecen con 21, con valores p
  cerca del umbral de 0,01.
- **La clasificación del reloj empeora las cosas:** las reglas marcan como foco entre el 57 % y el
  87 % de los minutos, porque con este generador un minuto "comprometido" sube el pulso unos 4,5 lpm,
  por debajo del umbral de 5 lpm de las reglas. La serie de foco se aplana y el ritmo se diluye. Es la
  misma limitación de las reglas registrada en la decisión 20.

### Por qué difiere de la Fase 5

Los usuarios de la Fase 5 tienen un ritmo fuerte y limpio: cada minuto se sortea de forma independiente
con una probabilidad que oscila ±0,35. En los usuarios latentes el ritmo solo actúa cuando el estado
cambia (en torno al 15 % de los minutos) y compite con la fatiga y la inercia del estado.

Se probó la hipótesis de que las sesiones cortas (20–60 min) fueran la causa, repitiendo la prueba con
sesiones de unos 80 minutos, y **se descartó**: los resultados fueron igual de pobres (0 de 18 con la
traza del reloj, 2 de 18 con la ideal, ambos dentro de ±10 %). La explicación más probable es que el
ritmo de este generador es débil frente al ruido de minuto a minuto, pero **no está verificado**.

### Conclusiones

- La detección de ciclos funciona de forma fiable solo con ritmos fuertes y limpios, como los de la
  Fase 5. Con señales parecidas a las de un reloj real, lo esperable es "sin patrón claro" en casi
  todos los casos, sobre todo con la clasificación por reglas.
- El valor más sólido del método hoy es que **no inventa ciclos**. Su mayor riesgo es el periodo
  equivocado con p bajo que apareció con la traza ideal.
- Mejoras posibles, no aplicadas: un umbral de significancia más estricto o una validación cruzada
  por semanas, una clasificación con más resolución (el modelo de la Fase 7 tiene un 85 % de acierto
  frente al 52 % de las reglas en estos datos) y más semanas de historial antes de mostrar un ciclo.

## Limitaciones

- Estos resultados vienen de datos simulados cuyo ritmo puso el generador. Muestran qué puede y qué no
  puede recuperar el detector con ritmos de esta fuerza; no dicen nada sobre si las personas reales
  tienen esos ritmos ni sobre lo fuertes que son.
- Con menos datos (dos semanas) la detección empeora en la Fase 5 y es frágil en el informe realista.
  Se espera que "sin patrón claro" sea la respuesta más común con usuarios reales, sobre todo al
  principio.
- Los tests de la Fase 5 tardan unos 45 segundos por las permutaciones, y el informe realista unos 40.
