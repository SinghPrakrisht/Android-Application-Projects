# 📸 PhotoVault - Android Camera & Gallery App

## 📱 Overview
PhotoVault is an Android application that enables users to capture, store, browse, and manage images securely on their device. The app integrates camera functionality with a custom gallery experience, allowing users to organize and control their media efficiently.

---

## ✨ Features

### 📷 Capture Images
- Capture photos using the device camera
- Save images to a user-selected folder
- Runtime permission handling for camera and storage access

### 📂 Browse Device Folders
- Select and open any folder from the device storage
- Display all images within the selected folder

### 🖼️ Gallery View
- Images displayed in a grid layout similar to a standard gallery app
- Smooth and user-friendly browsing experience

### 📄 Image Details
- View detailed information of selected image:
  - Image Name
  - File Path
  - File Size
  - Date Captured

### 🗑️ Delete Images
- Delete images directly from the app
- Confirmation dialog before deletion
- Automatically returns to gallery after deletion

---

## 🛠️ Tech Stack

- Java / Kotlin
- Android SDK
- XML (UI Design)
- Camera API / Intent
- RecyclerView (Grid Gallery)
- File Handling APIs
- Runtime Permissions

---

## 📂 Project Structure

```
app/
├── build/
├── src/
│   ├── androidTest/
│   └── main/
│       ├── java/
│       │   └── com.example.photovault/
│       │       ├── MainActivity
│       │       ├── CameraActivity
│       │       └── DetailActivity
│       └── res/
│           ├── drawable/
│           ├── layout/
│           ├── mipmap/
│           ├── values/
│           ├── values-night/
│           └── xml/
│       └── AndroidManifest.xml
├── test/
├── .gitignore
├── build.gradle.kts
└── proguard-rules.pro
```

---

## 📸 Screenshots

### 🏠 Home Page
Main interface providing options to access camera and browse folders.
[Home](home.jpeg)

### 📷 Camera Capture
Capturing image using device camera and saving to selected folder.
[Camera](camera.jpeg)

### 🔐 Folder Browsing / PhotoVault
Selecting and browsing a folder containing images.
[Folder](vault.jpeg)

### 🖼️ Gallery View
Images displayed in grid format for easy navigation.
[Gallery](gallery.jpeg)

### 📄 Image Details
Detailed view showing image metadata such as name, path, size, and date.
[Details](details.jpeg)

### 🗑️ Delete Confirmation
Delete option with confirmation dialog for secure removal.
[Delete](delete.jpeg)

---

## 🚀 How to Run

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle
4. Grant required permissions (Camera & Storage)
5. Run the app on a device or emulator

---

## ⚠️ Permissions Used

- Camera Permission
- Read External Storage
- Write External Storage

---

## 👨‍💻 Author
Prakrisht Singh
