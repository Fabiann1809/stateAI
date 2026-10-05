# Configuración del emulador de Wear OS

stateAI se desarrolla y se prueba sin un reloj físico. Estos pasos reproducen el entorno que usa el proyecto en Windows, macOS o Linux.

## Requisitos

- JDK 21 (`java -version`).
- Android SDK con:
  - **Command-line tools** (`cmdline-tools/latest`),
  - **Plataforma** `android-37`,
  - **Emulator** y **platform-tools**,
  - **Imagen del sistema** `system-images;android-36;android-wear-signed;x86_64` (Wear OS 6).
- Aceleración por hardware activada (Windows Hypervisor Platform / HAXM en Windows, KVM en Linux).

## 1. Indicar a Gradle dónde está el SDK

Crea `local.properties` en la raíz del repositorio (git lo ignora):

```properties
sdk.dir=C:/Users/<tu-usuario>/AppData/Local/Android/Sdk
```

En Windows usa barras normales. También puedes definir la variable de entorno `ANDROID_HOME`.

## 2. Instalar la imagen de Wear OS

Con Android Studio: *Settings → Languages & Frameworks → Android SDK → SDK Platforms*, activa *Show Package Details* y selecciona **Wear OS 6 Intel x86_64 Atom System Image** dentro de Android 16 (API 36).

Desde la línea de comandos (en Windows, pon el paquete entre comillas):

```sh
sdkmanager "system-images;android-36;android-wear-signed;x86_64"
```

## 3. Crear el dispositivo virtual

```sh
avdmanager create avd -n Wear_OS_Large_Round \
    -k "system-images;android-36;android-wear-signed;x86_64" \
    -d wearos_large_round
```

O en Android Studio: *Device Manager → Create Virtual Device → Wear OS → Wear OS Large Round*.

## 4. Arrancar el emulador

```sh
emulator -avd Wear_OS_Large_Round -no-boot-anim
adb wait-for-device
adb shell getprop sys.boot_completed   # imprime 1 cuando está listo
```

## 5. Compilar, instalar y abrir

```sh
./gradlew :app-wear:installDebug
adb shell am start -n com.stateai/.MainActivity
```

El reloj muestra la pantalla de Inicio. Para comprobarla sin la ventana del emulador, haz una captura:

```sh
adb exec-out screencap -p > screen.png
```

## Simular sensores

Por defecto la app usa sensores simulados. Para compilarla con el adaptador real de Health Services:

```sh
./gradlew :app-wear:installDebug -Pstateai.sensorSource=health
```

Con sensores reales, la app pide el permiso de pulso al empezar una sesión. Para concederlo por adb:

```sh
adb shell pm grant com.stateai android.permission.health.READ_HEART_RATE
```

- **Simulador de la app (por defecto)**: stateAI reproduce escenarios programados de pulso y movimiento con `SimulatedSensorSource`; no hace falta configurar el emulador.
- **Health Services**: abre el panel *Extended controls → Wear Health Services* del emulador para sobrescribir el pulso y otras métricas. Consulta `docs/sensors.md` para los comandos de adb y las limitaciones.
- **Acelerómetro**: *Extended controls → Virtual sensors*.
- **Hápticos**: el emulador no vibra. Los eventos hápticos se comprueban en logcat:

  ```sh
  adb logcat -s Haptics
  ```
