package ru.wald_t.sound_of_nature.screens.fragments.control

import android.app.Application
import android.content.*
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import ru.wald_t.sound_of_nature.services.PlayAudioService
import ru.wald_t.sound_of_nature.dataModels.ControlDataModel

class ControlViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application
    lateinit var playAudioServiceBinder: PlayAudioService.MyBinder
    lateinit var mediaController: MediaControllerCompat
    private var coroutineState = false
    var liveData = MutableLiveData<ControlDataModel>()

    init {
        liveData.value = ControlDataModel(
            "Nothing",
            false)
    }

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            playAudioServiceBinder = (binder as PlayAudioService.MyBinder)
            mediaController = MediaControllerCompat(application, playAudioServiceBinder.getMediaSessionToken())
            mediaController.registerCallback(object : MediaControllerCompat.Callback() {
                override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                    super.onPlaybackStateChanged(state)
                    if (state != null) {
                        liveData.value = ControlDataModel(
                            mediaController.metadata.description.title.toString(),
                            state.state == PlaybackStateCompat.STATE_PLAYING)
                    }
                }

                override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                    super.onMetadataChanged(metadata)
                    if (metadata != null) {
                        liveData.value = ControlDataModel(
                            metadata.description.title.toString(),
                            mediaController.playbackState.state == PlaybackStateCompat.STATE_PLAYING)
                    }

                }
            })
        }

        override fun onServiceDisconnected(className: ComponentName) {
            coroutineState = false
        }
    }

    fun starEvent() {
        mediaController.transportControls.play()
    }

    fun stopEvent() {
        mediaController.transportControls.stop()
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