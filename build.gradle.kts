// Top-level build file for AuraPlay
// Keep plugin artifacts on the buildscript classpath so CI/Gradle can resolve
// Hilt and KSP reliably even when plugin marker resolution is flaky.
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.48")
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:1.9.20-1.0.14")
    }
}
