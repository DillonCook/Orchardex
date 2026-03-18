# OrchardDex

OrchardDex is an offline-first Android app for managing subtropical fruit trees, cultivar collection progress, local reminders, and manual backups. It uses only on-device storage and requires no account, no backend, and no internet permission at runtime.

## Stack

- Kotlin
- Jetpack Compose + Material 3
- MVVM with manual dependency wiring
- Room for structured local data
- DataStore for settings
- WorkManager for local reminder scheduling
- Navigation Compose
- Android Photo Picker for media import

## Architecture

- `app/src/main/java/com/dillon/orcharddex/data/local`
  - Room entities, DAOs, database, converters
- `app/src/main/java/com/dillon/orcharddex/data/preferences`
  - DataStore-backed app settings
- `app/src/main/java/com/dillon/orcharddex/data/repository`
  - Main repository and local photo storage
- `app/src/main/java/com/dillon/orcharddex/backup`
  - Manual export/import `.orcharddex.zip` backups
- `app/src/main/java/com/dillon/orcharddex/notifications`
  - Notification channel, reminder scheduling, worker
- `app/src/main/java/com/dillon/orcharddex/ui`
  - Navigation, Compose screens, theme, viewmodels

The project uses a single app module to minimize maintenance cost. Data flows from Room/DataStore through `OrchardRepository` into screen-specific viewmodels.

## Local-Only Design

- No backend, sync, login, analytics, ads, crash reporting, maps, or weather APIs
- No `INTERNET` permission
- User photos are copied into app-specific storage under `files/photos`
- Backup/export uses the system file picker and a versioned zip archive
- Optional Android Auto Backup excludes large photo files

## Permissions

- `POST_NOTIFICATIONS`
  - Declared for local reminder notifications
  - Requested only when the user enables reminders on Android 13+

No media, camera, location, or broad file permissions are used for the MVP.

## Running The App

1. Open the project in Android Studio.
2. Confirm `local.properties` points to your Android SDK, or let Android Studio regenerate it.
3. Sync Gradle.
4. Run the `app` configuration on an emulator or device with API 26+.

## Build Debug APK

```bash
./gradlew assembleDebug
```

Windows:

```powershell
.\gradlew.bat assembleDebug
```

The debug APK is produced under `app/build/outputs/apk/debug/`.

## Build Release AAB

```bash
./gradlew bundleRelease
```

Windows:

```powershell
.\gradlew.bat bundleRelease
```

The unsigned release bundle is produced under `app/build/outputs/bundle/release/`.

## Signing

Release signing is intentionally left as a local keystore step:

1. Create a keystore in Android Studio or with `keytool`.
2. Add a signing config in `app/build.gradle.kts`.
3. Store secrets outside the repo, typically in `gradle.properties` or environment variables.
4. Rebuild with `bundleRelease`.

Example placeholder values to add locally:

```properties
ORCHARDDEX_STORE_FILE=/path/to/keystore.jks
ORCHARDDEX_STORE_PASSWORD=replace-me
ORCHARDDEX_KEY_ALIAS=orcharddex
ORCHARDDEX_KEY_PASSWORD=replace-me
```

## Play Store Prep

- Privacy policy draft: [`docs/privacy-policy.md`](/C:/Users/Dillo/code/orchardex/docs/privacy-policy.md)
- Release checklist: [`docs/google-play-release-checklist.md`](/C:/Users/Dillo/code/orchardex/docs/google-play-release-checklist.md)

## Changing The Package Name

Current package/application ID:

- `com.dillon.orcharddex`

To change it:

1. Update `namespace` and `applicationId` in [`app/build.gradle.kts`](/C:/Users/Dillo/Code/OrcharDex/app/build.gradle.kts).
2. Rename the Kotlin package under [`app/src/main/java/com/dillon/orcharddex`](/C:/Users/Dillo/Code/OrcharDex/app/src/main/java/com/dillon/orcharddex).
3. Update any manifest/provider authorities that rely on `${applicationId}` only if you replace the placeholder pattern.

## Backup Format

Manual exports create a `.orcharddex.zip` archive containing:

- `manifest.json`
- `trees.json`
- `tree_photos.json`
- `events.json`
- `harvests.json`
- `reminders.json`
- `wishlist.json`
- `settings.json`
- `photos/`

Imports validate the archive, then replace current local app state.

## Tests

- JVM unit tests
  - reminder scheduling helper
- Instrumented tests
  - Room database smoke coverage
  - repository behavior
  - backup export/import
  - Compose smoke tests for add tree, log harvest, and export backup

## Intentionally Not Implemented

These were excluded to preserve the zero-hosting, zero-backend requirement and keep the app maintainable:

- cloud sync
- collaboration or shared orchards
- accounts/authentication
- online cultivar database
- remote notifications
- weather, maps, GPS, or location features
- server-backed image storage
- analytics, ads, subscriptions, or crash SDKs
