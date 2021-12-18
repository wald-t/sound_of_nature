package ru.wald_t.sound_of_nature.screens.country

import android.R.layout.simple_list_item_1
import android.app.Application
import android.content.*
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import ru.wald_t.sound_of_nature.dataModels.CountryDataModel
import ru.wald_t.sound_of_nature.services.PlayAudioService

class CountryViewModel(application: Application) : AndroidViewModel(application) {
    private val spinnerData = listOf("Night", "Morning", "Noon", "Evening")
    private val adapter = ArrayAdapter(application, simple_list_item_1, spinnerData)
    private val app = application
    private var playAudio: PlayAudioService = PlayAudioService()
    lateinit var playAudioServiceBinder: PlayAudioService.MyBinder
    lateinit var mediaController: MediaControllerCompat
    private var countryDataModel = CountryDataModel()
    private var prefs: SharedPreferences = app.getSharedPreferences("Settings", Context.MODE_PRIVATE)

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            playAudioServiceBinder = (binder as PlayAudioService.MyBinder)
            playAudio = playAudioServiceBinder.getService()
            mediaController = MediaControllerCompat(application, playAudioServiceBinder.getMediaSessionToken())
            playAudioServiceBinder.getService().setEvent("Country")
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

    fun getAdapter() : ArrayAdapter<String> {
        return adapter
    }

    private fun savePrefs() {
        val editor = prefs.edit()
        editor.putString("CountryParameters", getParametersToJson()).apply()
    }

    private fun loadPrefs() {
        val json = prefs.getString("CountryParameters", null)
        if (json != null) setParametersFromJson(json)
    }

    fun setHour(hour: Int) {
        countryDataModel.hour = hour
        sendParametersToPlayAudioService()
    }

    fun getHour(): Int {
        return countryDataModel.hour
    }

    private fun sendParametersToPlayAudioService() {
        playAudio.setParameter(countryDataModel)
    }

    private fun getParametersToJson(): String {
        return Gson().toJson(countryDataModel)
    }

    private fun setParametersFromJson(parameters: String) {
        countryDataModel = Gson().fromJson(parameters, CountryDataModel::class.java)
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