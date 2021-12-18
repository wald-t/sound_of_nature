package ru.wald_t.sound_of_nature.activities

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import ru.wald_t.sound_of_nature.R
import ru.wald_t.sound_of_nature.services.PlayAudioService

class MainActivity : FragmentActivity() {
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHostFragment.navController
        //Initializing PlayAudioService
        startService(Intent(this, PlayAudioService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, PlayAudioService::class.java))
    }
}