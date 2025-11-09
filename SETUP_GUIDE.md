# GeoCue Android - Complete Setup & Run Guide

## ✅ Prerequisites (Already Verified)

- [x] JDK 17 installed and configured
- [x] Gradle wrapper (v8.7) set up
- [x] Android SDK 34 toolchain
- [x] Project builds successfully

## ⚡ Quick Commands

```bash
# Navigate to project
cd /Users/dmeghwal/Documents/myDocs/repo/GeoCueAnd/GeoCueAnd

# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Clean + Rebuild
./gradlew clean assembleDebug

 #  Clean + Rebuild + Install and run app locally
./gradlew clean assembleDebug installDebug

# Start emulator (replace with your AVD name)
$ANDROID_HOME/emulator/emulator -avd Medium_Phone_API_36.1 &

# Install and run app locally
./gradlew installDebug

# Generate release APK
./gradlew assembleRelease

# Output locations:
# Debug APK: app/build/outputs/apk/debug/app-debug.apk
# Release APK: app/build/outputs/apk/release/app-release-unsigned.apk
```

## 🚀 Quick Start - Running the App

### Option 1: Using Android Studio (Recommended)

1. **Open the project in Android Studio:**
   ```bash
   # Open Android Studio and select "Open an Existing Project"
   # Navigate to: /Users/dmeghwal/Documents/myDocs/repo/GeoCueAnd/GeoCueAnd
   ```

2. **Wait for Gradle sync to complete** (should be automatic)

3. **Set up Google Maps API Key** (Required for map features):
   - Get a Google Maps API key from [Google Cloud Console](https://console.cloud.google.com/)
   - Enable "Maps SDK for Android" and "Geocoding API"
   - Edit `secrets.properties` and replace `YOUR_GOOGLE_MAPS_API_KEY` with your actual key:
     ```properties
     MAPS_API_KEY=AIzaSyXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
     ```

4. **Connect a device or start an emulator:**
   - Physical device: Enable USB debugging in Developer Options
   - Emulator: Create one with Google Play Services (API 24+)

5. **Run the app:**
   - Click the green "Run" button (▶️) in Android Studio
   - Or press `Shift + F10` (Windows/Linux) or `Control + R` (Mac)
   - Select your target device

### Option 2: Command Line Build & Install

#### A. Build the APK
```bash
cd /Users/dmeghwal/Documents/myDocs/repo/GeoCueAnd/GeoCueAnd

# Build debug APK
./gradlew :app:assembleDebug

# Or build release APK (unsigned)
./gradlew :app:assembleRelease
```

The APK will be generated at:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release-unsigned.apk`

#### B. Install on Device

**Using ADB (Android Debug Bridge):**

1. **Check connected devices:**
   ```bash
   adb devices
   ```

2. **Install the APK:**
   ```bash
   # Install debug build
   adb install app/build/outputs/apk/debug/app-debug.apk
   
   # Or reinstall if already installed
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Launch the app:**
   ```bash
   adb shell am start -n com.geocue.android.debug/com.geocue.android.MainActivity
   ```

#### C. Build and Install in One Step
```bash
./gradlew :app:installDebug
```

### 📱 Setting Up and Starting an Emulator

If you don't have a physical device, you can use an Android emulator from the command line:

#### 1. Check Available Emulators
```bash
# List all available Android Virtual Devices (AVDs)
$ANDROID_HOME/emulator/emulator -list-avds

# Or with full path on macOS:
/Users/dmeghwal/Library/Android/sdk/emulator/emulator -list-avds
```

Example output:
```
Medium_Phone_API_36.1
Pixel_5_API_34
```

#### 2. Start an Emulator
```bash
# Start an emulator (replace with your AVD name)
$ANDROID_HOME/emulator/emulator -avd Medium_Phone_API_36.1 &

# Or with full path:
/Users/dmeghwal/Library/Android/sdk/emulator/emulator -avd Medium_Phone_API_36.1 &
```

**Note:** The `&` at the end runs the emulator in the background.

#### 3. Wait for Emulator to Boot
The emulator takes 30-60 seconds to fully boot. You can check the status:

```bash
# Check if device is detected
adb devices

# Output when ready:
# List of devices attached
# emulator-5554    device
```

**Status meanings:**
- `offline` - Emulator is starting up, wait a bit longer
- `device` - Emulator is ready! ✅
- Empty list - Emulator hasn't been detected yet

#### 4. Verify Connection
Once you see `device` status, you can install and run your app:

```bash
# Install the app
./gradlew :app:installDebug

# Or install manually
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch the app
adb shell am start -n com.geocue.android.debug/com.geocue.android.MainActivity
```

#### Troubleshooting Emulator Issues

**"adb: no devices/emulators found"**
- Wait 30-60 seconds for the emulator to fully boot
- Run `adb devices` to check status
- If still offline, try restarting ADB: `adb kill-server && adb start-server`

**"command not found: emulator"**
- Add Android SDK to your PATH:
  ```bash
  export ANDROID_HOME=$HOME/Library/Android/sdk
  export PATH=$PATH:$ANDROID_HOME/emulator
  export PATH=$PATH:$ANDROID_HOME/platform-tools
  ```
- Or use the full path: `/Users/dmeghwal/Library/Android/sdk/emulator/emulator`

**Create a new emulator (if needed):**
```bash
# List available system images
sdkmanager --list | grep system-images

# Download a system image (example: API 34 with Google Play)
sdkmanager "system-images;android-34;google_apis_playstore;arm64-v8a"

# Create AVD
avdmanager create avd -n MyEmulator -k "system-images;android-34;google_apis_playstore;arm64-v8a"
```

### Option 3: Install APK Directly

1. **Transfer the APK to your Android device:**
   - Email it to yourself
   - Use cloud storage (Google Drive, Dropbox)
   - Use USB file transfer

2. **On your Android device:**
   - Enable "Install unknown apps" for your file manager/browser
   - Tap the APK file and follow the installation prompts

## 📱 First Run - Permissions

When you first launch GeoCue, you'll need to grant:

1. **Location Permission** (Foreground) - for current location features
2. **Background Location Permission** - for geofence triggers when app is closed
3. **Notification Permission** (Android 13+) - to receive reminder alerts

## 🗺️ Google Maps API Key Setup (Important!)

Without a valid Google Maps API key, the map features won't work. Here's how to get one:

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable these APIs:
   - Maps SDK for Android
   - Geocoding API
4. Go to "Credentials" → "Create Credentials" → "API Key"
5. Copy the key and paste it in `secrets.properties`:
   ```properties
   MAPS_API_KEY=AIzaSyXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
   ```
6. (Optional) Restrict the key to your app's package name: `com.geocue.android.debug`

## 🛠️ Useful Commands

### Building
```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew :app:assembleDebug

# Build and install on connected device
./gradlew :app:installDebug

# Build release APK
./gradlew :app:assembleRelease

# Run all checks (lint, tests)
./gradlew check
```

### Debugging
```bash
# View logs
adb logcat | grep GeoCue

# Clear app data
adb shell pm clear com.geocue.android.debug

# Uninstall app
adb uninstall com.geocue.android.debug
```

### Device Info
```bash
# List connected devices
adb devices

# Get device info
adb shell getprop ro.build.version.sdk

# Take screenshot
adb exec-out screencap -p > screenshot.png
```

## 📦 Project Structure

```
GeoCueAnd/
├── app/
│   ├── src/main/
│   │   ├── java/com/geocue/android/
│   │   │   ├── data/          # Room DB, entities, DAOs
│   │   │   ├── domain/        # Business logic, models
│   │   │   ├── location/      # Location & geofencing
│   │   │   ├── notifications/ # Notification handling
│   │   │   ├── ui/            # Compose screens
│   │   │   └── MainActivity.kt
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── secrets.properties         # Your API keys (gitignored)
└── secrets.defaults.properties
```

## 🐛 Troubleshooting

### Build fails with "SDK location not found"
Create `local.properties` with:
```properties
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
```

### "No devices found"
- Physical device: Enable USB debugging in Developer Options
- Emulator: Start one from Android Studio AVD Manager
- Run `adb devices` to verify connection

### Map shows blank/gray
- Check that `MAPS_API_KEY` is set correctly in `secrets.properties`
- Verify the API key is enabled in Google Cloud Console
- Check that "Maps SDK for Android" is enabled

### App crashes on launch
- Clear app data: `adb shell pm clear com.geocue.android.debug`
- Check logs: `adb logcat | grep GeoCue`

### Gradle daemon issues
```bash
./gradlew --stop  # Stop all Gradle daemons
```

## 📱 Emulator Requirements

- API Level 24 (Android 7.0) or higher
- Must have Google Play Services (use a "Play Store" system image)
- Recommended: API 34 (Android 14) to match targetSdk

## 🔧 Advanced Configuration

### Change Package Name
Edit `app/build.gradle.kts`:
```kotlin
defaultConfig {
    applicationId = "com.yourcompany.geocue"
}
```

### Enable ProGuard/R8 (Release builds)
Edit `app/build.gradle.kts`:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
    }
}
```

## 📚 Additional Resources

- [Android Developer Guide](https://developer.android.com/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Google Maps Platform](https://developers.google.com/maps)
- [Geofencing API Guide](https://developer.android.com/training/location/geofencing)

## ✨ Features to Test

1. **Add Reminder** - Search for a location or use current location
2. **View on Map** - See all reminders with radius circles
3. **Geofence Triggers** - Move in/out of reminder areas to receive notifications
4. **Enable/Disable** - Toggle reminders on/off
5. **Settings** - Check permission status

---

**Need help?** Check the [main README](README.md) for project structure and feature details.

