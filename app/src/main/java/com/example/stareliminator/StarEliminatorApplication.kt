package com.example.stareliminator

import android.app.Application
import com.example.stareliminator.audio.SoundManager
import com.example.stareliminator.data.local.AppDatabase

class StarEliminatorApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val soundManager by lazy { SoundManager(this) }

    override fun onCreate() {
        super.onCreate()
    }
}
