# Android Applications Portfolio

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Java](https://img.shields.io/badge/Language-Java-ED8B00?style=flat-square&logo=java&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![MVVM](https://img.shields.io/badge/Architecture-MVVM-orange?style=flat-square)
![Clean Architecture](https://img.shields.io/badge/Architecture-Clean-blue?style=flat-square)
![SQLite](https://img.shields.io/badge/Database-SQLite-003B57?style=flat-square&logo=sqlite&logoColor=white)

## Short Description
A centralized repository hosting production-grade Android applications built with Clean Architecture, Modern Android Development (MAD) toolsets, and robust local-first patterns. The suite features specialized solutions for agricultural logistics weight tracking and network security auditing.

## Project Introduction
This portfolio showcases mobile applications engineered to demonstrate clean, maintainable, and testable design patterns. The projects solve distinct, real-world operational challenges: Cane Tracker automates harvest loading logistics by dynamically computing net crop weight and mitigating manual entry errors, while WiFi Inspector Pro provides network diagnostic telemetry and connection auditing. By separating business logic from platform components using MVVM, repository patterns, and local storage, these applications maintain performance, decoupled testing capability, and complete offline resilience.

```mermaid
graph TD
    A[Start: Capture Gross Weight] --> B[Input Worker Body Weight]
    B --> C[Compute Net Weight: Gross - Worker]
    C --> D[Persist Transaction to Room Local DB]
    D --> E{Network Connection Available?}
    E -- Yes --> F[Sync Data with Server]
    E -- No --> G[Queue Transaction locally]
    F --> H[End: Sync Complete]
    G --> H
```

## Tech Stack and Core Engineering

* **Technologies:** Kotlin, Java, Jetpack Compose, Material 3, XML Layouts, Room Database, SQLite, Shared Preferences, Retrofit, OkHttp, LiveData, ViewModels.
* **Engineering Methods:** MVVM (Model-View-ViewModel) architectural pattern, Clean Architecture principles, offline-first data caching strategies, repository abstraction layer, and automated real-time logic for weight subtraction.

## Getting Started

### Prerequisites
* JDK 17
* Android Studio Ladybug or later
* Android SDK Platform 34 (Android 14.0)

### Installation and Usage

```bash
# Clone the repository
git clone https://github.com/username/android-apps-portfolio.git

# Navigate into the project directory
cd android-apps-portfolio

# Build debug APKs for all projects
./gradlew assembleDebug
```

## Key Features

* **Automated Logistics Computation:** Calculates net harvest weight automatically by subtracting worker body weight from gross weight in real-time.
* **Network & Security Auditing:** Performs real-time wireless signal monitoring, active IP address discovery, and security scans to optimize performance.
* **Offline-First Persistence:** Implements Room Database for secure local transaction caching, ensuring operation in remote regions.
* **Modern UI/UX:** Employs declarative Jetpack Compose interfaces combined with XML fallback layouts to maintain support across legacy systems.

## Directory Structure

```text
android-apps/
├── cane-tracker/         # Agricultural loading & worker weight tracker
├── wifi-inspector-pro/   # Network diagnostic and security tool
└── README.md             # Repository documentation
```

## Configuration
To compile the applications, define your local Android SDK directory in a `local.properties` file at the root level:

```properties
sdk.dir=/path/to/your/android/sdk
```

## Contributing
Contributions to extend capabilities or optimize the existing applications are welcome. Please open an issue to outline suggested changes or submit a structured Pull Request adhering to the repository's MVVM and Clean Architecture patterns.

## License
This project is licensed under the MIT License:

```text
MIT License

Copyright (c) 2026 Alok M

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
