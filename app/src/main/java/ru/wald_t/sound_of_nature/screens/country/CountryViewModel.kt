package ru.wald_t.sound_of_nature.screens.country

import android.R
import android.app.Application
import android.content.*
import android.os.IBinder
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import ru.wald_t.sound_of_nature.dataModels.CountryDataModel
import ru.wald_t.sound_of_nature.services.PlayAudioService

class CountryViewModel(application: Application) : AndroidViewModel(application) {
    private val spinnerData = listOf("Night", "Morning", "Noon", "Evening")
    private val adapter = ArrayAdapter(application, R.layout.simple_list_item_1, spinnerData)
    private val app = application
    private var playAudio: PlayAudioService = PlayAudioService()
    private var countryDataModel = CountryDataModel()
    private var prefs: SharedPreferences = app.getSharedPreferences("Settings", Context.MODE_PRIVATE)

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            playAudio = (binder as PlayAudioService.MyBinder).getService()
            playAudio.setEvent("Country")
        }

        override fun onServiceDisconnected(className: ComponentName) {}
    }

    fun getAdapter() : ArrayAdapter<String> {
        return adapter
    }

    init {
        val json = prefs.getString("CountryParameters", null)
        if (json != null) setParametersFromJson(json)
        sendParametersToPlayAudioService()
    }

    fun savePrefs() {
        val editor = prefs.edit()
        editor.putString("CountryParameters", getParametersToJson()).apply()
    }

    fun setHour(hour: Int) {
        countryDataModel.hour = hour
        sendParametersToPlayAudioService()
    }

    fun getHour(): Int {
        return countryDataModel.hour
    }

    private fun sendParametersToPlayAudioService() {
        playAudio.countrySetParameter(countryDataModel)
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
    }


}