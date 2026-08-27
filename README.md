# AuraPlay 🎵

A modern, offline-first Android music player built with **Jetpack Compose**, **Media3 (ExoPlayer)** and **Material 3**.

> **Identity:** `com.auraplay.player` · **Min Android:** 11 (API 30) · **License:** GPL-3.0

---

## ✨ Features

| Area | What you get |
|---|---|
| **Playback** | Media3/ExoPlayer engine, gapless playback, playback speed, sleep timer, FFmpeg & MIDI decoder extensions |
| **Library** | Tracks, albums, artists, genres, folders — powered by a fast Room database with instant search |
| **Playlists & more** | Playlists (with NLP quick-fill), favorites, playback history, engagement stats, Daily Mix |
| **Audio** | Equalizer with presets, bass boost, virtualizer — all backed up with your settings |
| **Lyrics** | LRCLIB lyrics lookup (optional, opt-in) |
| **Online sync** *(off by default)* | Navidrome, Subsonic, Jellyfin, ListenBrainz scrobbling, MusicBrainz |
| **Widgets** | Glance home-screen widgets, quick-settings tiles |
| **Backup** | Full encrypted backup/restore of settings, playlists, stats and more |
| **Design** | Material 3 Expressive, dynamic album-art theming, expressive sliders & animations |
| **Android integration** | Media session, Android Auto-ready, headphone/Bluetooth controls |

## 📦 Get the APK

1. Go to [**Releases**](https://github.com/Mylittlestories/AuraPlay/releases)
2. Download `AuraPlay-<version>-universal.apk`
3. Install it (enable "install unknown apps" if asked)

Every push to a `v*` tag builds and publishes a fresh release automatically via GitHub Actions.

**Obtainium users:** add this repo, app id `com.auraplay.player`.

## 🛠️ Build from source

```bash
git clone https://github.com/Mylittlestories/AuraPlay.git
cd AuraPlay
./gradlew :app:assembleDebug                      # debug build
./gradlew :app:assembleRelease -Ppixelplayer.enableAbiSplits=false   # one universal release APK
```

Requirements: **JDK 21**. For a signed release build, provide `keystore.properties` at the repo root:

```properties
storeFile=your-release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

## 🔄 CI

`.github/workflows/android.yml`:
- **Every push** → universal release APK built, signed with the CI keystore (from repo secrets), uploaded as a workflow artifact
- **Every `v*` tag** → same APK attached to an auto-generated **GitHub Release**

## 🙏 Credits & License

AuraPlay is based on **[PixelPlayerOSS](https://github.com/PixelPlayerHQ/PixelPlayerOSS)** by [@lostf1sh](https://github.com/lostf1sh) and its contributors — thank you for an outstanding FOSS music player!

This project therefore inherits the **GNU General Public License v3.0** (see [LICENSE](LICENSE)). The source will always remain public in compliance with the license.
