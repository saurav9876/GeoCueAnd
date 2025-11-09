# GeoCue (Android)

GeoCueAnd is the Android sibling to the existing iOS GeoCue app. It provides a Compose-first experience with geofenced reminders, a live map, and a settings surface that mirrors the core flows from the SwiftUI implementation.

## Project structure

```
GeoCueAnd/
├── app/
│   ├── src/main/
│   │   ├── java/com/geocue/android/
│   │   │   ├── data/                # Room entities, DAO, repository
│   │   │   ├── domain/              # Domain models + interactor coordinating geofencing
│   │   │   ├── location/            # Fused location + Geofencing controller
│   │   │   ├── notifications/       # Notification channels + broadcast receiver
│   │   │   ├── permissions/         # Lightweight permission helpers
│   │   │   ├── ui/                  # Compose screens + theming
│   │   │   └── GeoCueApp.kt         # Root composable with bottom navigation
│   │   ├── res/                     # Resources (icons, strings, themes)
│   │   └── AndroidManifest.xml
├── build.gradle.kts                 # Version catalog and plugin declarations
├── settings.gradle.kts
├── gradle.properties
└── secrets.defaults.properties      # Placeholder for Google Maps key
```

## Features

- **Reminder list**: Grouped active/inactive reminders with enable/disable and delete actions.
- **Add reminder sheet**: Geocoder-backed place search, current-location shortcut, and map pinning with radius controls.
- **Geofencing backend**: Room-backed storage plus Play Services geofencing via `GeofenceController`.
- **Map tab**: Google Maps Compose integration with reminder markers and radius overlays.
- **Settings tab**: Permission status summary and quick links to system settings.
- **Notification pipeline**: Broadcast receiver posts reminders through Material notifications with throttling.
- **Theming**: Material 3 theme tuned to GeoCue branding with light/dark support.

## Getting started

1. Install the Android SDK 34 toolchain and JDK 17.
2. From the project root, generate the Gradle wrapper (the repo omits binaries):
   ```bash
   cd GeoCueAnd
   gradle wrapper --gradle-version 8.7
   ```
3. Open the project in Android Studio (Giraffe+ recommended) or run from the command line:
   ```bash
   ./gradlew :app:assembleDebug
   ```
4. Add your Google Maps API key. Either:
   - Copy `secrets.defaults.properties` to `secrets.properties` and replace `YOUR_GOOGLE_MAPS_API_KEY`, or
   - Define `MAPS_API_KEY` in your environment when building.
5. Deploy to a device/emulator with Google Play Services. Approve foreground/background location and notification permissions when prompted.

## iOS parity notes / TODOs

- The add-reminder sheet supports geocoder-backed search and a live preview map; consider upgrading to Google Places autocomplete for richer suggestions and place details (hours, photos, etc.).
- Subscription, onboarding, Do-Not-Disturb, and ringtone selection systems present in iOS are stubbed out in this first Android pass.
- Background location prompts follow Android 13+ best practices (manual upgrade via Settings after first grant). Consider adding an in-app education flow similar to iOS onboarding.
- Crash reporting/analytics hooks mirror the Swift structure conceptually but are not wired to a provider yet.
- Automated tests are not included yet; targeted unit tests for the repository and interactor plus UI tests would be good follow-ups.

---

Feel free to reach out if you want to iterate on deeper feature parity or plug in shared services (analytics, subscriptions, etc.).
