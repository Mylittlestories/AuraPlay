#!/usr/bin/env bash
# Run this script to verify all required files exist before uploading to GitHub
cd "$(dirname "$0")"

echo "=========================================="
echo "  AuraPlay - File Verification"
echo "=========================================="
echo ""

MISSING=0

check() {
    if [ -f "$1" ]; then
        echo "  ✅ $1"
    else
        echo "  ❌ MISSING: $1"
        MISSING=$((MISSING + 1))
    fi
}

echo "── Root Files ──"
check "build.gradle.kts"
check "settings.gradle.kts"
check "gradle.properties"
check "gradle/wrapper/gradle-wrapper.properties"
check ".github/workflows/build-apk.yml"
echo ""

echo "── App Config ──"
check "app/build.gradle.kts"
check "app/proguard-rules.pro"
check "app/src/main/AndroidManifest.xml"
echo ""

echo "── Resources ──"
check "app/src/main/res/values/colors.xml"
check "app/src/main/res/values/strings.xml"
check "app/src/main/res/values/themes.xml"
check "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml"
check "app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml"
echo ""

echo "── Core ──"
check "app/src/main/java/com/auraplay/player/AuraPlayApp.kt"
check "app/src/main/java/com/auraplay/player/MainActivity.kt"
echo ""

echo "── Audio Engine ──"
check "app/src/main/java/com/auraplay/player/audio/AudioEngine.kt"
check "app/src/main/java/com/auraplay/player/audio/ShuffleManager.kt"
echo ""

echo "── Data Layer ──"
check "app/src/main/java/com/auraplay/player/data/local/AuraPlayDatabase.kt"
check "app/src/main/java/com/auraplay/player/data/local/Converters.kt"
check "app/src/main/java/com/auraplay/player/data/local/PlaylistDao.kt"
check "app/src/main/java/com/auraplay/player/data/local/TrackDao.kt"
check "app/src/main/java/com/auraplay/player/data/model/Album.kt"
check "app/src/main/java/com/auraplay/player/data/model/RepeatMode.kt"
check "app/src/main/java/com/auraplay/player/data/model/Track.kt"
check "app/src/main/java/com/auraplay/player/data/repository/MusicRepository.kt"
echo ""

echo "── DI / Playback ──"
check "app/src/main/java/com/auraplay/player/di/AppModule.kt"
check "app/src/main/java/com/auraplay/player/playback/PlaybackManager.kt"
check "app/src/main/java/com/auraplay/player/service/PlaybackService.kt"
check "app/src/main/java/com/auraplay/player/receiver/MediaButtonReceiver.kt"
echo ""

echo "── UI Screens ──"
check "app/src/main/java/com/auraplay/player/ui/navigation/Navigation.kt"
check "app/src/main/java/com/auraplay/player/ui/components/MiniPlayer.kt"
check "app/src/main/java/com/auraplay/player/ui/screens/HomeScreen.kt"
check "app/src/main/java/com/auraplay/player/ui/screens/LibraryScreen.kt"
check "app/src/main/java/com/auraplay/player/ui/screens/NowPlayingScreen.kt"
check "app/src/main/java/com/auraplay/player/ui/screens/EqualizerScreen.kt"
check "app/src/main/java/com/auraplay/player/ui/screens/SearchScreen.kt"
check "app/src/main/java/com/auraplay/player/ui/screens/QueueScreen.kt"
check "app/src/main/java/com/auraplay/player/ui/screens/SettingsScreen.kt"
check "app/src/main/java/com/auraplay/player/ui/screens/ShuffleSettingsScreen.kt"
check "app/src/main/java/com/auraplay/player/ui/screens/PlaylistsScreen.kt"
check "app/src/main/java/com/auraplay/player/ui/screens/DetailScreens.kt"
check "app/src/main/java/com/auraplay/player/ui/screens/TrackListItem.kt"
echo ""

echo "── Theme ──"
check "app/src/main/java/com/auraplay/player/ui/theme/Color.kt"
check "app/src/main/java/com/auraplay/player/ui/theme/Theme.kt"
check "app/src/main/java/com/auraplay/player/ui/theme/Type.kt"
echo ""

echo "── ViewModel ──"
check "app/src/main/java/com/auraplay/player/ui/viewmodel/MainViewModel.kt"
echo ""

echo "=========================================="
if [ $MISSING -eq 0 ]; then
    echo "  ✅ ALL 50 FILES PRESENT — Ready to push!"
else
    echo "  ❌ $MISSING FILE(S) MISSING — Fix before pushing"
fi
echo "=========================================="
