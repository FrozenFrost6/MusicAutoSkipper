# Music Auto Skipper

Music Auto Skipper is an Android app that automatically skips songs on YouTube Music or Spotify after a specified percentage of playback. It utilizes Android's Notification Listener Service to detect currently playing songs and schedule skips accordingly.

## Features
- 🎵 **Detects currently playing songs** on YouTube Music and Spotify.
- ⏩ **Automatically skips songs** after a set percentage of playback.
- 📊 **Adjustable skip percentage** using a seek bar.
- 🔔 **Works via notification listener service**, requiring minimal permissions.
- 📡 **Broadcast receiver integration** for real-time updates.
- 🛠 **Lightweight and efficient**, designed to run in the background without excessive battery usage.

## How It Works
1. The app listens for song notifications from YouTube Music.
2. Extracts metadata such as song name, artist, and duration.
3. Uses a configurable percentage to determine the skip time.
4. Automatically skips the song when the specified playback time is reached.

## Setup & Permissions
- Requires **Notification Access** for detecting currently playing songs.
- Make sure to grant the required permissions in settings.

## Usage
1. Download the app-debuk.apk file and install.
2. Open the app and grant notification listener permission if prompted.
3. In the battery settings of the app, found in app info, allow the app to be run on the background.
4. Use the seek bar to adjust the **skip percentage** (e.g., 50% playback before skipping).
5. The app will automatically detect and skip songs based on your preference.
   

## Future Enhancements
- ✅ Support for additional music apps.
- ✅ Customizable skip conditions (e.g., skip based on song genre, artist, etc.).
- ✅ Improved UI/UX with visual song progress indicators.

---
### 🚀 Contributions & Support
Feel free to contribute or report any issues on GitHub. Happy Skipping! 🎶

