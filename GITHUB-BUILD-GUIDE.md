# 🚀 Complete Step-by-Step: Build AuraPlay APK via GitHub Actions

> **What you need:** A web browser + a free GitHub account. Nothing else.

---

## STEP 1 — Create a Free GitHub Account

1. Go to **https://github.com**
2. Click **Sign Up** (top-right)
3. Follow the prompts (email, password, verify)
4. Free plan is perfect — no payment needed

---

## STEP 2 — Create a New Repository

1. Log into GitHub
2. Click the **+** icon (top-right corner) → **New repository**
3. Fill in:
   - **Repository name:** `AuraPlay`
   - **Description:** `Premium Android Music Player`
   - **Visibility:** `Public` (required for free GitHub Actions)
   - ✅ Check **Add a README file**
4. Click **Create repository**

---

## STEP 3 — Upload All Project Files

You have two options:

### Option A — Upload via GitHub Web (Easiest, no software needed)

For **each file** listed below, do this:
1. In your repo, click **Add file** → **Create new file**
2. In the **file name** box at the top, type the full path (e.g. `app/build.gradle.kts`)
3. Paste the file contents in the editor
4. Scroll down, click **Commit changes**

#### The files to create (in this exact order):

**Root files:**
```
build.gradle.kts
settings.gradle.kts
gradle.properties
```

**Gradle wrapper:**
```
gradle/wrapper/gradle-wrapper.properties
```

**App build:**
```
app/build.gradle.kts
app/proguard-rules.pro
```

**Android manifest:**
```
app/src/main/AndroidManifest.xml
```

**Resources:**
```
app/src/main/res/values/colors.xml
app/src/main/res/values/strings.xml
app/src/main/res/values/themes.xml
app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
```

**Kotlin source files (under `app/src/main/java/com/auraplay/player/`):**
```
AuraPlayApp.kt
MainActivity.kt
audio/AudioEngine.kt
audio/ShuffleManager.kt
data/local/AuraPlayDatabase.kt
data/local/Converters.kt
data/local/PlaylistDao.kt
data/local/TrackDao.kt
data/model/Album.kt
data/model/RepeatMode.kt
data/model/Track.kt
data/repository/MusicRepository.kt
di/AppModule.kt
playback/PlaybackManager.kt
receiver/MediaButtonReceiver.kt
service/PlaybackService.kt
ui/components/MiniPlayer.kt
ui/navigation/Navigation.kt
ui/screens/DetailScreens.kt
ui/screens/EqualizerScreen.kt
ui/screens/HomeScreen.kt
ui/screens/LibraryScreen.kt
ui/screens/NowPlayingScreen.kt
ui/screens/PlaylistsScreen.kt
ui/screens/QueueScreen.kt
ui/screens/SearchScreen.kt
ui/screens/SettingsScreen.kt
ui/screens/ShuffleSettingsScreen.kt
ui/screens/TrackListItem.kt
ui/theme/Color.kt
ui/theme/Theme.kt
ui/theme/Type.kt
ui/viewmodel/MainViewModel.kt
```

**GitHub Actions workflow:**
```
.github/workflows/build-apk.yml
```

---

### Option B — Upload via Git Command Line (Faster for bulk upload)

If you have Git installed on your computer:

```bash
# 1. Download/clone the AuraPlay folder to your computer

# 2. Open terminal in the AuraPlay folder
cd AuraPlay

# 3. Initialize git
git init
git branch -M main

# 4. Add all files
git add .
git commit -m "Initial commit - AuraPlay music player"

# 5. Connect to YOUR GitHub repo (replace YOUR_USERNAME)
git remote add origin https://github.com/YOUR_USERNAME/AuraPlay.git

# 6. Push everything
git push -u origin main
```

> **Don't have Git?** Download from https://git-scm.com/downloads
> Or just use **Option A** (web upload).

---

## STEP 4 — Enable GitHub Actions

1. Go to your repo on GitHub
2. Click the **Settings** tab (top menu bar)
3. In the left sidebar, click **Actions** → **General**
4. Under "Actions permissions", select:
   - **Allow all actions and reusable workflows**
5. Click **Save**

> This is usually enabled by default on public repos, but check to be sure.

---

## STEP 5 — Trigger the Build

### Automatic trigger:
The build runs automatically when you push the code (Step 3).
If you already pushed, it's probably building right now!

### Manual trigger:
1. Go to your repo
2. Click the **Actions** tab (top menu bar)
3. In the left sidebar, click **Build AuraPlay APK**
4. Click the **Run workflow** button (right side)
5. Select branch: **main**
6. Click the green **Run workflow** button

---

## STEP 6 — Wait for the Build

1. Stay on the **Actions** tab
2. You'll see a workflow run appear with a yellow circle ⏳ (in progress)
3. Click on it to watch the progress
4. Wait **5-10 minutes** for all steps to complete
5. When done, the circle turns green ✅

> If it turns red ❌, click on it to see the error log. The most common issue is a missing file — double-check all files were uploaded correctly.

---

## STEP 7 — Download the APK

1. Click on the **green ✅ completed workflow run**
2. Scroll down to the **Artifacts** section
3. You'll see **AuraPlay-debug**
4. Click it to **download the ZIP**
5. **Unzip** the downloaded file
6. Inside you'll find **`app-debug.apk`** — that's your AuraPlay!

---

## STEP 8 — Install on Your Android Phone

### Transfer the APK to your phone:
- **USB cable** — plug phone into computer, copy the file
- **Email** — email the APK to yourself, open on phone
- **Google Drive / Dropbox** — upload, then download on phone
- **WhatsApp / Telegram** — send the APK file to yourself

### Install:
1. On your phone, tap the `.apk` file
2. If prompted, go to **Settings → Security → Install unknown apps**
3. Enable it for your file manager or browser
4. Tap **Install**
5. Open **AuraPlay**
6. Grant the **Music/Audio** permission when asked
7. Tap **Scan for Music**
8. 🎵 **Enjoy your music!**

---

## 🔁 How to Rebuild After Changes

Every time you push new code to `main`, the APK rebuilds automatically.

```bash
# Make your changes to the files, then:
git add .
git commit -m "Updated feature X"
git push
```

Go to **Actions** → download the new APK.

---

## ❓ Troubleshooting

| Problem | Solution |
|---------|----------|
| **Actions tab not showing** | Go to Settings → Actions → General → enable "Allow all actions" |
| **Build failed (red ❌)** | Click the run → click the failed job → read the error at the bottom |
| **"No artifacts" after build** | The APK step may have failed — check if all files were uploaded correctly |
| **APK won't install on phone** | Go to Settings → Security → enable "Unknown sources" or "Install unknown apps" |
| **"App not installed"** | Uninstall any previous version first, then install again |
| **Build takes too long** | First build is slow (~10 min). Subsequent builds use cache (~3-5 min) |
| **"License not accepted" error** | This is handled in the workflow. If it still fails, re-run the workflow |

---

## 📋 Quick Checklist

- [ ] GitHub account created
- [ ] Repository `AuraPlay` created (Public)
- [ ] All project files uploaded
- [ ] `.github/workflows/build-apk.yml` exists in repo
- [ ] Actions enabled in repo Settings
- [ ] Build triggered (auto or manual)
- [ ] Green checkmark ✅ on Actions tab
- [ ] APK downloaded from Artifacts
- [ ] APK installed on phone
- [ ] Music scanned and playing! 🎶
