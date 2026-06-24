package com.auraplay.player

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AuraPlayApp : Application() {

    companion object {
        lateinit var instance: AuraPlayApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}