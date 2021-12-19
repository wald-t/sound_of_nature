package ru.wald_t.sound_of_nature

import android.app.Application
import android.content.Intent
import ru.wald_t.sound_of_nature.services.PlayAudioService

class App: Application(){
    override fun onCreate() {
        super.onCreate()
        //Initializing PlayAudioService
        startService(Intent(this, PlayAudioService::class.java))
    }
}