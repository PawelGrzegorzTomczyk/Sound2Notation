📱 sound2notation Android App
Minimal Android client for the sound2notation system.

This project contains only the essential code and configuration files.
All build artifacts, test folders (androidTest, test) and cache files have been removed.

⚙️ Configuration
Before running the app:
  Set server IP
    In the network module, update the base URL to match the IP and port where your Flask server is running.
    You'll find this in a file like NetworkModule.kt

  Build & Run
    Open the project in Android Studio and build as usual.

🧾 Included
This repository includes:
  Modified Kotlin source files (/app/src/main)
  Gradle configuration files (build.gradle, settings.gradle, etc.)
  Custom AppLayout, navigation logic, and networking setup
  Bottom bar navigation, login, and file upload features
  XML score viewer powered by embedded WebView
