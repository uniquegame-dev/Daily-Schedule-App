# Daily Schedule

Daily Schedule is an offline Android task manager for planning a day, reviewing upcoming work, and checking past activity. It is built with Kotlin, Jetpack Compose, Room, ViewModel/StateFlow, `AlarmManager`, and local Android notifications.

## Features

- Create, edit, complete, and delete tasks with a date, time, notes, category, and optional reminder.
- Review tasks in Today, Upcoming, and History views.
- Search and filter history, browse dates on a calendar, and view an activity heatmap.
- Distinguish pending, completed, and missed tasks without automatically completing or deleting overdue items.
- Track today's completion progress and time progress from the dashboard.
- Show individual task reminders and an optional ongoing summary of today's pending tasks.
- Support light and dark themes.
- Store all task data locally with Room.

## Android requirements

- Minimum Android version: Android 7.0 (API 24)
- Target/compile SDK: Android 16 / API 36
- Development environment: Android Studio with JDK 17 and Android SDK 36 installed

## Offline behavior

The app does not require an account, internet connection, server, or cloud service. Tasks, completion history, and notification settings are stored on the device. Clearing the app's data or uninstalling it may remove this local data, subject to the device's Android backup settings.

## Permissions

- **Notifications** (`POST_NOTIFICATIONS`): required on Android 13 and later before reminders or the ongoing task summary can appear.
- **Alarms & reminders** (`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`): allows reminders to fire at the requested time where the Android version and device policy permit it.
- **Run after startup** (`RECEIVE_BOOT_COMPLETED`): lets the app restore future reminders after a reboot or app update.
- **Vibration** (`VIBRATE`): allows reminder notifications to vibrate according to system settings.

Task management remains usable if notification or exact-alarm access is denied; only reminder delivery or precision is affected.

## Reminder behavior

Reminders are scheduled locally with `AlarmManager` for the task time or the selected number of minutes beforehand. If exact alarms are unavailable, the app uses an inexact alarm. Reminder times that have already passed are not scheduled.

Future reminders are restored after reboot, app replacement, date/time or timezone changes, and exact-alarm permission changes. A reminder notification can open the app or mark its task complete. Android power management and manufacturer-specific battery restrictions can still delay alarms or notifications.

## Downloads

- [Latest GitHub Release](https://github.com/uniquegame-dev/Daily-Schedule-App/releases/latest)
- [Download the debug APK](https://github.com/uniquegame-dev/Daily-Schedule-App/releases/download/v1.0.0/app-debug.apk)

The downloadable APK is a debug build intended for evaluation and local testing. It is not production-signed and should not be distributed through an app store.

## Build a debug APK

Clone the repository, open it in Android Studio, allow Gradle sync to finish, and run:

```powershell
# Windows
.\gradlew.bat assembleDebug
```

```bash
# macOS or Linux
./gradlew assembleDebug
```

The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. Debug builds use Android's automatically generated debug signing key; no repository keystore is required.

## Run tests and checks

On Windows:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat compileDebugAndroidTestKotlin
.\gradlew.bat connectedDebugAndroidTest
```

Use the equivalent `./gradlew` commands on macOS or Linux. `connectedDebugAndroidTest` requires a running emulator or connected device and includes the Room migration test. Room schema snapshots are kept in `app/schemas`.

## Release signing

Release builds must be signed with a private production/upload keystore that is created and retained by the publisher. The Gradle configuration expects:

- `KEYSTORE_PATH`: absolute path to the keystore
- `STORE_PASSWORD`: keystore password
- `KEY_PASSWORD`: password for the `upload` key alias

With those environment variables configured, build with `./gradlew assembleRelease` (or `.\gradlew.bat assembleRelease` on Windows). Never commit a keystore, passwords, `local.properties`, `.env` files, APKs, or Android App Bundles to source control. Back up the release key securely; losing it can prevent future updates outside a managed app-signing service.
