# Wear OS emulator setup

stateAI is developed and tested without a physical watch. These steps reproduce the environment used by the project on Windows, macOS or Linux.

## Requirements

- JDK 21 (`java -version`).
- Android SDK with:
  - **Command-line tools** (`cmdline-tools/latest`),
  - **Platform** `android-37`,
  - **Emulator** and **platform-tools**,
  - **System image** `system-images;android-36;android-wear-signed;x86_64` (Wear OS 6).
- Hardware acceleration enabled (Windows Hypervisor Platform / HAXM on Windows, KVM on Linux).

## 1. Point Gradle to the SDK

Create `local.properties` in the repository root (it is git-ignored):

```properties
sdk.dir=C:/Users/<you>/AppData/Local/Android/Sdk
```

Use forward slashes on Windows. Alternatively set the `ANDROID_HOME` environment variable.

## 2. Install the Wear OS image

With Android Studio: *Settings → Languages & Frameworks → Android SDK → SDK Platforms*, enable *Show Package Details* and select the **Wear OS 6 Intel x86_64 Atom System Image** under Android 16 (API 36).

From the command line (quote the package on Windows):

```sh
sdkmanager "system-images;android-36;android-wear-signed;x86_64"
```

## 3. Create the virtual device

```sh
avdmanager create avd -n Wear_OS_Large_Round \
    -k "system-images;android-36;android-wear-signed;x86_64" \
    -d wearos_large_round
```

Or in Android Studio: *Device Manager → Create Virtual Device → Wear OS → Wear OS Large Round*.

## 4. Start the emulator

```sh
emulator -avd Wear_OS_Large_Round -no-boot-anim
adb wait-for-device
adb shell getprop sys.boot_completed   # prints 1 when ready
```

## 5. Build, install and launch

```sh
./gradlew :app-wear:installDebug
adb shell am start -n com.stateai/.MainActivity
```

The watch shows the home screen. Take a screenshot to check it without the emulator window:

```sh
adb exec-out screencap -p > screen.png
```

## Simulating sensors

- **App simulator (default)**: stateAI plays scripted heart rate and movement scenarios through `SimulatedSensorSource`; no emulator configuration is needed.
- **Health Services**: open the emulator's *Extended controls → Wear Health Services* panel to override heart rate and other metrics. See `docs/sensors.md` for the adb commands and limitations.
- **Accelerometer**: *Extended controls → Virtual sensors*.
- **Haptics**: the emulator does not vibrate. Haptic events are verified through logcat:

  ```sh
  adb logcat -s Haptics
  ```
