# stateAI

> Entrenador de foco para relojes inteligentes (Wear OS) que aprende en qué momentos del día rindes mejor y te dice cuánta energía estimada te queda para aprovecharlos. Todo funciona en el reloj, sin teléfono y sin nube.

Este documento es la **fuente de verdad del proyecto**. Está pensado para ejecutarse **tarea por tarea** (ver la sección 12).

---

## 1. Resumen

**Nombre**: stateAI (nombres anteriores: "NeuroFocus" y "FlowState AI"). Usa siempre **stateAI** en el código, los paquetes, el README y la interfaz.

**Problema**: Las apps de productividad tradicionales (Pomodoro) son estáticas: usan tiempos fijos y no tienen en cuenta si la persona está agotada, sobrecargada o concentrada.

**Solución**: Una app solo para reloj que:
1. Pregunta al empezar "¿qué vas a hacer?": eliges una actividad reciente o creas una nueva (nombre libre opcional sobre una de 5 categorías base). Cada actividad aprende su propio perfil con el tiempo.
2. Estima el nivel de activación de la persona a partir del pulso, un indicador aproximado de variabilidad del pulso y el movimiento (la VFC real no está disponible, ver `docs/sensors.md`).
3. Aprende con los días en qué franjas horarias y con qué ciclos rinde mejor la persona.
4. Avisa con **micropulsos hápticos** cuándo respirar o hacer una pausa, sin necesidad de mirar la pantalla.
5. Muestra una **batería de energía estimada** y una puntuación diaria.

**Declaración del producto (versión honesta)**:
> stateAI aprende en qué momentos del día rindes mejor y te dice cuánta energía estimada te queda para aprovecharlos.

**Decisión de jerarquía**: aprender las ventanas de foco es el producto principal. El presupuesto de energía es **una función dentro de él**, un único indicador derivado de los mismos datos, no un segundo modelo.

---

## 2. Restricciones y principios (no negociables)

- **Solo reloj** (Wear OS). Sin app de teléfono, sin backend, sin cuentas, sin nube.
- **Todo local**: los datos, el aprendizaje y la inferencia viven en el reloj.
- **Sin reloj físico**: el desarrollo y las pruebas usan un **emulador**. Los sensores están detrás de una interfaz y se simulan.
- **Honestidad científica**: la app **estima** el estado fisiológico (carga y recuperación). No mide la productividad real y no es un dispositivo médico. Evitar lenguaje clínico ("neuro", "diagnóstico").
- **No asumir ciclos de 90 minutos**: la app *descubre* si la persona tiene ciclos y de qué duración; si no hay un patrón claro, lo dice.
- **No interrumpir el foco**: ninguna vibración durante el foco profundo, salvo fatiga o sobrecarga sostenidas.
- **Recomendar, no imponer**: sin bloqueos ni descansos "obligatorios".
- **Primero el alcance mínimo**: reglas antes que modelo, modelo antes que aprendizaje de rutinas.
- **Validación**: con datos simulados, documentados con claridad en el README. La evaluación con datos simulados muestra que **el sistema funciona de extremo a extremo**, no que stateAI sea mejor que otra técnica con personas reales.
- **Verificar antes de construir**: la disponibilidad real de las señales (T-0.4) se resuelve **antes** de fijar características y estados. El modelo de estados se adapta a lo que el reloj puede medir, no al revés.

---

## 3. Stack

**Reloj (Kotlin)**
- Jetpack Compose para Wear OS (interfaz)
- Health Services API (pulso, solo lpm) y SensorManager (acelerómetro), siempre detrás de `SensorSource`
- `Vibrator` / `VibrationEffect` con formas de onda (hápticos)
- Servicio en primer plano + Ongoing Activity (sesiones largas)
- Room (resúmenes de segmentos) y DataStore (ajustes y líneas base)
- Corrutinas + Flow
- MVVM, módulos separados, Hilt opcional
- Tests: JUnit para el módulo de dominio (sin Android)

**Offline (Python)**
- scikit-learn o TensorFlow para el clasificador de estados
- Exportación a **TensorFlow Lite** (su valor principal es demostrar el flujo Python → TFLite → Kotlin con paridad verificada; entrenado con datos sintéticos no se espera que supere al motor de reglas)
- Generador de semanas sintéticas
- Notebooks de evaluación

---

## 4. Arquitectura

Módulos de Gradle:

| Módulo | Contenido | Depende de Android |
|---|---|---|
| `:core-domain` | Modelos, motor de estados por reglas, aprendizaje, energía, puntuación | **No** (Kotlin puro) |
| `:sensors` | `SimulatedSensorSource`, `HealthServicesSensorSource` (la interfaz `SensorSource` vive en `:core-domain`) | Sí |
| `:haptics` | Patrones y limitador de frecuencia | Sí |
| `:data` | Room + DataStore | Sí |
| `:ml` | Envoltorio de TFLite | Sí |
| `:app-wear` | Interfaz en Compose, ViewModels, servicio de sesión | Sí |

Regla: `:core-domain` **no importa nada de Android**, para poder probarlo con tests en la JVM.

Regla de datos: el dominio solo conoce **interfaces de repositorio** (implementadas en `:data`). Room y DataStore son implementaciones intercambiables. El MVP es 100 % local, pero se podría añadir exportación, un teléfono o sincronización sin reescribir la lógica.

Carpeta aparte en el repositorio: `/ml-python` (entrenamiento, datos sintéticos, evaluación).

---

## 5. Modelo de dominio

- **ActivityCategory**: 5 categorías base: `DEEP_WORK`, `STUDY`, `READING`, `COLLAB` (reuniones/colaborativo), `OTHER`. Es la única definición; el resto del documento se remite a ella.
- **Activity**: nombre libre normalizado opcional + categoría + estado (`ACTIVE` / `ARCHIVED`). Sin nombre, la actividad es la propia categoría.
- **CategoryProfile**: valores por defecto de una categoría (duración del bloque objetivo, umbrales de estado, movimiento "normal", máximo de vibraciones por hora).
- **ActivityProfile**: los mismos parámetros, pero **aprendidos** para una actividad concreta y mezclados con los de su categoría según el número de sesiones (ver 6.7).
- **SensorSample**: marca de tiempo, pulso (lpm), magnitud del movimiento. Sin intervalos RR (Health Services no los ofrece).
- **FeatureWindow**: ventana de ~3 min calculada cada 60 s: pulso medio, indicador aproximado de variabilidad del pulso (desviación estándar y diferencia sucesiva media de los lpm), movimiento medio, número de movimientos bruscos.
- **ActivationLevel**: `LOW`, `MEDIUM`, `HIGH` (decidido en 6.4.1). `LOW` durante una sesión cuenta como tiempo de foco.
- **StateEstimate**: nivel de activación + marca de inquietud (muchos movimientos bruscos tras mucho tiempo en sesión; sustituye al antiguo estado "agotado").
- **Segment**: una actividad continua dentro del día (inicio, fin, actividad con su categoría y su nombre normalizado, tiempo por nivel de activación, tiempo con inquietud, puntuación, feedback).
- **DayRecord**: lista de segmentos, puntuación diaria, energía final.
- **UserBaseline**: pulso en reposo e indicador de variabilidad **por persona**, opcionalmente por franja horaria (medias móviles). Lo que se aprende por actividad son los umbrales y parámetros del perfil, no la línea base en reposo.
- **FocusProfile**: perfil de foco por franja horaria y ciclo detectado (si lo hay).
- **EnergyBudget**: valor de 0 a 100, capacidad diaria, consumo y recuperación.

---

## 6. Reglas del producto

### 6.1 Flujo de pantallas (máximo 4)
1. **Selector de actividad**: primero la actividad sugerida por la rutina, después las recientes (máx. 10 activas) y la opción "Nueva" (categoría base + nombre opcional con la entrada de texto estándar de Wear OS). Un toque para empezar.
2. **Sesión**: tiempo, icono/color del estado, batería de energía. Modo ambiente casi vacío.
3. **Pausa guiada**: un círculo que se expande y se contrae al ritmo de la respiración, sincronizado con la vibración.
4. **Resumen diario**: puntuación, línea de tiempo corta, mejor franja horaria, batería final.

Al terminar un segmento: feedback de un toque "¿cómo te sentiste?" (bien / regular / mal).

### 6.2 Lenguaje háptico
| Evento | Patrón |
|---|---|
| Inicio de bloque / ventana de foco | 1 pulso corto |
| Respirar | pulsos lentos al ritmo inhalar 4 s / exhalar 6 s |
| Pausa sugerida | 1 pulso largo y suave |
| Alerta de sobrecarga | 2 pulsos cortos |

Reglas:
- Como máximo 1 vibración cada 5 minutos, sin contar los eventos de inicio.
- En foco (`LOW` durante una sesión): ninguna vibración, salvo inquietud sostenida o `HIGH` (3+ min).
- El límite por hora depende del `ActivityProfile`.

### 6.3 Perfiles de categoría (valores iniciales, configurables)
Son los valores por defecto de cada categoría base. Cada actividad los sustituye poco a poco por sus propios valores aprendidos (ver 6.7).

| Categoría | Bloque objetivo | Sensibilidad | Movimiento normal |
|---|---|---|---|
| Trabajo profundo | 60-90 min | baja a las interrupciones | bajo |
| Estudio | 25-50 min | media | bajo |
| Lectura | 30-45 min | alta | muy bajo |
| Reuniones / colaborativo | 30-60 min | media | medio-alto |
| Otra | 45 min | media | medio |

### 6.4 Motor de estados por reglas
- Se calcula cada 60 s sobre una `FeatureWindow` de ~3 min.
- Primera sesión: 2 minutos en reposo para fijar la línea base personal (una sola vez, no por actividad; se ajusta con el uso).
- Umbrales **relativos a la línea base de la persona**, no absolutos.
- `LOW`: pulso cerca de la línea base, indicador de variabilidad estable, poco movimiento.
- `MEDIUM`: pulso moderadamente elevado o señales mixtas.
- `HIGH`: pulso por encima de la línea base de forma sostenida.
- **Marca de inquietud**: muchos movimientos bruscos tras mucho tiempo en sesión (indicio de fatiga).
- Ventanas con mucho movimiento: bajar su peso o descartarlas.

### 6.4.1 Decisión: número de estados (resuelta)
Los 4 estados originales (`DEEP_FOCUS`, `NORMAL`, `OVERLOADED`, `EXHAUSTED`) suponían VFC real (intervalos RR). Health Services no ofrece intervalos RR (ver `docs/sensors.md`) y, solo con el pulso de muñeca, distinguir el foco profundo de lo normal es muy débil (la carga cognitiva mueve el pulso unos pocos lpm, lo mismo que el café, la postura o hablar).

**Decisión**: el dominio usa 3 niveles de activación (`LOW`, `MEDIUM`, `HIGH`) más una marca de inquietud. La interfaz los muestra con nombres amables en español. La energía, las ventanas de foco y la puntuación funcionan sobre esos niveles.

### 6.5 Energía (indicador simple)
- Valor inicial 100 al empezar el día; entrada opcional por la mañana para ajustarlo (no depende de datos de sueño).
- **Consume** según el tiempo en sesión por nivel de activación y mientras hay inquietud (tasas por nivel).
- **Se recupera** con pausas que de verdad mejoran el estado después.
- Capacidad diaria ajustada por el `FocusProfile` (franjas buenas o malas).
- Sugerencias, no bloqueos: "te queda poca energía; quizá convenga dejar esto para después".
- Se llama "energía estimada", nunca "energía biológica real".

### 6.6 Puntuación de 0 a 100 (por segmento y por día)
Pesos iniciales (ajustables):
- Tiempo en foco: **40 %**
- Recuperación (pausas hechas que mejoraron el estado): **25 %**
- Carga sostenible (penaliza el tiempo prolongado en `HIGH` o con inquietud): **20 %**
- Constancia (cumplir la duración planificada): **15 %**

La puntuación diaria es la media ponderada por la duración. Mostrarla siempre **desglosada**, no solo el número. Es un indicador de calidad del foco y de la carga, **no** del trabajo producido.

### 6.7 Actividades libres y aprendizaje jerárquico

Las personas pueden crear sus propias actividades. Cada una cuelga de una **categoría base**, así que nunca empieza de cero.

**Estructura**
- Categorías base: las 5 definidas en `ActivityCategory` (sección 5).
- Actividad = nombre libre opcional (normalizado: minúsculas, sin tildes, sin espacios de más) + categoría.

**Qué entra en el MVP y qué se pospone**
- MVP: categorías, actividades con nombre opcional (entrada de texto estándar), normalización del nombre, perfil aprendido con mezcla `n/(n+K)`, sesión válida, tope de 10 activas e indicador de "aprendiendo".
- Después del MVP (Fase 9): archivado automático, reactivación con historial, detección de nombres parecidos ("¿es la misma?") y dictado dedicado.
- Mientras no exista el archivado, al llegar a 10 actividades activas la opción "Nueva" se desactiva con un mensaje corto.

**Mezcla gradual categoría → actividad** (evita un cambio brusco de comportamiento):
```
own_weight = n / (n + K)
value = own_weight * activity_profile + (1 - own_weight) * category_profile
```
- `n` = sesiones válidas de esa actividad; `K = 5` (con n=0 usa un 100 % de la categoría; con n=5, mitad y mitad; con n=20, ~80 % propio).
- Se aplica a cada parámetro aprendido: bloque objetivo, umbrales y movimiento normal (la línea base en reposo es personal, no por actividad).

**Sesión válida**: dura al menos 10 min y al menos el 60 % de sus ventanas están "limpias" (no descartadas por exceso de movimiento). Una sesión cortada o muy ruidosa no suma a `n`.

**Límite y archivado**
- Máximo **10 actividades activas** (las del selector). El motivo es la usabilidad en una pantalla pequeña y evitar la dilución estadística, no el almacenamiento.
- Orden del selector: sugerida por la rutina, recientes, más usadas.
- *(Después del MVP)* Al crear la número 11 se **archiva** la usada hace más tiempo.
- *(Después del MVP)* Archivar **no borra el aprendizaje**: sus datos se conservan agregados en su categoría y, si la actividad se reactiva, vuelve con su historial.

**Contra la fragmentación de nombres**: en el MVP basta con normalizar ("BD" y "bd " son lo mismo). *(Después del MVP)* Al crear un nombre muy parecido a uno existente, preguntar con un toque "¿es la misma?" antes de duplicarla.

**Experiencia de uso**: mientras `n` sea bajo, mostrar en el resumen un indicador discreto de "aprendiendo", sin números, para no sugerir una precisión que aún no existe.

**Parámetros (todos constantes configurables)**

| Parámetro | Valor inicial |
|---|---|
| `K` (mezcla categoría/actividad) | 5 |
| Duración mínima de una sesión válida | 10 min |
| Porcentaje mínimo de ventanas limpias | 60 % |
| Máximo de actividades activas | 10 |
| Criterio de archivado (después del MVP) | usada hace más tiempo |
| Categorías base | 5 |
| Umbral para mostrar "aprendiendo" | n < 5 |

---

## 7. IA: qué aprende y cómo

1. **Clasificador de estados** (offline en Python, inferencia en el reloj con TFLite). Entrada: `FeatureWindow`; salida: `ActivationLevel`. El motor de reglas se mantiene como respaldo. Entrenado con datos sintéticos, el modelo básicamente vuelve a aprender las reglas del generador: se presenta como una **demostración del flujo** (entrenamiento, exportación, paridad Kotlin/Python, respaldo), no como una mejora de precisión.
2. **Línea base personal**: medias móviles exponenciales del pulso en reposo y del indicador de variabilidad, por persona y opcionalmente por franja horaria. Los parámetros del perfil (bloque objetivo, umbrales, movimiento normal) se **aprenden por actividad**, mezclados con los de su categoría según el número de sesiones (6.7).
3. **Ventanas de foco por franja horaria**: puntuación media de foco por hora del día y día de la semana.
4. **Detección de ciclos** (el núcleo de stateAI): buscar periodicidad en la serie de foco de la persona (autocorrelación o periodograma). Informar la duración del ciclo **solo con suficiente confianza**; si no, "sin patrón claro".
   - **Periodicidad provocada por la propia app**: la app vibra al alcanzar el bloque y sugiere pausas, lo que puede crear un ciclo artificial. Mitigación: marcar los eventos generados por la app, descartar o excluir de la serie los minutos de alrededor y comprobar que el ciclo detectado no coincide sin más con el bloque objetivo del perfil.
   - **Series con huecos**: las sesiones duran entre 25 y 90 min con pausas entre ellas. El método debe tolerar huecos (por ejemplo, el periodograma de Lomb-Scargle) y se espera que "sin patrón claro" sea una respuesta frecuente.
   - Método implementado y resultados con usuarios sintéticos: `docs/cycles.md`.
5. **Predicción de la rutina**: frecuencias por hora y día para sugerir la siguiente actividad en el selector.
6. **Personalización con el feedback**: "¿cómo te sentiste?" es una etiqueta de todo el segmento, no de cada ventana, así que no dice qué ventanas se clasificaron mal. Se usa para un **ajuste lento de la sensibilidad global de la actividad** (pasos pequeños y acotados), no para reajustar umbrales finos.
7. **Opcional (fase final)**: bandit contextual para decidir cuándo sugerir una pausa.

Fuera del MVP: LLM en el reloj, resúmenes en lenguaje natural mediante una API.

---

## 8. Datos y evaluación

- **Datos sintéticos**: generador de usuarios con rutinas de 2 a 3 semanas, con ciclos de duración conocida (por ejemplo, 60, 90 y 110 min y un usuario sin ciclo), ruido del sensor y variación de un día a otro.
- **Dataset público opcional** para el clasificador base (por ejemplo, WESAD, **revisar licencia y etiquetas**). Mide estrés, no productividad: documentarlo como una aproximación.
- **Evaluación de extremo a extremo**: simular el mismo día con (a) un temporizador fijo y (b) stateAI, y comparar el tiempo en foco, el tiempo en sobrecarga y las pausas hechas.
  - **Advertencia de circularidad**: si el simulador genera los estados con la misma lógica con la que stateAI los clasifica, stateAI gana por construcción. Para reducirlo, el generador modela la fisiología con un mecanismo **distinto** del motor de reglas (por ejemplo, un estado latente con sus propias transiciones y ruido), y el informe presenta el resultado como prueba de que el sistema integrado funciona, **no** como evidencia de superioridad.
- **Test de detección de ciclos**: ¿recupera la duración conocida? ¿Dice "sin patrón" cuando no lo hay?
- Limitación que hay que declarar: el aprendizaje se valida con datos simulados; no muestra la precisión con personas reales.

---

## 9. Alcance

**Dentro (MVP)**
- Actividades libres: 5 categorías base + nombre opcional, hasta 10 activas, con un perfil aprendido por actividad (6.7). El archivado, la reactivación y la detección de nombres parecidos quedan para después del MVP (Fase 9)
- Repositorios abstractos en `:data` y exportación manual de resúmenes (deja abierta la puerta a un teléfono o a la nube sin construirlos)
- Sesión con estado, hápticos y pausa guiada
- Sensores simulados con un panel de depuración
- Motor de estados por reglas y después modelo TFLite
- Segmentos, puntuación y resumen diario
- Línea base, ventanas por franja horaria, detección de ciclos, energía simple
- Generador sintético y evaluación comparativa

**Fuera del alcance**
- App de teléfono, nube, cuentas, sincronización
- Historial a largo plazo con gráficas
- Otros relojes (Apple Watch, etc.)
- Coste metabólico por tarea, datos reales de sueño
- LLM, diagnósticos o cualquier uso médico

---

## 10. Riesgos

- **VFC en Wear OS**: confirmado que Health Services no ofrece intervalos RR (el SDK de Samsung sí, pero necesita un Galaxy Watch físico y aprobación como socio). Un "RMSSD" calculado sobre el pulso en lpm a 1 Hz no es VFC: stateAI usa un indicador aproximado de variabilidad del pulso y lo llama así (ver `docs/sensors.md`).
- **Señales débiles**: el pulso de muñeca separa mal el foco de lo normal. Mitigación: 3 niveles de activación en lugar de 4 estados (6.4.1).
- **Periodicidad provocada por la propia app** en la detección de ciclos: ver la sección 7, punto 4.
- **Evaluación circular**: ver la sección 8.
- **Calibración frágil** (encontrada en la evaluación de ML, `ml-python/reports/classifier.md`): una calibración de 2 minutos registra el estado en que esté la persona; en un usuario sintético quedó 5 lpm alta y bajó el acierto de las reglas de 0,72 a 0,52. La media móvil de la línea base (T-5.1) solo lo corrige en parte. Posible solución: volver a comprobar la línea base con las ventanas de calma más bajas de las primeras sesiones.
- **Batería del reloj**: un servicio en primer plano con pulso continuo durante horas gasta notablemente la batería de un reloj real. Medir el consumo si se prueba en hardware (T-9.1) y considerar un muestreo intermitente fuera de las sesiones.
- **Señal de muñeca ruidosa** con movimiento: ponderar según el movimiento.
- **Datos sintéticos circulares**: el modelo puede "descubrir" lo que puso el generador. Declararlo.
- **Alcance grande para una sola persona**: respetar el orden de las fases y terminar cada una antes de la siguiente.
- **Emulador**: la vibración no se siente; verificarla con el registro y con un indicador visual.
- **Arranque en frío y fragmentación de actividades**: pocas sesiones por actividad no permiten aprender; se mitiga con la mezcla categoría/actividad, la línea base personal compartida, el tope de 10 activas y la normalización de nombres (6.7).

---

## 11. Tareas por fase

Convención: cada tarea es **corta** y tiene un criterio de "Hecho cuando". Marcarla con `[x]` al completarla.

### Fase 0: Preparación
**T-0.4 va primero**: su resultado decide las características y el número de estados (6.4.1). Las demás tareas de la fase pueden hacerse después.

- [x] **T-0.1** Crear el proyecto de Wear OS (Kotlin, Compose) con los módulos de la sección 4. *Hecho cuando:* compila y se ejecuta en el emulador mostrando una pantalla vacía.
- [x] **T-0.2** Configurar el emulador de Wear OS y documentar los pasos en `docs/emulator-setup.md`. *Hecho cuando:* otra persona puede reproducirlo.
- [x] **T-0.3** Crear la carpeta `/ml-python` con un entorno virtual y `requirements.txt`. *Hecho cuando:* `python -c "import sklearn"` funciona.
- [x] **T-0.4** Investigar si Health Services ofrece intervalos RR o VFC y cómo inyectar datos sintéticos en el emulador. Escribir las conclusiones en `docs/sensors.md`. *Hecho cuando:* el documento responde ambas preguntas con fuentes, registra la decisión de 6.4.1 (4 estados o 3 niveles) y este documento queda actualizado con ella.
- [x] **T-0.5** Crear el README con la sección "Limitaciones y honestidad científica" (secciones 2 y 10 de este documento). *Hecho cuando:* el README las incluye.

### Fase 1: Selector, sesión y hápticos básicos
- [x] **T-1.1** Definir `ActivityCategory` (5 base), `Activity` (nombre normalizado + categoría) y `CategoryProfile` con los valores de 6.3, en `:core-domain`. *Hecho cuando:* hay tests para los perfiles de categoría y para la normalización del nombre.
- [x] **T-1.2** Pantalla del selector: primero la sugerida, después las recientes (máx. 10 activas) y la opción "Nueva" (desactivada con un mensaje al llegar a 10). *Hecho cuando:* tocar una actividad lleva a la sesión.
- [x] **T-1.3** Pantalla de sesión con un temporizador basado en el perfil (tiempo transcurrido y objetivo). *Hecho cuando:* el tiempo avanza y sobrevive a la rotación y al modo ambiente.
- [x] **T-1.4** Servicio en primer plano + Ongoing Activity para la sesión. *Hecho cuando:* la sesión continúa con la pantalla apagada en el emulador.
- [x] **T-1.5** Módulo `:haptics` con los 4 patrones de 6.2. *Hecho cuando:* cada patrón se activa desde un botón de depuración y queda registrado.
- [x] **T-1.6** Limitador de frecuencia de vibración (máx. 1 cada 5 min, límite por hora según el perfil). *Hecho cuando:* los tests muestran que las vibraciones fuera de las reglas se bloquean.
- [x] **T-1.7** Vibración de fin de bloque al alcanzar el objetivo del perfil. *Hecho cuando:* se activa una vez cuando se cumple el tiempo.
- [x] **T-1.8** Flujo de "Nueva actividad": elegir categoría y nombre opcional (entrada de texto estándar de Wear OS). *Hecho cuando:* la actividad se crea, aparece en el selector, y "BD" y "bd " se unifican como la misma.

### Fase 2: Sensores y simulador
- [x] **T-2.1** Definir `SensorSample` y la interfaz `SensorSource` (flujo de muestras). *Hecho cuando:* compila y está documentada.
- [x] **T-2.2** Implementar `SimulatedSensorSource`, que reproduce escenarios programados. *Hecho cuando:* emite muestras a 1 Hz según el escenario.
- [x] **T-2.3** Escenarios: foco profundo, sobrecarga, fatiga y sesión mixta. *Hecho cuando:* cada uno tiene tests de la forma de la señal (tendencias del pulso y del movimiento).
- [x] **T-2.4** Panel de depuración para elegir un escenario y ver los valores en vivo (solo en versiones de depuración). *Hecho cuando:* cambiar el escenario cambia la señal en vivo.
- [x] **T-2.5** Implementar `HealthServicesSensorSource` (pulso y movimiento) como adaptador real, según lo encontrado en T-0.4. *Hecho cuando:* compila y se puede activar por configuración, aunque no se haya probado en un reloj real.

### Fase 3: Motor de estados por reglas
- [x] **T-3.1** Calcular `FeatureWindow` (pulso medio, indicador de variabilidad, movimiento, movimientos bruscos) a partir de las muestras. *Hecho cuando:* los tests con señales conocidas dan los valores esperados.
- [x] **T-3.2** Calibración de la línea base personal (2 min, una vez) y su persistencia. *Hecho cuando:* la línea base se guarda y se reutiliza en todas las actividades.
- [x] **T-3.3** Clasificador por reglas con umbrales relativos a la línea base (6.4). *Hecho cuando:* los escenarios simulados producen los niveles de activación y la marca de inquietud esperados.
- [x] **T-3.4** Bajar el peso o descartar las ventanas con mucho movimiento. *Hecho cuando:* un test muestra que el movimiento alto no provoca estados falsos.
- [x] **T-3.5** Suavizado del estado (histéresis, al menos 2 ventanas iguales para cambiar). *Hecho cuando:* no hay parpadeo de estado en el escenario mixto.
- [x] **T-3.6** Política háptica: en foco ninguna vibración salvo inquietud sostenida o `HIGH`; pausa sugerida al salir del foco. *Hecho cuando:* los tests cubren cada caso.
- [x] **T-3.7** Mostrar el nivel de activación en la pantalla de sesión (icono y color). *Hecho cuando:* cambia en vivo con el simulador.

### Fase 4: Segmentos, puntuación y resumen
- [x] **T-4.1** Modelos `Segment` y `DayRecord` (con categoría y nombre normalizado); cerrar un segmento cuando la actividad cambia o termina. *Hecho cuando:* hay tests del ciclo de vida del segmento.
- [x] **T-4.2** Persistencia de los segmentos con Room (solo resúmenes, sin señal cruda). *Hecho cuando:* los datos sobreviven a un reinicio de la app.
- [x] **T-4.3** Calcular la puntuación de 0 a 100 por segmento (6.6). *Hecho cuando:* hay tests con casos extremos (todo foco, todo `HIGH`).
- [x] **T-4.4** Calcular la puntuación diaria (media ponderada por la duración). *Hecho cuando:* hay un test con 3 segmentos de duraciones distintas.
- [x] **T-4.5** Feedback de un toque al cerrar un segmento, guardado. *Hecho cuando:* queda asociado al segmento.
- [x] **T-4.6** Pantalla de resumen diario con la puntuación **desglosada**. *Hecho cuando:* muestra los 4 componentes.
- [x] **T-4.7** Pausa guiada: pantalla de respiración sincronizada con la vibración (4 s / 6 s). *Hecho cuando:* el círculo y los hápticos mantienen el mismo ritmo.
- [x] **T-4.8** Interfaces de repositorio (segmentos, líneas base, perfiles); `:core-domain` solo conoce las interfaces. *Hecho cuando:* el dominio se prueba con repositorios en memoria.
- [x] **T-4.9** Exportar los resúmenes (segmentos y perfiles) a JSON o CSV desde el reloj, para analizarlos en Python. *Hecho cuando:* el archivo se genera y se lee desde `/ml-python`.

### Fase 5: Aprendizaje (en `:core-domain`)
- [x] **T-5.1** Línea base personal con media móvil exponencial (opcionalmente por franja horaria), actualizada tras cada sesión. *Hecho cuando:* un test muestra la convergencia con datos nuevos.
- [x] **T-5.2** `FocusProfile`: puntuación media de foco por franja horaria y día de la semana. *Hecho cuando:* hay tests con varios días de datos.
- [x] **T-5.3** Aviso de "ventana de foco" cuando la franja actual es buena según el perfil. *Hecho cuando:* solo se activa con una confianza mínima (por ejemplo, 5+ días).
- [x] **T-5.4** Detección de ciclos: construir la serie de foco y buscar periodicidad (autocorrelación o periodograma). *Hecho cuando:* recupera ciclos de 60/90/110 min en datos sintéticos con huecos entre sesiones, y un test confirma que las vibraciones de fin de bloque no crean un ciclo falso.
- [x] **T-5.5** Umbral de confianza: informar "sin patrón claro" cuando no hay periodicidad. *Hecho cuando:* un usuario sintético sin ciclo da "sin patrón".
- [x] **T-5.6** Predicción de la siguiente actividad por hora y día; preseleccionarla en el selector. *Hecho cuando:* con suficientes datos, el selector muestra primero la sugerida.
- [x] **T-5.7** Ajuste lento de la sensibilidad global de la actividad a partir del feedback "¿cómo te sentiste?" (pasos pequeños y acotados). *Hecho cuando:* un test muestra que la sensibilidad se mueve en la dirección correcta y que un único feedback no la cambia de golpe.
- [x] **T-5.8** Motor de energía (6.5): consumo por estado, recuperación por pausa útil, capacidad por franja horaria. *Hecho cuando:* hay tests de consumo y de recuperación.
- [x] **T-5.9** Mostrar la batería en la sesión y en el resumen, con sugerencias (nunca bloqueos). *Hecho cuando:* se ve y cambia durante una sesión simulada.
- [x] **T-5.10** Perfil aprendido por actividad con la mezcla `n / (n + K)` hacia el perfil de su categoría (6.7). *Hecho cuando:* los tests con n=0, 5 y 20 dan los pesos esperados.
- [x] **T-5.11** Criterio de sesión válida (≥ 10 min y ≥ 60 % de ventanas limpias). *Hecho cuando:* una sesión corta o ruidosa no aumenta `n`.
- [x] **T-5.12** Aprender el bloque objetivo y el movimiento normal de cada actividad a partir de sus sesiones. *Hecho cuando:* con sesiones simuladas el valor se acerca al del usuario sintético.
- [x] **T-5.13** Tope de 10 actividades activas (sin archivado automático en el MVP). *Hecho cuando:* un test confirma que no se puede crear la número 11.
- [x] **T-5.14** Indicador de "aprendiendo" mientras `n < 5`. *Hecho cuando:* aparece en el resumen con n bajo y desaparece después.

### Fase 6: ML offline (Python)
- [x] **T-6.1** Generador de usuarios sintéticos: rutina de varios días con ciclos conocidos y ruido. La fisiología se genera con un mecanismo **distinto** del motor de reglas (estado latente con sus propias transiciones), para no validar las reglas contra sí mismas. *Hecho cuando:* produce CSV reproducibles con una semilla y el README del generador explica en qué se diferencia del motor de reglas.
- [x] **T-6.2** Incluir un usuario sin ciclo y otro con un ciclo irregular. *Hecho cuando:* están en el dataset de evaluación.
- [ ] **T-6.3** (Opcional) Flujo para un dataset público de estrés (revisar licencia y etiquetas). *Hecho cuando:* produce el mismo formato de características que `FeatureWindow`.
- [x] **T-6.4** Ingeniería de características idéntica a la de Kotlin (misma definición de pulso medio, indicador de variabilidad, etc.). *Hecho cuando:* hay un test de paridad con valores de muestra compartidos.
- [x] **T-6.5** Entrenar un clasificador simple con los estados decididos en 6.4.1 y evaluarlo (matriz de confusión). *Hecho cuando:* hay un informe con métricas que lo compara con el motor de reglas y que indica que, con datos sintéticos, se espera un resultado parecido.
- [x] **T-6.6** Exportar a TFLite y verificar el modelo con un script. *Hecho cuando:* la inferencia en Python con el `.tflite` coincide con el modelo original.
- [x] **T-6.7** Incluir en el generador actividades personalizadas con pocas y muchas sesiones. *Hecho cuando:* el dataset permite probar la mezcla categoría/actividad.

### Fase 7: Integración del modelo
- [x] **T-7.1** Módulo `:ml` que carga el `.tflite` y clasifica una `FeatureWindow`. *Hecho cuando:* devuelve un `ActivationLevel` en el emulador.
- [x] **T-7.2** Interfaz `StateClassifier` con dos implementaciones: reglas y modelo. *Hecho cuando:* se puede elegir por configuración.
- [x] **T-7.3** Respaldo automático a las reglas si el modelo falla o su confianza es baja. *Hecho cuando:* un test con un modelo "roto" usa las reglas.
- [x] **T-7.4** Test de paridad Kotlin/Python con características de muestra. *Hecho cuando:* las predicciones coinciden dentro de la tolerancia.

### Fase 8: Evaluación y demo
- [x] **T-8.1** Script que simula un día con un temporizador fijo (referencia). *Hecho cuando:* produce métricas de tiempo en foco, sobrecarga y pausas.
- [x] **T-8.2** El mismo día con stateAI. *Hecho cuando:* produce las mismas métricas.
- [x] **T-8.3** Informe de extremo a extremo con gráficas en un notebook. *Hecho cuando:* el notebook está en el repositorio e indica que los datos son simulados, que el resultado muestra que el sistema integrado funciona y que **no** demuestra superioridad con personas reales.
- [x] **T-8.4** Informe de las pruebas de detección de ciclos (T-5.4 y T-5.5). *Hecho cuando:* hay una tabla de duración real frente a detectada.
- [ ] **T-8.5** Preparar la demo: usuario sintético de 14 días, panel de simulación y flujo completo. *Hecho cuando:* se puede mostrar de principio a fin en el emulador.
- [ ] **T-8.6** Capturas o un video corto para el portafolio. *Hecho cuando:* están en `/docs/media`.
- [ ] **T-8.7** Pulir el README: problema, arquitectura, decisiones, limitaciones, cómo ejecutarla. *Hecho cuando:* una persona ajena puede entenderlo en 5 minutos.

### Fase 9: Después del MVP / opcional
- [ ] **T-9.1** Probar con un reloj real, aunque sea prestado, y registrar de 3 a 5 sesiones, incluido el consumo de batería. *Hecho cuando:* hay notas sobre qué funcionó, qué no y cuánta batería gasta una sesión.
- [ ] **T-9.2** Bandit contextual para decidir cuándo sugerir pausas. *Hecho cuando:* mejora el tiempo en foco en simulación frente a las reglas fijas.
- [ ] **T-9.3** Archivado automático al crear la actividad número 11 (la usada hace más tiempo). *Hecho cuando:* crear la número 11 archiva la correcta.
- [ ] **T-9.4** Archivar sin perder el aprendizaje: agregarlo en la categoría y reactivar con historial. *Hecho cuando:* un test confirma que los datos se conservan tras archivar y reactivar.
- [ ] **T-9.5** Detección de nombres parecidos al crear ("¿es la misma?"). *Hecho cuando:* un nombre parecido a uno existente provoca la pregunta.
- [ ] **T-9.6** Dictado dedicado para nombrar actividades. *Hecho cuando:* se puede crear una actividad solo con la voz.

### Fase 10: Mascota y entrada por voz

Una mascota, una llama turquesa, abre la app. Al tocarla escucha una sola vez (nunca de forma
continua); la app interpreta qué va a hacer la persona, pide una confirmación breve y empieza la
sesión. La lista de actividades sigue en la misma pantalla como alternativa en silencio, y cualquier
fallo vuelve a ella. El audio nunca se guarda; el reconocedor del sistema (posiblemente en la nube)
solo se usa con consentimiento. La mascota solo cambia de expresión, nunca con la energía. El motor de
estados, la energía y la puntuación no se tocan.

- [x] **T-10.1** Importar las 5 expresiones como VectorDrawable y documentar sus nombres. *Hecho cuando:* los drawables compilan y `docs/mascot.md` los enumera.
- [x] **T-10.2** Componente `Mascot(expression)` con las 5 expresiones y previews redondas. *Hecho cuando:* cada expresión se ve como en la lámina del diseño.
- [x] **T-10.3** Animación sutil en reposo (balanceo de la punta), apagada en modo ambiente y con "quitar animaciones". *Hecho cuando:* se mueve en reposo y se detiene en ambos casos.
- [x] **T-10.4** Pantalla de Inicio: mascota con "Toca y dime qué vas a hacer" y, debajo, la sugerida y las recientes. *Hecho cuando:* es la pantalla de entrada y la lista sigue empezando sesiones.
- [x] **T-10.5** Nombre de la mascota elegido en el primer uso (DataStore). *Hecho cuando:* se pregunta una vez y se muestra después.
- [x] **T-10.6** Permiso RECORD_AUDIO con una explicación; si se rechaza, se usa la lista. *Hecho cuando:* se manejan ambas respuestas.
- [x] **T-10.7** `SpeechInput` con reconocedor en el dispositivo, reconocedor del sistema (con consentimiento) y entrada de texto para depuración/emulador, en español. *Hecho cuando:* se elige el adecuado en tiempo de ejecución.
- [x] **T-10.8** Flujo de escucha: toque, "escuchando" + vibración corta, resultado, "pensando"; tiempo límite si no hay voz. *Hecho cuando:* funciona con la entrada de texto en el emulador.
- [x] **T-10.9** `IntentParser` en `:core-domain`: categoría, nombre de la actividad y confianza, ignorando los saludos. *Hecho cuando:* pasan los tests con frases de ejemplo.
- [x] **T-10.10** Emparejar con actividades existentes (normalización y nombres parecidos); crearla si falta; con confianza baja, abrir "Nueva actividad" prellenada. *Hecho cuando:* los tests cubren nombres exactos, parecidos y nuevos.
- [x] **T-10.11** Tarjeta de confirmación "¿Empezamos ...?" con cuenta atrás de 3 s, Cambiar y Cancelar. *Hecho cuando:* confirmar empieza la sesión con el flujo existente.
- [x] **T-10.12** Errores (sin permiso, sin reconocedor, sin red, silencio, sin coincidencia): mensaje corto y vuelta a la lista. *Hecho cuando:* cada caso está probado.
- [x] **T-10.13** Privacidad: no se guarda audio; aviso y consentimiento antes del reconocedor del sistema; "Voz y privacidad" en el README. *Hecho cuando:* el consentimiento se pide una vez y está documentado.
- [ ] **T-10.14** (Opcional) La mascota en la pausa guiada y en el feedback.
- [x] **T-10.15** Tests: al menos 15 frases del parser, el emparejamiento y la política de vuelta a la lista. *Hecho cuando:* pasan en CI.

---

## 12. Reglas de trabajo

1. **Trabajar una tarea a la vez**, en el orden de las fases, empezando por **T-0.4**. No pasar a la siguiente fase sin cerrar la actual. La Fase 9 no se toca hasta terminar la Fase 8.
2. Antes de empezar una tarea, leerla junto con su "Hecho cuando" y esbozar un plan de 3 a 5 líneas.
3. **No ampliar el alcance**: nada de lo que está en "Fuera del alcance" (sección 9) se implementa.
4. Todo lo de `:core-domain` debe tener **tests unitarios** y no debe importar Android.
5. Mantener las funciones y clases pequeñas y con una sola responsabilidad. El código, los nombres de carpetas y los comentarios están en **inglés**; los textos que ve la persona en la interfaz están en **español** (recursos de texto). Los documentos de `docs/` están en **español**, conservando los identificadores, el código, las rutas y los números de tarea tal cual.
6. Cualquier umbral, peso o duración debe ser una **constante configurable**, no un número mágico.
7. Si una tarea requiere una decisión que este documento no cubre, **preguntar** antes de suponer.
8. Si una suposición técnica puede ser falsa (por ejemplo, si los datos sintéticos del emulador llegan a `MeasureClient`), **verificarla y documentarla** antes de construir sobre ella.
9. Al terminar una tarea: marcarla con `[x]` y hacer un commit Conventional Commit que describa lo hecho (sin números de tarea en el mensaje). Ver `CONTRIBUTING.md`.
10. Mantener un lenguaje de producto honesto: "estima", "energía estimada", "indicador de foco y carga". Nunca "mide tu cerebro" ni afirmaciones médicas.
