# 🎵 sound2notation – Android App

**sound2notation** is a mobile application designed to convert recorded or uploaded audio files into standard music notation (in **MusicXML** format) and display them as sheet music.  
It's a **client-side app** that communicates with a lightweight **Flask server** to handle file uploads and audio-to-notation conversion.

> ⚠️ This app is a **proof-of-concept** and testing tool — not intended for production usage.

---

## 📱 Main Features

- 📤 Upload audio files from your device
- 🔐 Login & user authentication via Flask server
- 🎼 View converted sheet music using **WebView** with `OpenSheetMusicDisplay`
- 🗂️ Manage uploaded files (list & access past uploads)
- 📡 Communicates with a custom **Flask API** for processing

---

## 🧠 How It Works

1. **User logs in** (optional for testing).
2. **User uploads an audio file** (e.g., `.wav`, `.mp3`) via the app.
3. The app sends the file to a **Flask-based backend**.
4. The backend:
   - Converts audio → MIDI → **MusicXML** using:
     - [`basic-pitch`](https://github.com/spotify/basic-pitch)
     - [`music21`](https://web.mit.edu/music21/)
   - Saves the generated XML on the server.
5. The app opens the sheet music via `WebView` using the `OpenSheetMusicDisplay` JS library.

---

## ⚙️ Tech Stack

### 📲 Android App
- Jetpack **Compose** (UI)
- Kotlin **Coroutines** / `viewModelScope` (async logic)
- **Jetpack Navigation**
- **Retrofit2** + custom `NetworkModule` (HTTP API)
- **WebView** for sheet music rendering

### 🌐 Flask Server (separate repo)
- Python **3.11**
- **Flask** + **SQLite**
- [`basic-pitch`](https://github.com/spotify/basic-pitch) – audio to MIDI
- [`music21`](https://web.mit.edu/music21/) – MIDI to MusicXML
- [`OpenSheetMusicDisplay`](https://opensheetmusicdisplay.org/) – visual rendering of MusicXML

---

## 📄 Displaying Scores

The app opens a URL in a `WebView` that renders the MusicXML file using `OpenSheetMusicDisplay`.

📎 **Example score URL:**
http://<server_ip>:<port>/static/displayScore.html?scoreUrl=http://<server_ip>:<port>/<file_name>.xml

---

## 🚧 Known Limitations / Possible Improvements

- ❌ No real-time recording – only local file upload
- 🧹 Temporary files (in `uploads/` and `xmlFiles/guest/`) aren't deleted automatically
- 🧾 No file deletion support in UI (yet)
- 🔑 Placeholder `app.secret_key` on backend – insecure
- 📶 You **must manually configure the server IP** in `NetworkModule.kt`

---

## 🛠️ Setup Instructions

### 1. Backend
Make sure the **Flask server is running**.  
Refer to the [server repo](https://github.com/your-server-repo) for setup instructions.

### 2. Configure IP
Update `NetworkModule.kt` to match your backend address:

```kotlin
private const val BASE_URL = "http://<your_server_ip>:<port>/"
```

### 3. Build the App
Open the project in Android Studio and build as usual.

## ✔️ The repository includes:
- Kotlin source files & Composables
- build.gradle, settings.gradle, etc.
- No build/, test/, or androidTest/ directories
- Clean structure, focused on what's essential for development

### 4. License
#### Use it as you like, u can credit me somewhere by name(Paweł Grzegorz Tomczyk), and send my your version(adrestomaczyka@gmail.com), but i'm not to attached to this. Feel free to ask me anything, im open to talk.
