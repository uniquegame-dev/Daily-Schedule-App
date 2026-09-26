# Daily Schedule

An offline Android task manager built with Kotlin, Jetpack Compose, Room, StateFlow, and `AlarmManager`. Tasks, settings, and completion history stay on the device; the app does not require a network connection.

## Requirements

- Android Studio with JDK 17 support
- Android SDK 36
- Minimum device API 24

## Build and verify

On Windows:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat compileDebugAndroidTestKotlin
```

The debug build uses Android's normal generated debug signing key. Release signing expects `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD` environment variables and an `upload` key alias.

## Notifications

Android 13 and later require notification permission. Exact reminder timing may also require the Alarms & reminders system permission. The app falls back to an inexact alarm when exact alarms are unavailable and reschedules future reminders after reboot, app replacement, clock/date/timezone changes, or an exact-alarm permission change.

Room schema snapshots are stored in `app/schemas`. Migration coverage lives in the Android instrumentation tests and should be run on an emulator/device before release.
