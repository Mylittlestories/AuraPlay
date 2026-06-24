# 🔨 Build AuraPlay APK — No Android Studio Required

Three methods, pick whichever suits you:

---

## Method 1 — GitHub Actions (Zero Local Setup) ⭐ Recommended

> **No software to install. Just push to GitHub and download the APK.**

### Steps:

1. **Create a GitHub repo** (free at github.com)

2. **Upload the project:**
   ```bash
   cd AuraPlay
   git init
   git add .
   git commit -m "Initial AuraPlay build"
   git remote add origin https://github.com/YOUR_USERNAME/AuraPlay.git
   git push -u origin main
   ```

3. **Watch it build automatically:**
   - Go to your repo → **Actions** tab
   - The workflow `build-apk.yml` starts on every push
   - Wait ~5-10 minutes for the build to complete

4. **Download your APK:**
   - Click the completed workflow run
   - Scroll to **Artifacts** section
   - Download **`AuraPlay-debug`** → unzip → you have your APK!

### Trigger a build manually:
   - Go to **Actions** → **Build AuraPlay APK** → **Run workflow** → **Run**

---

## Method 2 — Local Build Script (One Command)

> **Just need JDK 17. The script auto-downloads the Android SDK.**

### Prerequisites:
- **JDK 17** (that's it!)

### Install JDK 17:

**macOS:**
```bash
brew install openjdk@17
```

**Ubuntu / Debian:**
```bash
sudo apt update
sudo apt install -y openjdk-17-jdk curl unzip
```

**Fedora / RHEL:**
```bash
sudo dnf install -y java-17-openjdk-devel curl unzip
```

**Windows (PowerShell as Admin):**
```powershell
winget install EclipseAdoptium.Temurin.17.JDK
```

### Build:
```bash
cd AuraPlay
./build-apk.sh
```

The script will:
- ✓ Verify Java is installed
- ✓ Auto-download Android SDK if missing
- ✓ Install required SDK packages
- ✓ Set up Gradle wrapper
- ✓ Build the APK
- ✓ Copy it to `AuraPlay-debug.apk` in the project root

**That's it. One command.**

---

## Method 3 — Docker (Fully Isolated)

> **Nothing to install except Docker. Clean build every time.**

### Prerequisites:
- **Docker** (docker.com/get-docker)

### Build:
```bash
cd AuraPlay

# Build the Docker image (installs JDK + Android SDK inside)
docker build -t auraplay-builder .

# Run the build and extract the APK
docker run --rm -v $(pwd)/output:/output auraplay-builder

# Your APK is now at:
ls -la output/AuraPlay-debug.apk
```

### Windows (PowerShell):
```powershell
cd AuraPlay
docker build -t auraplay-builder .
docker run --rm -v ${PWD}/output:/output auraplay-builder
dir output\AuraPlay-debug.apk
```

---

## Method 4 — Manual CLI Build

> **For those who already have JDK + Android SDK installed.**

```bash
cd AuraPlay

# If you have the Gradle wrapper JAR, use:
./gradlew assembleDebug

# Or if you have Gradle installed globally:
gradle assembleDebug

# APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

### If `gradlew` fails (missing wrapper JAR):
```bash
# Install Gradle:  https://gradle.org/install/
# Then generate the wrapper:
gradle wrapper --gradle-version 8.4
./gradlew assembleDebug
```

---

## 📱 Install the APK on Your Phone

1. **Transfer the APK** to your Android device (USB, email, cloud drive, etc.)
2. On your phone, go to **Settings → Security** → enable **Unknown Sources** (or "Install unknown apps" for your file manager)
3. Open the `.apk` file and tap **Install**
4. Launch **AuraPlay** and grant music permission
5. Tap **Scan for Music** → enjoy!

---

## ❓ Troubleshooting

| Problem | Fix |
|---------|-----|
| `JAVA_HOME not set` | Install JDK 17, then: `export JAVA_HOME=$(/usr/libexec/java_home -v 17)` (macOS) or `export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64` (Linux) |
| `SDK location not found` | Run `build-apk.sh` which auto-downloads it, OR set `export ANDROID_HOME=$HOME/Android/Sdk` |
| `gradlew: Permission denied` | Run `chmod +x gradlew` |
| `License not accepted` | Run `yes \| $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses` |
| Build runs out of memory | Add `org.gradle.jvmargs=-Xmx4g` to `gradle.properties` |
| `gradle-wrapper.jar not found` | Use Method 2 (`build-apk.sh`) which handles this automatically |
