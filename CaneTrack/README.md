# CaneTrack 🌾

![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge&logo=github&logoColor=white)
![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![UI Tool](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Widget Engine](https://img.shields.io/badge/Widget-Jetpack%20Glance-00C4CC?style=for-the-badge&logo=androidworkspaces&logoColor=white)
![Licence](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

A high-fidelity, highly-animated Android utility engineered for real-time sugarcane harvest tracking, crew weight logging, and comprehensive earnings diaries—paired with an interactive, glanceable Home Screen Widget.

---

## 📸 Legacy vs. Premium Revamp

CaneTrack was transformed from a static, utility-centric tool into a gorgeous, premium, highly-interactive app optimized specifically for high-sunlight, real-world outdoor field operations.

| Feature / Metric | Legacy Experience (Before) | Premium Obsidian Forest Revamp (After) |
| :--- | :--- | :--- |
| **Colors & Palette** | Static emerald green accents with default system backgrounds. High glare. | **Obsidian Forest System**: Sleek Obsidian slates (`#070A13`) engineered for sunlight contrast and ultra-low eye strain. |
| **Progress View** | Standard progress indicators and static text readout panels. | **Circular Progress Canvas Gauge**: Handcrafted `Canvas` arc showing session progress against customizable daily goals (5,000 kg). |
| **Borders & UI** | Flat panels without depth or adaptive shading. | **Glassmorphic Outlines**: Modern card wrappers with dynamic `1.dp` borders adjusting dynamically to Light/Dark mode. |
| **Interaction** | Quick tapping on buttons (prone to accidental triggers/data loss in fields). | **Slide-to-Confirm Swipe**: Animated sliding capsule thumb gesture that securely locks in harvest logs. |
| **Page Transitions** | Standard instant cuts (`Crossfade`) between views. | **Directional Nav Deck**: Elastic, horizontal sliding screens shifting based on hierarchical route index. |
| **Button Physics** | Standard system default click responses. | **Bouncy Spring Physics**: Dynamic `bounceClick` modifier scaling components smoothly to `0.93f` on touch. |
| **Live Aggregates** | Must launch the app to inspect running totals. | **Jetpack Glance Widget**: Real-time earnings (`₹`) and weight progress right on the launcher home screen. |

---

## ✨ Features & Micro-Interactions

### 🎨 Part A: Premium Obsidian Forest Design Language
* **High-Sunlight Contrast**: Tailored specifically for agricultural environments. Deep charcoal obsidian tones offer maximum readability under direct sun glare.
* **Sugarcane Mint Gradients**: Beautiful gradient rings drawn dynamically on a custom canvas showing performance sweeps.
* **Glassmorphic Layouts**: Smooth rounded cards containing outline accents that adapt gracefully between theme elevations.
* **Tactile Slide-to-Confirm**: Secure gesture confirmation button requiring a full horizontal drag-sweep to submit and persist session entries.

### 🌀 Part B: Motion & Spring Dynamics
* **Elastic Bouncy Physics (`bounceClick`)**: Applied across buttons, crew selectors, and date pickers for satisfying haptic-like scaling feedback.
* **Rolling Odometer Digits (`AnimatedWeightText`)**: Split columns rolling independently. When weight changes, only the modified numbers slide vertically while decimals and unchanging numbers stand perfectly still.
* **Accordion Expansions**: Smooth `.animateContentSize()` transitions that ease history cards open elastically to reveal crew lists.
* **Navigation Sliding Deck**: Transitions slide gracefully from right to left when descending deeper, and left to right when backing out.

### 📱 Part C: Jetpack Glance Home Screen Widget
* **Live Earnings Tracker**: Calculates real-time total session weight and financial earnings in Rupees (`₹`) on the fly.
* **Quick Log Shortcut**: An interactive `"LOG NEW TRIP"` trigger button that boots the app directly into session mode.
* **Zero Resource Overhead**: Leverages a local initial loader layout (`widget_initial_layout.xml`) to prevent widget loading lockups during compilation.

---

## 🛠️ Architecture & Specifications

CaneTrack is written fully in **Kotlin** and built on top of **Jetpack Compose / Material 3**.

### Sdk Requirements
*   **Min SDK**: `24` (Android 7.0)
*   **Target & Compile SDK**: `35` (Android 15)
*   **Java Compatibility**: `Java 11`

### Added Core Engine Dependencies
```kotlin
// Glance App Widget Core
implementation("androidx.glance:glance-appwidget:1.1.0")
// Glance Material 3 Dynamic Theming integration
implementation("androidx.glance:glance-material3:1.1.0")
```
🚀 How to Compile & Build
-------------------------

To compile and assemble the debug application package (.apk), set your local JDK environment variable to the bundled JetBrains Runtime path (inside Android Studio) and run the Gradle wrapper:

### 💻 PowerShell
```text
$env:JAVA_HOME="E:\AndroidStudio\jbr"
.\gradlew.bat assembleDebug
```
### 🐚 Bash / Command Prompt
```text
set JAVA_HOME="E:\AndroidStudio\jbr"
gradlew.bat assembleDebug
```
TIP
The resulting standalone debug application binary package will be generated at: app/build/outputs/apk/debug/app-debug.apk

Developed with 🌾 for high-performance agricultural operations.
