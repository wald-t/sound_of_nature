package ru.wald_t.sound_of_nature.screens.control

import android.app.Application
import android.content.*
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import ru.wald_t.sound_of_nature.services.PlayAudioService
import ru.wald_t.sound_of_nature.dataModels.ControlDataModel


class ControlViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application
    private var playAudio: PlayAudioService = PlayAudioService()
    private var coroutineState = false

    var liveData = MutableLiveData<ControlDataModel>()

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            playAudio = (binder as PlayAudioService.MyBinder).getService()
            playAudio.liveData.observeForever { liveData.value = playAudio.liveData.value }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            coroutineState = false
        }
    }

    fun startLastEvent() {
        playAudio.startLastEvent()
    }

    fun stopEvent() {
        playAudio.stopEvent()
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