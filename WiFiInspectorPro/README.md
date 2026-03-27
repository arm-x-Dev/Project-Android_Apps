# 📡 WiFi Inspector Pro

**WiFi Inspector Pro** is a high-fidelity network diagnostics tool designed to map and visualize WiFi signal strength across different rooms. Built with a custom **"Neon Nocturne"** UI aesthetic, the app features liquid-crystal signal graphs, glassmorphism UI elements, and a custom bilinear thermal heatmap engine.

---

## 🚀 Quick Install (Download the App)

If you just want to use the app on your Android phone without opening any code, you can download the APK directly:

1. Look at the file list above and click on **`WiFiInspectorPro.apk`** (or whatever you named your APK file).
2. On the right side of the screen, click the **Download raw file** button (the icon looks like a tray with a down arrow).
3. Open the downloaded file on your Android device to install it. *(Note: You may need to allow "Install from unknown sources" in your Android settings).*

---

## 💻 How to Open in Android Studio (For Developers)

If you want to view, edit, or build the code yourself, follow these steps:

1. **Download the Code:** * Click the green **Code** button at the top of the main repository page and select **Download ZIP**, then extract it.
   * *Alternatively*, clone the repo via terminal: `git clone https://github.com/arm-x-Dev/Android-Apps.git`
2. **Open Android Studio:** Launch Android Studio and click **Open**.
3. **Select the Project:** Navigate to the extracted folder and select the **`WiFiInspectorPro`** folder. Click OK.
4. **Sync Gradle:** Wait a moment for Android Studio to index the files and sync the Gradle build system. (If it prompts you to update Gradle, you can usually accept it).
5. **Run the App:** Connect your Android phone via USB (with USB Debugging enabled) or start a Virtual Device, then click the green **Play/Run** button at the top.

---

## 🛠️ How to Use the App

### 1. Grant Permissions
Upon first launch, the app requires Location permissions. This is an Android OS requirement to scan for nearby WiFi BSSIDs and signal strengths. 

### 2. Select a Node
Tap **Start New Inspection**. The app will scan the surrounding area. Tap on the specific WiFi network (Node) you want to map.

### 3. Add Rooms
In the **Layout / Configuration** dashboard, type in the names of the rooms you are inspecting (e.g., "Master Bedroom", "Kitchen") and hit the `+` button. Tap a room card to enter the Grid Mapper.

### 4. Map the Signal Grid (The Grid Mapper)
Walk to different areas of the physical room to map the signal:
* **Tap a tile:** Initiates a 5-second localized scan to find the precise average signal strength for that spot.
* **Tap and Hold a tile:** Opens the tagging menu. You can tag specific tiles as a Router 🌐, Door 🚪, Window 🪟, or Wall 🧱 to provide physical context to your heatmap.

### 5. Finalize and View History
Once you've mapped your rooms, click **Finalize Report**. Go to the **View History** screen from the Home menu to see your saved sessions. You will see:
* **Liquid Crystal Graphs:** A smooth, quadratic bezier curve showing the signal stability over time.
* **Thermal Heatmaps:** A custom-rendered 3x3 visual grid showing dead zones and strong signal areas shifting from deep Obsidian to Kinetic Lavender.
