# Guion de la demo

Cómo mostrar stateAI de principio a fin en el emulador, con 14 días de historial simulado para que se
vea lo que la app aprende. **Todos los datos de la demo son simulados**: el historial lo genera la app
(`com.stateai.demo`) y los sensores son el simulador.

## Preparación (una vez)

1. Arranca el emulador e instala la versión de depuración (ver `emulator-setup.md`):

   ```sh
   ./gradlew :app-wear:installDebug
   adb shell pm clear com.stateai          # opcional: empezar desde cero
   adb shell am start -n com.stateai/.MainActivity
   ```

2. La primera vez la mascota pide un nombre ("Ponerle nombre" o "Ahora no").
3. **Cargar el historial:** mantén pulsada la mascota → *Depuración* → **Cargar demo (14 días)**. Aparece
   "Demo cargada ✓". Se crean las actividades Tesis, Bases de datos, Novela y Reuniones, con 56 sesiones
   de los 14 días anteriores a hoy. Cada sesión pasa por los mismos aprendizajes que una sesión real
   (línea base y perfil por actividad). Cargarla dos veces no duplica nada.
4. **Elegir el escenario de sensores:** en *Depuración* → *Sensores simulados*. "Sesión mixta" recorre
   foco, sobrecarga, recuperación e inquietud en 30 minutos; "Sobrecarga" muestra antes la alerta.

La rutina simulada es: Tesis a las 9:00, Bases de datos a las 11:30 y a las 18:00, y a las 15:00
Reuniones (lunes, miércoles y viernes) o la Novela. La tarjeta "Sugerida" aparece a esas horas. Ojo:
el emulador suele usar la hora GMT, que puede no coincidir con la tuya.

## Recorrido (unos 5 minutos)

1. **Inicio.** La mascota con su nombre y "Toca y dime qué vas a hacer". Debajo, la sugerida, las
   actividades, "Nueva", "Resumen del día" y la energía estimada.
2. **Voz.** Toca la mascota. En el emulador no hay reconocedor de voz, así que se abre el teclado:
   escribe, por ejemplo, `voy a estudiar bases de datos`. La mascota pasa a "pensando" y luego aparece
   "¿Empezamos? Estudio: Bases de datos" con una cuenta atrás de 3 s. Prueba también "Cambiar" o
   "Cancelar", o una frase que no entienda (por ejemplo `hola`) para ver la vuelta a la lista.
3. **Sesión.** Cronómetro, anillo de progreso, estado en vivo y silueta de energía. Tras unos 3 minutos
   aparece el primer estado ("Enfocado"); con "Sesión mixta" cambia a Normal y a Sobrecarga hacia el
   minuto 16-21. Desliza a la izquierda para ver la página de **Energía**.
4. **Pausa guiada.** Botón de pausa → círculo de respiración (inhala 4 s, exhala 6 s) → "Volver".
5. **Terminar.** Botón ✓ → "¿Cómo te sentiste?" (o "Omitir") → **resumen de la sesión**: anillo de
   minutos (S1) y tiempo por estado y pausas (S2) → "Listo".
6. **Resumen del día.** Siete páginas: puntuación (D1), factores con la variación respecto a ayer (D2),
   energía (D3), estados del día (D4), ciclo de foco (D5), la semana (D6) y el foco por hora con la
   mejor franja (D7).

## Qué esperar del historial simulado

- **La semana** (D6) muestra los días anteriores con barras y hoy vacío hasta que hagas una sesión.
- **Foco por hora** (D7) muestra la mejor franja solo si la respaldan al menos 3 días.
- **El ciclo de foco** (D5) dirá casi seguro "Sin patrón claro". Es coherente con `cycles.md`: con 14
  días y señales de este tipo, el detector no encuentra un ciclo, y no se forzó la demo para que lo
  hiciera.
- La vibración no se siente en el emulador; los eventos hápticos se ven con `adb logcat -s Haptics`.
