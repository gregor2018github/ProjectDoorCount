# DoorCount

Android app for tracking gate occupancy over time. Each visit, you record which of 6 gates are occupied; the app stores timestamped snapshots and visualises the patterns.

## Features

**Entry tab**
- Six toggle buttons (one per gate) that change colour when active
- Save button records the current state with the phone's timestamp
- History button lists all past entries — tap to edit gate states or delete a record

**Statistics tab**
- Gate occupation rate — horizontal bar chart per gate, tap a bar for the exact count
- Measurements per day — last 30 days
- Measurement time distribution — how often you record by hour of day
- Average occupation rate by hour — which hours tend to have more gates occupied
- Overall occupation — single large percentage with raw fraction (e.g. 36/60)

All charts have clickable bars that highlight the selection and show the value inline.

## Tech stack

| Layer | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| State | ViewModel + StateFlow |
| Storage | JSON file in internal storage via Gson |
| Min SDK | 26 (Android 8.0) |
| Target device | Google Pixel 6 |

## Data format

Each record stored in `measurements.json` (internal app storage):

```json
{"timestamp": "2026-06-26T14:30:00", "gates": [true, false, true, false, false, true]}
```

The file is a JSON array of these objects, appended on every save.

## Build & run

Open `DoorCountApp/` in Android Studio (not the repo root). Android Studio will sync Gradle automatically.

```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Unit tests
./gradlew :app:testDebugUnitTest
```

## Tests

`MeasurementRepositoryTest` pins the JSON schema and verifies round-trip correctness, ordering, editing, and deletion. The key test (`existing JSON from previous install loads correctly`) ensures that reinstalling a new version of the app does not corrupt data already saved on the device.
