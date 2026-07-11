# WiFi Inspector Pro

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-v1.9.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Theme](https://img.shields.io/badge/Adaptive-Light_&_Dark-6366F1?style=for-the-badge)](https://material.io/design)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](https://opensource.org/licenses/MIT)

**WiFi Inspector Pro** is a fluid, high-fidelity wireless network diagnostics tool designed to map, track, and visualize WiFi signal coverage across physical spaces. Embodying a clean **"Modern Monolith"** design system, the app utilizes native Material 3, system-aware dark/light modes, organic spring-based bouncy micro-interactions, and a real-time cubic Bezier signal engine.

---

## Key Upgrades & New Features

We have upgraded the core architecture of the app to deliver an extremely polished, tactile, and responsive user experience. Here is a breakdown of the new features:

### 1. Unified "Modern Monolith" Aesthetic
*   **Mono-Accent Palette:** Replaced the multi-colored neon gradients with a highly refined Indigo accent system (**Electric Indigo `#6366F1`** for Dark Mode, **Royal Indigo `#4F46E5`** for Light Mode).
*   **Dynamic Slate Surfaces:** Sleek deep slate backgrounds (`#0B0A0F`) and minimalist slate-card structures featuring razor-thin, system-aware borders with 8% opacity.

### 2. Native System-Aware Light & Dark Themes
*   **Theme Synchronization:** The entire UI automatically detects and transitions between Dark and Light modes based on the device's system settings.
*   **Adaptive System Bars:** The Android top status bar and bottom navigation bar update their background fills and icon contrast dynamically to match the active theme.
*   **Dynamic Heatmaps:** Bilinear thermal heatmaps adapt their color scales cleanly (slate-to-indigo blend) to match dark and light backgrounds.

### 3. Organic Spring-Based Tactile Interactions
*   **Modifier.bounceClick:** Implemented custom spring physics that dynamically squish and scale elements (down to `0.94f`) on press, springing back to standard shape with natural damping on touch release. Applied natively to all buttons, grid mapping tiles, and menu cards.
*   **Staggered Grid Entry:** Mapping grids load with sequential index-based offset delays, resulting in a beautiful staggered entry animation when entering a room.

### 4. High-Fidelity Diagnostics & Scan Reticles
*   **Cubic Bezier Curves:** The real-time `SignalGraph` now plots history points using cubic Bezier splines (`cubicTo`) instead of rigid jagged lines.
*   **Infinite Sonar Pulsing & Radar Sweep:**
    *   **Home Screen Radar:** A dynamic circular radar screen that loops infinite sonar pulses and a rotating visual line sweep overlay.
    *   **Active Tile Sonar:** Grid cells render beautiful circular ripples radiating from the center during an active scan, while floating/scaling up to `1.08f` using bouncy easing.
*   **Playful Reticles:** Static grid tiles show clean crosshair SCAN icons instead of loud placeholder text, and empty reports display a beautiful search vector illustration.

---

## Quick Install (Download the App)

If you want to use the app on your Android phone immediately:

1. Look at the file list above and click on **`WiFiInspectorPro.apk`** (or your designated APK artifact).
2. Click the **Download raw file** button (the tray icon with a down arrow).
3. Open the downloaded file on your Android device to install. 

> [!NOTE]
> You may need to enable **"Install from unknown sources"** in your Android Security Settings to authorize sideloading the APK.

---

## Developer Setup (Android Studio)

To inspect, edit, or compile the source code:

1. **Download the Repository:**
   * Download the ZIP archive from the repository root and extract it.
   * *Alternatively*, clone the repository using your terminal:
     ```bash
     git clone https://github.com/arm-x-Dev/Android-Apps.git
     ```
2. **Open Android Studio:** Click **Open** and choose the **`WiFiInspectorPro`** project directory.
3. **Sync Gradle:** Wait for Android Studio to index code files and configure the Gradle build environment.
4. **Run Project:** Connect a physical device with USB Debugging enabled (or start an Android Virtual Device emulator) and click the green **Play (Run)** button at the top toolbar.

---

## How to Use the App

### 1. Permissions & Edge-to-Edge Comfort
*   Upon first launch, the app prompts for Location access—this is required by the Android OS to scan for nearby wireless routers, SSID frequencies, and dBm strengths. All layouts are bounded with status bar insets to prevent UI text overlaps under front-facing camera notches.

### 2. Access Point Scanning
*   Tap **Start New Inspection**. The app scans local network nodes and lists them with dynamic signal strength badges (`STRONG`, `GOOD`, `FAIR`) indicating real-time BSSID quality.

### 3. Layout Mapping
*   In the room layout panel, enter room titles (e.g., *Living Lounge*, *Office Suite*) and tap `+`. Tap any room card to enter the mapping view.

### 4. Active Grid Scanning
*   Walk to different physical areas of a room to record signal details:
    *   **Tap a Cell:** Triggers a 5-second sonar ripple scan, locking down the precise average decibel reading. The active cell floats and scales up using custom spring physics during the sweep.
    *   **Long Press a Cell:** Assign icons such as Router , Door , Window , or Wall  to map obstacles and identify interference dead zones.

### 5. Review Heatmap Archives
*   Tap **Finalize Report** to output logs. Go to **View Scan History** from the home panel to inspect saved reports complete with smooth Bezier charts and Slate-to-Indigo bilinear heatmaps tracking coverage shifts.

---
