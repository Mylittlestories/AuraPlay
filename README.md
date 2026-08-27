# AuraPlay – Premium Android Music Player

**[🎵 Listen online](https://auraplay.example.com) | [📦 F-Droid](https://f-droid.org/en/packages/com.auraplay.player/) | [🛠️ Source](https://github.com/Mylittlestories/AuraPlay)**

AuraPlay is a **modern, offline‑first Android music player** built with **Jetpack Compose**, **Material 3**, and **ExoPlayer (Media3)**. It focuses on high‑quality audio, a gorgeous UI that adapts to your wallpaper (dynamic colors), and a FOSS‑first optional‑services model.

---

## ✨ Highlights

| Feature | Description |
|---|---|
| **Audio Engine** | ExoPlayer (Media3) with a 10‑band equalizer, bass boost, virtualizer, loudness enhancer and 11 presets. |
| **Playback Controls** | Cross‑fade, gapless playback, playback speed 0.5×‑2.0×, sleep timer. |
| **Shuffle Modes** | Off, Smart, True Random, Artist Mix, Album Mix, Genre Mix, **Most Played First**, **Discovery Mode** (prioritises un‑played tracks). |
| **Library** | Full‑device scan, browse by Tracks, Albums (grid), Artists, Genres, Folders, Favorites. Playlist support, instant search. |
| **Design** | Material 3 / Material You – dynamic colors from album art, rotating vinyl animation, edge‑to‑edge UI, mini‑player, smooth animations, light/dark themes. |
| **Optional Online Services** (off by default) | Self‑hosted Navidrome/Subsonic/Jellyfin sync, LRCLIB lyrics lookup, Deezer artist image caching. |
| **Background Playback** | Media Session for Android Auto & smartwatch, head‑hardware button support. |
| **Accessibility** | Volume key navigation, talkback friendly labels. |

---

## 📦 Quick Start

| Platform | Command |
|---|---|
| **Build via GitHub Actions** (debug APK) | Push a tag matching `v*` (e.g., `git tag v1.1.0 && git push origin v1.1.0`). The workflow `.github/workflows/release.yml` will compile and upload `AuraPlay‑debug.apk`. |
| **Local build** (requires Android SDK 30 + JDK 21) | `./gradlew :app:assembleDebug --no-daemon` |
| **Obtainium auto‑update** | Add repository `Mylittlestories/AuraPlay` in Obtainium; it will watch for new `v*‑tags`. |

---

## 🛠️ Upgrade Roadmap (Phases 1‑4)

| Phase | What changed | Why it matters |
|---|---|---|
| **1 – Toolchain** | Gradle 8.5, Media 3 1.3.0, Lifecycle 2.8.2, Compose BOM 2024.07.00, Kotlin 1.6.0 compiler extension | Modern compiler, better ExoPlayer APIs, future‑proof Compose features. |
| **2 – UI & Dynamic Color** | `androidx.palette:palette:2.0.0`, Compose 1.6.0, Material You theming from album art | UI can automatically theme itself from each album’s cover. |
| **3 – Playback & Networking** | Cross‑fade & gapless playback, Retrofit 2 + Gson (optional Navidrome/Subsonic/Jellyfin) | Smooth track transitions, gap‑less album playback, future‑proof server‑sync capability. |
| **4 – CI / Release** | GitHub Actions workflow that installs Android 30, builds a debug APK, and makes the artifact available on every `v*‑tag` push. `github_repo` set for Obtainium auto‑update. | One‑click builds, automatic updates via Obtainium, ready for F‑Droid. |

---

## 🚀 GitHub Actions – Build Your APK

The repository ships a ready‑to‑run workflow (`.github/workflows/release.yml`). On every push of a tag like `v1.1.0`:

1. **Checkout** the code.  
2. **Set up JDK 21** (Temurin).  
3. **Install Android 30 SDK** and accept licences automatically.  
4. **Cache** Gradle dependencies.  
5. **Assemble** `app-debug.apk`.  
6. **Upload** the APK as a workflow artifact.

### Workflow file (`.github/workflows/release.yml`)

```yaml
name: AuraPlay Release

on:
  push:
    tags:
      - 'v*'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v3

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '21'

      - name: Set up Android SDK
        uses: actions/setup-android@v2
        with:
          api-level: 30   # installs platform‑30 + build‑tools‑30, accepts licences

      - name: Cache Gradle
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ runner.os }}-${{ hashFiles('**/gradle-wrapper.properties') }}

      - name: Assemble Debug
        run: ./gradlew :app:assembleDebug --no-daemon

      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: AuraPlay-debug
          path: app/build/outputs/apk/debug/app-debug.apk
```

**How to trigger it**

```bash
git tag v1.1.0          # bump version as you wish
git push origin v1.1.0
```

Then go to the **Actions** tab on GitHub, watch the run, and download `AuraPlay‑debug.apk` from the artifacts.

---

## 📦 F‑Droid Ready

AuraPlay is **FOSS‑first**: the core player is completely offline, and optional network features (Navidrome/Subsonic/Jellyfin, lyrics, Deezer images) are disabled by default and can be turned on only if the user wants them. This makes it eligible for F‑Droid.

### What you need to add for F‑Droid compliance

1. **`fdroid.cfg`** (optional) – place in the repo root with basic metadata, e.g.:

   ```ini
   # F-Droid metadata for AuraPlay
   # See https://docs.f-droid.org/en/docs/ConfigFiles/
   ```
2. **License file** – `LICENSE` (GPL‑3.0) is already present.
3. **No proprietary dependencies** – the only optional ones are behind a toggle and use Retrofit‑based APIs; they are not compiled into the default build.
4. **Release signing** – F‑Droid prefers release‑signed APKs. You can add a `signingConfigs.release` block in `build.gradle.kts` (a simple debug keystore works for testing) and modify the workflow to also build a **release** APK (`assembleRelease`). The workflow can then upload that artifact for you to submit to F‑Droid.

### Optional: Add a Release‑APK job to the workflow

If you want the CI to also produce a release‑signed APK (unsigned for now, but ready for your own keystore), add this after the debug step:

```yaml
      - name: Assemble Release (unsigned)
        run: ./gradlew :app:assembleRelease --no-daemon

      - name: Upload Release APK
        uses: actions/upload-artifact@v3
        with:
          name: AuraPlay-release
          path: app/build/outputs/apk/release/app-release-unsigned.apk
```

You can later replace the unsigned build with a real release keystore by adding a `signingConfigs.release` block and adjusting the `android` block in `build.gradle.kts`.

---

## 📄 License

AuraPlay is free software: you can redistribute and modify it under the terms of the **GNU General Public License v3.0** (see `LICENSE`).

---

*Happy listening, and thank you for supporting free software!* 🎧