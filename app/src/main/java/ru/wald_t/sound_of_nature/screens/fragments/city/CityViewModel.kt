package ru.wald_t.sound_of_nature.screens.fragments.city

import android.app.Application
import android.content.*
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import ru.wald_t.sound_of_nature.dataModels.CityDataModel
import ru.wald_t.sound_of_nature.services.PlayAudioService

class CityViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application
    private var playAudio: PlayAudioService = PlayAudioService()
    lateinit var playAudioServiceBinder: PlayAudioService.MyBinder
    lateinit var mediaController: MediaControllerCompat
    private var cityDataModel = CityDataModel()
    private var prefs: SharedPreferences = app.getSharedPreferences("Settings", Context.MODE_PRIVATE)

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            playAudioServiceBinder = (binder as PlayAudioService.MyBinder)
            playAudio = playAudioServiceBinder.getService()
            mediaController = MediaControllerCompat(application, playAudioServiceBinder.getMediaSessionToken())
            playAudio.setEvent("City")
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
        editor.putString("CityParameters", getParametersToJson()).apply()
    }

    private fun loadPrefs() {
        val json = prefs.getString("CityParameters", null)
        if (json != null) setParametersFromJson(json)
    }

    fun setTraffic(traffic: Int) {
        cityDataModel.traffic = traffic
        sendParametersToPlayAudioService()
    }

    fun getTraffic(): Int {
        return cityDataModel.traffic
    }

    fun setWalla(walla: Int) {
        cityDataModel.walla = walla
        sendParametersToPlayAudioService()
    }

    fun getWalla(): Int {
        return cityDataModel.walla
    }

    private fun sendParametersToPlayAudioService() {
        playAudio.setParameter(cityDataModel)
    }

    private fun getParametersToJson(): String {
        return Gson().toJson(cityDataModel)
    }

    private fun setParametersFromJson(parameters: String) {
        cityDataModel = Gson().fromJson(parameters, CityDataModel::class.java)
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