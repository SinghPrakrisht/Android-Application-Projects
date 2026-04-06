# 📱 Sensor Dashboard App

An Android application that reads and displays real-time data from built-in device sensors including **Accelerometer**, **Light Sensor**, and **Proximity Sensor** using the Android SensorManager API.

---

## 🚀 Features

- 📊 Real-time sensor data monitoring  
- ⚡ Accelerometer with X, Y, Z axis values  
- 🌞 Light sensor showing ambient intensity (lux)  
- 📡 Proximity sensor detecting nearby objects  
- 🎨 Clean and modern UI dashboard  
- 🟢 Live status indicators for each sensor  

---

## 🛠️ Tech Stack

- Java  
- Android SDK  
- SensorManager API  
- XML (UI Design) 

---

## 🧠 How It Works

The application uses the **SensorManager API** to access device sensors:

- **Accelerometer**  
  Measures motion and displays values across X, Y, and Z axes. It also determines whether the device is still, moving, or moving fast.

- **Light Sensor**  
  Detects ambient light intensity in lux and categorizes it into levels such as Dark, Dim, Indoor Light, Bright, and Sunlight.

- **Proximity Sensor**  
  Detects the presence of nearby objects and indicates whether the surroundings are clear or an object is close.

---
## ▶️ How to Run

1. Clone the repository:
   ```bash
   git clone <your-repo-url>
2. Open in Android Studio
3. Sync Gradle
4. Run the app on:
   i)Physical device 📱 (recommended for sensors)
   ii)Emulator (limited sensor support)

---
## 📂 Project Structure
```
app/
├── manifests
│   └── AndroidManifest.xml
├── kotlin+java
│   ├── com.example.sensordashboard
│   │   └── MainActivity
│   ├── com.example.sensordashboard (androidTest)
│   └── com.example.sensordashboard (test)
├── java (generated)
└── res
    ├── drawable
    │   ├── axis_bg_x.xml
    │   ├── axis_bg_y.xml
    │   ├── axis_bg_z.xml
    │   ├── badge_bg_blue.xml
    │   ├── badge_bg_green.xml
    │   ├── badge_bg_yellow.xml
    │   ├── ic_launcher_background.xml
    │   ├── ic_launcher_foreground.xml
    │   ├── icon_bg_blue.xml
    │   ├── icon_bg_green.xml
    │   ├── icon_bg_yellow.xml
    │   ├── indicator_active.xml
    │   ├── indicator_inactive.xml
    │   ├── value_bg_green.xml
    │   └── value_bg_yellow.xml
    ├── layout
    │   └── activity_main.xml
    ├── mipmap
    ├── values
    │   ├── themes (2)
    │   │   ├── themes.xml
    │   │   └── themes.xml (night)
    │   ├── colors.xml
    │   └── strings.xml
    └── xml
        ├── backup_rules.xml
        └── data_extraction_rules.xml
Gradle Scripts
├── build.gradle.kts (Project: Sensor_Dashboard)
├── build.gradle.kts (Module :app)
├── proguard-rules.pro (ProGuard Rules for ":app")
├── gradle.properties (Project Properties)
├── gradle-wrapper.properties (Gradle Version)
├── libs.versions.toml (Version Catalog "libs")
├── local.properties (SDK Location)
└── settings.gradle.kts (Project Settings)
```
---
## 📸 Screenshots

### 🏠 Home Screen 1
[Home Screen 1](home_page_1.jpeg)

### 🏠 Home Screen 2
[Home Screen 2](home_page_2.jpeg)

---
👨‍💻 Author

Prakrisht Singh
