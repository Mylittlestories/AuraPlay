# AuraPlay — F-Droid Publication Plan

Status: **ready to submit** · App id: `com.auraplay.player` · License: GPL-3.0-or-later

AuraPlay qualifies for F-Droid: 100% FOSS dependencies, no proprietary services, no tracking,
source-buildable with the standard Android SDK/NDK, versioned from `gradle.properties`.

---

## Path A — IzzyOnDroid (fast, recommended first step)

[IzzyOnDroid](https://apt.izzysoft.de/applist) is an F-Droid client repository that publishes
apps directly from your GitHub releases (signed with *your* key, which you already have set up).

1. Your releases already contain `AuraPlay-<version>-universal.apk` ✓
2. Fastlane metadata is in place (`fastlane/metadata/android/en-US/`) ✓
3. Open a submission issue: **https://gitlab.com/IzzyOnDroid/repo/-/issues/new**
   with:
   - Repo: `https://github.com/Mylittlestories/AuraPlay`
   - App name: AuraPlay, package: `com.auraplay.player`
   - License: GPL-3.0
   - Note that releases are signed with a stable key and include fastlane metadata
4. Users get OTA updates through the official F-Droid client within a day or two.

## Path B — the main F-Droid repository

1. **Check upstream policy** — AuraPlay is a rebrand of PixelPlayerOSS (GPL-3.0). F-Droid
   accepts forks/rebrands when they are *substantively developed*: AuraPlay adds AuraShuffle,
   Mood Radio, the Sound Engine hub and the DRVsoft branding, so it qualifies — but state the
   upstream relationship openly in the submission (you already do in README + fastlane).
2. Open a *packaging request* (RFP): **https://gitlab.com/fdroid/fdroiddata/-/issues**
   with the template below, or send a merge request adding the metadata file yourself.
3. F-Droid builds from source with its own signing key. Nothing to change in the app.
4. First build can take a few weeks (volunteer queue). After acceptance, updates are automatic
   whenever a new `v*` tag is pushed.

### Proposed metadata (fdroiddata style) — `metadata/com.auraplay.player.yml`

```yaml
Categories:
  - Multimedia
  - Audio
License: GPL-3.0-or-later
AuthorName: DRVsoft
AuthorWebSite: https://github.com/Mylittlestories
SourceCode: https://github.com/Mylittlestories/AuraPlay
IssueTracker: https://github.com/Mylittlestories/AuraPlay/issues
Changelog: https://github.com/Mylittlestories/AuraPlay/releases
Donate: https://github.com/sponsors/lostf1sh

AutoName: AuraPlay
Summary: Offline-first music player with hi-fi USB DAC engine
Description: |-
    AuraPlay is an offline-first Android music player with a hi-fi sound
    engine, intelligent shuffle and mood-based playback.

    Engineered by DRVsoft. Built on the open-source PixelPlayerOSS
    (GPL-3.0) — credit to @lostf1sh and contributors.

    Features: USB DAC routing, float PCM output, FFmpeg hi-res decoders,
    equalizer with AutoEQ headphone correction, in-app preamp and
    true-peak limiter, Pure Direct mode, live spectrum visualizer,
    AuraShuffle weighted whole-library shuffle, offline Mood Radio,
    lyrics, Navidrome/Subsonic + Jellyfin sync (optional),
    encrypted backups, widgets, Material 3 dynamic theming.

    No ads, no tracking, no account required.

RepoType: git
Repo: https://github.com/Mylittlestories/AuraPlay.git

Builds:
  - versionName: 2.3.0
    versionCode: 9
    commit: v2.3.0
    subdir: app
    gradle:
      - yes
    output: build/outputs/apk/release/app-release.apk

AutoUpdateMode: Version v%v
UpdateCheckMode: Tags v.*
CurrentVersion: 2.3.0
CurrentVersionCode: 9
```

## Checklist (already done in this repo)

- [x] GPL-3.0 license + upstream credit (README, About screen, fastlane)
- [x] Fastlane metadata: title, short + full description, changelogs, icon
- [x] No proprietary dependencies (verified: FOSS-only libraries incl. jitpack ones)
- [x] Reproducible versioning (`APP_VERSION_NAME` / `APP_VERSION_CODE` in gradle.properties)
- [x] Tag-driven releases with attached universal APK (`v*` tags)
- [ ] *You:* open the IzzyOnDroid issue / F-Droid RFP (links above)
- [ ] *(optional)* Add real screenshots to `fastlane/metadata/android/en-US/images/phoneScreenshots/`

## Note on screenshots

The repo currently ships no store screenshots (the old ones were of the upstream app).
Take 3–5 phone screenshots of Home (aura section), Sound Engine, Now Playing and Library,
then drop them into `phoneScreenshots/` before submitting — stores convert much better.
