# Anatomy Insight

[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-%230A8FDC.svg?style=flat&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Kotlin](https://img.shields.io/badge/Kotlin-%230095D5.svg?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Filament](https://img.shields.io/badge/3D%20Rendering-Google%20Filament-brightgreen)](https://google.github.io/filament/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![3D Models](https://img.shields.io/badge/3D%20Models-Z%20Anatomy-orange)](https://www.z-anatomy.com/)

Anatomy Insight is an Android application for interactive 3D exploration of human anatomy, powered by Google Filament for real-time rendering and built entirely with Jetpack Compose.

---

<p align="center">
  <img src="https://github.com/shaikhmohammadtalha/android-anatomy-insight/blob/main/metadata/images/phoneScreenshots/01.png" width="230">
  <img src="https://github.com/shaikhmohammadtalha/android-anatomy-insight/blob/main/metadata/images/phoneScreenshots/02.png" width="230">
  <img src="https://github.com/shaikhmohammadtalha/android-anatomy-insight/blob/main/metadata/images/phoneScreenshots/03.png" width="230">
</p>

<p align="center">
  <img src="https://github.com/shaikhmohammadtalha/android-anatomy-insight/blob/main/metadata/images/phoneScreenshots/04.png" width="230">
  <img src="https://github.com/shaikhmohammadtalha/android-anatomy-insight/blob/main/metadata/images/phoneScreenshots/05.png" width="230">
  <img src="https://github.com/shaikhmohammadtalha/android-anatomy-insight/blob/main/metadata/images/phoneScreenshots/06.png" width="230">
</p>

---

## About

Anatomy Insight offers detailed real-time 3D anatomical visualization across 6 major systems with 2,370 individual subpart models. Built on Google Filament, every structure can be rotated, scaled, and inspected in high detail.

Anatomical metadata — including common names, Latin scientific names, and descriptions — is backed by an on-device Room SQLite database. The app includes study notes per subpart, date-grouped viewing history, global search, configurable rendering settings, and full backup and restore support.

---

## Features

- **Interactive 3D Viewport** – Real-time rendering with Google Filament. Supports gesture rotation, scaling, and a toggleable fullscreen canvas mode.
- **Fullscreen Viewing Modes** – One-tap fullscreen toggles for the 3D model viewport, subpart browsing panels, and the description and study notes editor.
- **6 Anatomical Systems** – Angiology, Arthrology, Muscular Insertions, Myology, Neurology, and Splanchnology.
- **2,370 Subpart Models** – High-resolution individual `.glb` meshes for every anatomical structure.
- **Descriptions & Study Notes** – Formatted scientific descriptions alongside persistent, auto-saving personal notes (with live save indicators and undoable deletion).
- **Viewing History** – Chronological "Recently Viewed" screen grouped by date with quick navigation back to any viewed subpart.
- **Dedicated Global Search** – Search across all 2,370 subparts by common or Latin scientific name with configurable result limits.
- **Database Backup & Restore** – SAF-based database export and restore with schema validation, identity hash patching, and clean process restart. Corrupted or missing local databases are automatically recovered from bundled assets on launch.
- **Graphics & Rendering Controls** – Selectable quality presets (Low, Medium, High, Ultra) with individual toggles for MSAA, Ambient Occlusion, and Bloom.
- **Theme Options** – System Auto, Light, Dark, and dynamic Material You palette support.
- **Responsive Layout** – Adapts cleanly between phones (vertical split + bottom navigation) and tablets/foldables/landscape (side-by-side split + navigation rail).

---

## Anatomical Systems & Subpart Counts

| System | Description | Subparts |
|---|---|---|
| Angiology | Blood vessels and lymphatics | 539 |
| Arthrology | Joints and ligaments | 507 |
| Muscular Insertions | Skeletal muscle attachments | 626 |
| Myology | Muscular system | 395 |
| Neurology | Central and peripheral nervous system | 253 |
| Splanchnology | Visceral organs | 49 |
| **Total** | | **2,370** |

---

## Architecture & Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin 2.4 |
| **UI Framework** | Jetpack Compose + Material 3 |
| **3D Rendering** | Google Filament 1.77 (`filament-android`, `filament-utils-android`, `gltfio-android`) |
| **Navigation** | Jetpack Navigation Compose |
| **Database** | Room 2.8 (SQLite, pre-packaged asset, v8 schema) |
| **Settings** | Jetpack DataStore Preferences |
| **Image Loading** | Coil 2.7 |
| **Architecture** | AndroidX ViewModel + Kotlin Coroutines & Flow |
| **Build System** | Gradle 9.4 with Kotlin DSL + KSP |

### Package Structure

```
com.shaikhmohammadtalha.anatomyinsight
├── data/               # Room entities (Model, Subpart, Note, History), DAOs, Database, BackupManager
├── datastore/          # GraphicsSettingsStore, ThemeStore
├── rendering/          # ModelRenderer (Filament lifecycle & render loop), HDRUtils
├── ui/
│   ├── components/     # BottomNavBar, AppNavigationRail, DescriptionCard, ModelRows, SubpartRows
│   ├── model/          # AnatomyModel UI model
│   ├── screens/        # SearchScreen, SettingsScreen, HistoryScreen, GraphicsSettingsScreen,
│   │                   # ThemeSettingsScreen, PrivacyPolicyScreen, CreditsScreen
│   ├── theme/          # Color, Theme, Typography
│   └── utils/          # DoubleBackToExitHandler
├── viewmodel/          # ModelViewModel
└── MainActivity.kt     # NavHost setup, MainActivityContent layout & fullscreen coordination
```

### Navigation Routes

| Route | Screen |
|---|---|
| `main` | Home – 3D viewport with model and subpart selector |
| `search` | Dedicated full-screen search across all systems |
| `history` | Recently Viewed subparts grouped by date |
| `settings` | Settings hub (Graphics, Theme, Backup & Restore, About) |
| `graphics_settings` | Render quality, MSAA, AO, Bloom, and search limits |
| `theme_settings` | Theme selector (Auto, Light, Dark, Dynamic) |
| `privacy_policy` | Privacy Policy |
| `credits` | Model, texture, and text attributions |

---

## Requirements

- Version: 2.0 (Build 2)
- Minimum SDK: Android 8.1 (API 27)
- Compile / Target SDK: API 37
- Storage: ~300 MB free space (includes 2,370 bundled 3D models)
- Universal APK build supported

---

## Installation & Setup

1. **Clone the repository:**
   ```sh
   git clone https://github.com/shaikhmohammadtalha/android-anatomy-insight.git
   cd android-anatomy-insight
   ```
2. **Open in Android Studio:**
   - Open the project folder in Android Studio Ladybug or newer.
   - Let Gradle sync project dependencies.
3. **Run:**
   - Connect an Android device or start an emulator running API 27+.
   - Press **Run** (Shift + F10).

---

## Attributions

### lightroom_14b.hdr
Environment map provided by Google under Apache License 2.0.
- Source: [Google Model Viewer](https://github.com/google/model-viewer)
- License: [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

### 3D Models
Modified and original models from Z-Anatomy under CC BY-SA 4.0.
- Source: [Z-Anatomy](https://www.z-anatomy.com/)
- Origin: Adapted from BodyParts3D
- License: [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/)

### Anatomical Descriptions
Sourced from open-access and public domain medical literature:
- [OpenStax Anatomy and Physiology](https://openstax.org/details/books/anatomy-and-physiology) (CC BY 4.0)
- [Gray's Anatomy (1918 Edition)](https://www.bartleby.com/107/) (Public Domain)
- [MedlinePlus](https://medlineplus.gov/) (U.S. National Library of Medicine, Public Domain)

---

## License

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.

---

## Contributing & Support

- Contributions and improvements are welcome. Please open an issue or submit a pull request.
- If you find this project helpful, consider starring the repository.
- Bug reports and feature suggestions can be submitted via [GitHub Issues](https://github.com/shaikhmohammadtalha/android-anatomy-insight/issues).

---

## Contact

- Email: shaikhmot@gmail.com
- GitHub: [Shaikh Mohammad Talha](https://github.com/shaikhmohammadtalha)
