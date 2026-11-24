# RSS Reader - Kotlin Multiplatform

A modern, cross-platform RSS feed reader application built with **Compose Multiplatform** targeting **Android**, **Desktop (Linux & Windows)**, and **iOS**.

The project is built following strict **Clean Architecture** principles and an **Offline-First** mindset, ensuring smooth performance with local persistence and seamless cloud synchronization.

---

## 🌟 Key Highlights

- **Clean Architecture:** Strict separation of concerns divided into Domain, Data, and Presentation layers:
  - **Domain:** Pure Kotlin models, isolated Use Cases (`Interactors`), and repository contracts with zero framework dependencies.
  - **Data:** Repository implementations, Room Database (SSOT), DataStore preferences, and remote cloud data sources.
  - **Presentation:** Compose Multiplatform UI powered by unidirectional data flow (UDF) with MVI-style `UiState` and `UiEvent` view models.
- **Offline-First Architecture:**
  - **Room Database** acts as the Single Source of Truth (SSOT). All feeds and subscriptions are stored locally and instantly accessible without internet.
  - Queued mutations (such as pending deletions) handle offline edge cases and synchronize automatically when network connectivity is restored.
- **Cloud Synchronization & Backend:**
  - Powered by **Supabase** (PostgreSQL, Row-Level Security, Auth, and Realtime sync).
  - Background synchronization engine orchestrates remote sources and feeds while avoiding duplicates and preserving read states.
  - Optional guest mode ("Continue without account") for a 100% local, friction-free experience.
- **Dependency Injection:**
  - Robust multiplatform DI architecture using **Koin** (`koin-core`, `koin-compose`, and `koin-compose-viewmodel`).
- **Adaptive UI:**
  - Responsive layouts supporting compact phone screens and large wide displays (two-pane layouts, floating dialogs, and side panels for desktop/tablets).

---

## 🛠 Tech Stack

| Component | Technology |
|---|---|
| **Language** | [Kotlin Multiplatform (KMP)](https://kotlinlang.org/docs/multiplatform.html) (Kotlin 2.1+) |
| **UI Framework** | [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) (Material 3) |
| **Local Database** | [AndroidX Room](https://developer.android.com/training/data-storage/room) Multiplatform (SQLite) |
| **Preferences** | [AndroidX DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (Preferences) |
| **Backend / Remote** | [Supabase-kt](https://github.com/supabase-community/supabase-kt) (Auth, Postgrest, Realtime) |
| **Dependency Injection** | [Koin](https://insert-koin.io/) |
| **Networking** | [Ktor Client](https://ktor.io/) |
| **HTML / XML Parsing** | [Ksoup](https://github.com/fleeksoft/ksoup) |
| **Image Loading** | [Coil 3](https://coil-kt.github.io/coil/) |

---

## 📁 Architecture Overview

```text
shared/src/
├── domain/                  # Pure Business Logic
│   ├── model/               # Immutable domain entities
│   ├── repository/          # Repository interfaces (contracts)
│   └── usecase/             # Single-responsibility interactors
│
├── data/                    # Data Layer Implementation
│   ├── database/            # Room Database, DAOs, & Entities (Local SSOT)
│   ├── datastore/           # UserPreferencesDataSource
│   ├── remote/              # Supabase API & RSS parser data sources
│   ├── mapper/              # DTO <-> Entity <-> Domain mappers
│   └── repository/          # Repository implementations
│
└── presentation/            # UI Layer (Compose Multiplatform)
    ├── components/          # Reusable UI widgets & Adaptive ScreenSize
    ├── navigation/          # Navigation graph & NavHost
    ├── feedsscreen/         # Feed list & adaptive layout
    ├── feeddetail/          # Article reader & HTML content renderer
    ├── addsourcescreen/     # RSS feed source manager & side panel
    ├── settingsscreen/      # User settings & session management
    └── authscreen/          # Authentication & guest mode entry
```

---

## 🚀 Getting Started

### Prerequisites
- JDK 21
- Android Studio / IntelliJ IDEA with KMP support

### Environment Configuration
Create a `local.properties` file in the root directory:
```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_KEY=your-anon-key
```

### Running the Applications

The project uses the self-bootstrapping [Kotlin Toolchain](https://kotlin-toolchain.org/dev/) wrappers (`./kotlin` on Linux/macOS, `kotlin.bat` on Windows).

- **Android App:**
  ```bash
  ./kotlin run -m androidApp
  ```
- **Desktop App (JVM):**
  ```bash
  ./kotlin run -m desktopApp
  ```
- **Run All Shared Checks (Linters & Unit Tests):**
  ```bash
  ./kotlin check -m shared
  ```
