# GeoCue Android - Super Simple Flow Diagram

## 🎯 The Big Picture (30-Second Version)

```
┌─────────────────────────────────────────────────────────────┐
│                    USER OPENS APP                           │
│                                                             │
│  GeoCueApplication → MainActivity → GeoCueApp               │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│                  3 TABS APPEAR                              │
│                                                             │
│   🏠 HOME        🗺️ MAP         ⚙️ SETTINGS                │
│                                                             │
│  View/Add      Visualize       Check                        │
│  Reminders     on Map          Permissions                  │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│              USER CREATES A REMINDER                        │
│                                                             │
│  1. Click "+" button                                        │
│  2. Search for "Starbucks"                                  │
│  3. Set radius: 100 meters                                  │
│  4. Add message: "Buy coffee!"                              │
│  5. Click SAVE                                              │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│                 APP SAVES & MONITORS                        │
│                                                             │
│  ✅ Save to Database (Room)                                 │
│  ✅ Register with Android Geofencing API                    │
│  ✅ App closes, Android monitors in background              │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│            USER WALKS NEAR STARBUCKS                        │
│                                                             │
│  Android detects: "User entered geofence!"                  │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│               SEND NOTIFICATION! 🔔                         │
│                                                             │
│  📱 "You're near Starbucks! Buy coffee!"                    │
└─────────────────────────────────────────────────────────────┘
```

---

## 🏗️ Code Organization (Folder-by-Folder)

```
app/src/main/java/com/geocue/android/
│
├── 📂 data/                        ← Database stuff
│   ├── GeofenceRepository.kt       (Save/Load reminders)
│   └── local/
│       ├── GeoCueDatabase.kt       (SQLite database)
│       ├── GeofenceDao.kt          (Database queries)
│       └── GeofenceEntity.kt       (Table structure)
│
├── 📂 domain/                      ← Business logic
│   ├── GeofenceInteractor.kt       (Main coordinator)
│   └── model/
│       ├── GeofenceLocation.kt     (Reminder model)
│       └── NotificationMode.kt     (Entry/Exit settings)
│
├── 📂 location/                    ← GPS & Geofencing
│   ├── AndroidLocationClient.kt   (Get current location)
│   └── GeofenceController.kt      (Register geofences)
│
├── 📂 notifications/               ← Alert system
│   ├── GeofenceEventReceiver.kt   (Listens for events)
│   ├── GeofenceNotificationManager.kt (Shows notifications)
│   └── NotificationChannels.kt    (Setup channels)
│
├── 📂 permissions/                 ← Permission checks
│   └── PermissionChecker.kt       (Has location? notifications?)
│
├── 📂 ui/                          ← All screens
│   ├── home/                       (Reminder list)
│   │   ├── HomeScreen.kt
│   │   ├── AddReminderSheet.kt
│   │   ├── GeofenceListViewModel.kt
│   │   └── AddReminderViewModel.kt
│   ├── map/                        (Google Maps)
│   │   ├── MapScreen.kt
│   │   └── MapViewModel.kt
│   ├── settings/                   (Settings page)
│   │   ├── SettingsScreen.kt
│   │   └── SettingsViewModel.kt
│   └── theme/                      (Colors, fonts)
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
├── 📂 di/                          ← Dependency injection
│   └── AppModule.kt               (Provides shared objects)
│
├── GeoCueApplication.kt           ← App entry point
├── MainActivity.kt                ← Main screen
└── GeoCueApp.kt                   ← Root UI with tabs
```

---

## 🔄 Data Flow (Step-by-Step)

### When User Creates a Reminder:

```
👆 User Action
   ↓
🎨 UI Layer (AddReminderSheet.kt)
   ↓
🧠 ViewModel (AddReminderViewModel.kt)
   ↓
🎯 Domain Logic (GeofenceInteractor.kt)
   ↓
💾 Database (GeofenceRepository.kt)
   ↓
📍 System Service (GeofenceController.kt)
   ↓
✅ Registered with Android!
```

### When Location is Triggered:

```
📍 User enters location
   ↓
🤖 Android System detects geofence
   ↓
📡 GeofenceEventReceiver.onReceive()
   ↓
💾 Look up reminder in database
   ↓
🔔 GeofenceNotificationManager.notifyEvent()
   ↓
📱 Notification appears!
```

---

## 🎯 The 3 Main Components

```
┌───────────────────────────────────────────────────────┐
│              1️⃣ SCREENS (UI)                          │
│                                                       │
│  What user sees and interacts with                    │
│  • HomeScreen.kt - List of reminders                  │
│  • MapScreen.kt - Map view                            │
│  • SettingsScreen.kt - Permissions                    │
│  • AddReminderSheet.kt - Create new reminder          │
└───────────────────────────────────────────────────────┘
                        ↕
┌───────────────────────────────────────────────────────┐
│          2️⃣ BUSINESS LOGIC (Domain)                   │
│                                                       │
│  What the app does                                    │
│  • GeofenceInteractor.kt - Coordinates actions        │
│  • GeofenceRepository.kt - Manages data               │
│  • ViewModels - Connect UI to logic                   │
└───────────────────────────────────────────────────────┘
                        ↕
┌───────────────────────────────────────────────────────┐
│           3️⃣ SYSTEM SERVICES                          │
│                                                       │
│  How it works                                         │
│  • Room Database - Store reminders                    │
│  • Geofencing API - Monitor locations                 │
│  • Notification Manager - Show alerts                 │
└───────────────────────────────────────────────────────┘
```

---

## 🧩 How Everything Connects

```
                    GeoCueApp.kt
                   (Main container)
                         │
         ┌───────────────┼───────────────┐
         │               │               │
    🏠 Home Tab      🗺️ Map Tab    ⚙️ Settings Tab
         │               │               │
         ↓               ↓               ↓
   GeofenceList      MapViewModel   SettingsViewModel
    ViewModel             │               │
         │                │               │
         └────────────────┼───────────────┘
                         │
                         ↓
                GeofenceInteractor
              (Business logic hub)
                         │
         ┌───────────────┼───────────────┐
         ↓               ↓               ↓
   Repository      GeofenceController  PermissionChecker
         │               │
         ↓               ↓
   Room Database    Android Geofencing
                         │
                         ↓
               GeofenceEventReceiver
                         │
                         ↓
            GeofenceNotificationManager
                         │
                         ↓
                 📱 Notification!
```

---

## 🔑 Key Files to Understand First

If you're new to the codebase, **start with these files in order:**

1. **`GeoCueApp.kt`** - See the overall app structure
2. **`HomeScreen.kt`** - See how reminders are displayed
3. **`GeofenceLocation.kt`** - Understand the reminder model
4. **`GeofenceInteractor.kt`** - See the business logic
5. **`GeofenceEventReceiver.kt`** - See how notifications trigger

---

## 💭 Think of it Like This...

| Concept | Real-World Analogy |
|---------|-------------------|
| **GeofenceLocation** | A sticky note with a location |
| **Room Database** | A filing cabinet to store notes |
| **GeofenceInteractor** | Your brain organizing everything |
| **GeofenceController** | A GPS tracker watching locations |
| **GeofenceEventReceiver** | An alarm that goes off when you arrive |
| **NotificationManager** | A messenger that tells you |
| **ViewModel** | An assistant preparing info for display |
| **Screen/UI** | The actual piece of paper you read |

---

## ✨ That's It!

The app is basically:
1. **Store** reminders in a database
2. **Register** locations with Android
3. **Listen** for location events
4. **Show** notifications when triggered

Clean, simple, and efficient! 🚀

---

**Need help?** Check the [main README](README.md) for project structure and feature details.

