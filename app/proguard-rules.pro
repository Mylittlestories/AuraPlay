# ============================================================
# AuraPlay ProGuard Rules — Production
# ============================================================

# Keep Kotlin metadata
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions

# Keep Room entities and DAOs
-keep class com.auraplay.player.data.model.** { *; }
-keep class com.auraplay.player.data.local.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep ExoPlayer / Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Keep Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Coil
-keep class coil.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep data classes for serialization
-keepclassmembers class ** {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
}