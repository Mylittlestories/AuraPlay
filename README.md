# 🎵 AuraPlay - Premium Android Music Player

<div align="center">

**DAC Quality Audio • Smart Shuffle • Material 3 Design**

*A next-generation offline music player for Android*

</div>

---

## ✨ Features

### 🎧 Audio Engine
- **DAC Quality Sound** - Crystal clear audio output with ExoPlayer
- **10-Band Equalizer** - Fine-tune your sound with 10 frequency bands
- **Bass Boost** - Deep, powerful bass enhancement
- **Virtualizer** - Immersive surround sound effect
- **Loudness Enhancer** - Boost overall volume without distortion
- **11 Audio Presets** - Flat, Bass Boost, Vocal, Rock, Pop, Jazz, Classical, Electronic, Hip-Hop, Acoustic, Bass & Treble
- **Playback Speed Control** - 0.5x to 2.0x speed adjustment

### 🔀 Advanced Shuffle System
- **Smart Shuffle** - No repetition, varied artists/albums for the best experience
- **True Random** - Pure random selection
- **Artist Mix** - Prioritizes different artists for variety
- **Album Mix** - Mixes tracks from different albums
- **Genre Mix** - Rotates through different genres
- **Most Played First** - Plays your favorites more often
- **Discovery Mode** - Prioritizes less-played tracks for music discovery

### 📱 Library Management
- **Full Device Scan** - Automatically finds all music files
- **Browse by:**
  - Tracks
  - Albums (with grid view)
  - Artists
  - Genres
  - Folders (internal storage & SD card)
  - Favorites
- **Playlist Support** - Create, edit, and manage custom playlists
- **Search** - Instant search across songs, artists, and albums

### 🎨 Design
- **Material 3 / Material You** - Modern, lustrous design
- **Dark Theme** - Beautiful deep purple/blue aesthetic
- **Rotating Vinyl Animation** - Immersive now-playing experience
- **Edge-to-Edge** - Full screen immersive UI
- **Mini Player** - Persistent playback controls
- **Smooth Animations** - Fluid transitions throughout

### 🔧 Technical
- **100% Offline** - No internet required
- **Background Playback** - Media notification controls
- **Headphone Controls** - Hardware button support
- **Queue Management** - Add, remove, reorder tracks
- **Swipe Gestures** - Swipe album art to skip tracks
- **Media Session** - Android Auto & smartwatch support

---

## 🏗️ Architecture

```
AuraPlay/
├── app/
│   └── src/main/java/com/auraplay/player/
│       ├── AuraPlayApp.kt                 # Application class
│       ├── MainActivity.kt                # Main entry point
│       │
│       ├── audio/
│       │   ├── AudioEngine.kt             # Equalizer, effects, presets
│       │   └── ShuffleManager.kt          # 8 shuffle algorithms
│       │
│       ├── data/
│       │   ├── local/
│       │   │   ├── AuraPlayDatabase.kt    # Room database
│       │   │   ├── TrackDao.kt            # Track queries
│       │   │   └── PlaylistDao.kt         # Playlist queries
│       │   ├── model/
│       │   │   ├── Track.kt               # Track entity
│       │   │   └── RepeatMode.kt          # Repeat modes
│       │   └── repository/
│       │       └── MusicRepository.kt     # Data layer
│       │
│       ├── di/
│       │   └── AppModule.kt               # Hilt dependency injection
│       │
│       ├── playback/
│       │   └── PlaybackManager.kt         # ExoPlayer management
│       │
│       ├── service/
│       │   └── PlaybackService.kt         # Background playback
│       │
│       ├── receiver/
│       │   └── MediaButtonReceiver.kt     # Hardware button handler
│       │
│       └── ui/
│           ├── navigation/
│           │   └── Navigation.kt          # Navigation graph
│           ├── screens/
│           │   ├── HomeScreen.kt          # Home with quick actions
│           │   ├── LibraryScreen.kt       # 6-tab library browser
│           │   ├── NowPlayingScreen.kt    # Full player with vinyl
│           │   ├── EqualizerScreen.kt     # 10-band EQ & effects
│           │   ├── SearchScreen.kt        # Search with suggestions
│           │   ├── QueueScreen.kt         # Queue management
│           │   ├── SettingsScreen.kt      # App settings
│           │   ├── ShuffleSettingsScreen.kt # 8 shuffle modes
│           │   ├── PlaylistsScreen.kt     # Playlist management
│           │   ├── DetailScreens.kt       # Album/Artist/Folder detail
│           │   ├── TrackListItem.kt       # Reusable track components
│           │   └── MiniPlayer.kt          # Mini player bar
│           ├── theme/
│           │   ├── Color.kt               # Color palette
│           │   ├── Theme.kt               # Material 3 theme
│           │   └── Type.kt                # Typography
│           └── viewmodel/
│               └── MainViewModel.kt       # Central ViewModel
│
├── build.gradle.kts                       # Top-level build
├── settings.gradle.kts                    # Project settings
└── README.md                              # This file
```

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| **Language** | Kotlin |
| **UI** | Jetpack Compose |
| **Design** | Material 3 / Material You |
| **Audio** | ExoPlayer (Media3) |
| **Database** | Room |
| **DI** | Hilt |
| **Images** | Coil |
| **Architecture** | MVVM + Repository |
| **Async** | Coroutines + Flow |
| **Navigation** | Navigation Compose |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 34
- Kotlin 1.9.20

### Build & Run
1. Clone or open the project in Android Studio
2. Sync Gradle dependencies
3. Run on device or emulator (API 26+)
4. Grant music permission when prompted
5. Tap "Scan for Music" to load your library

---

## 📋 Permissions

| Permission | Purpose |
|------------|---------|
| `READ_MEDIA_AUDIO` | Access music files (Android 13+) |
| `READ_EXTERNAL_STORAGE` | Access music files (Android 12-) |
| `FOREGROUND_SERVICE` | Background playback |
| `POST_NOTIFICATIONS` | Media notification |
| `WAKE_LOCK` | Prevent sleep during playback |
| `BLUETOOTH_CONNECT` | Bluetooth audio devices |
| `MODIFY_AUDIO_SETTINGS` | Audio effects |

---

## 🎯 Key Highlights

### Inspired by Poweramp & Musicolet
- **From Poweramp**: Advanced equalizer, audio effects, DAC-quality processing, vinyl animation
- **From Musicolet**: Clean folder browsing, offline-first, playlist management, no internet required
- **Unique to AuraPlay**: 8 smart shuffle modes, Material 3 design, modern Compose UI

### Performance
- Instant search with debounce
- Efficient Room database queries
- Lazy loading for large libraries
- Smooth 60fps animations

---

## 📄 License

This project is provided as-is for educational and personal use.

---

<div align="center">

**Made with ❤️ for music lovers**

*AuraPlay - Where Every Note Matters*

</div>