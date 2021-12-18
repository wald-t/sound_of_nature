package ru.wald_t.sound_of_nature.screens.forest

import android.app.Application
import android.content.ComponentName
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import ru.wald_t.sound_of_nature.dataModels.ForestDataModel
import ru.wald_t.sound_of_nature.services.PlayAudioService

class ForestViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application
    lateinit var playAudioServiceBinder: PlayAudioService.MyBinder
    var playAudio = PlayAudioService()
    lateinit var mediaController: MediaControllerCompat
    private var forestDataModel = ForestDataModel()
    private var prefs: SharedPreferences = app.getSharedPreferences("Settings", MODE_PRIVATE)

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            playAudioServiceBinder = (binder as PlayAudioService.MyBinder)
            playAudio = playAudioServiceBinder.getService()
            mediaController = MediaControllerCompat(application, playAudioServiceBinder.getMediaSessionToken())
            playAudio.setEvent("Forest")
            sendParametersToPlayAudioService()
            mediaController.transportControls.play()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            savePrefs()
        }
    }

    init {
        loadPrefs()
    }

    private fun savePrefs() {
        val editor = prefs.edit()
        editor.putString("ForestParameters", getParametersToJson()).apply()
    }

    private fun loadPrefs() {
        val json = prefs.getString("ForestParameters", null)
        if (json != null) setParametersFromJson(json)
    }

    fun setRain(rain: Int) {
        forestDataModel.rain = rain
        sendParametersToPlayAudioService()
    }

    fun getRain(): Int {
        return forestDataModel.rain
    }

    fun setWind(wind: Int) {
        forestDataModel.wind = wind
        sendParametersToPlayAudioService()
    }

    fun getWind(): Int {
        return forestDataModel.wind
    }

    fun setCover(cover: Int) {
        forestDataModel.cover = cover
        sendParametersToPlayAudioService()
    }

    fun getCover(): Int {
        return forestDataModel.cover
    }

    private fun sendParametersToPlayAudioService() {
        playAudio.setParameter(forestDataModel)
    }

    private fun getParametersToJson(): String {
        return Gson().toJson(forestDataModel)
    }

    private fun setParametersFromJson(parameters: String) {
        forestDataModel = Gson().fromJson(parameters, ForestDataModel::class.java)
    }

    fun bindService() {
        app.bindService(
            Intent(app, PlayAudioService::class.java), mConnection,
            AppCompatActivity.BIND_AUTO_CREATE
        )
    }

    fun unbindService() {
        app.unbindService(mConnection)
        savePrefs()
    }
}