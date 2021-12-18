package ru.wald_t.sound_of_nature.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.session.PlaybackState
import android.os.IBinder
import org.fmod.FMOD
import android.os.Binder
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.session.MediaButtonReceiver
import ru.wald_t.sound_of_nature.BuildConfig
import ru.wald_t.sound_of_nature.activities.MainActivity
import ru.wald_t.sound_of_nature.dataModels.CityDataModel
import ru.wald_t.sound_of_nature.dataModels.CountryDataModel
import ru.wald_t.sound_of_nature.dataModels.ForestDataModel
import ru.wald_t.sound_of_nature.notification.sendNotification

class PlayAudioService : Service() {
    private val mBinder: IBinder = MyBinder()
    private var event: String = "Nothing"
    val metadataBuilder = MediaMetadataCompat.Builder()
    val stateBuilder: PlaybackStateCompat.Builder = PlaybackStateCompat.Builder()
        .setActions(
            PlaybackStateCompat.ACTION_PLAY
                    or PlaybackStateCompat.ACTION_STOP
                    or PlaybackStateCompat.ACTION_PAUSE
                    or PlaybackStateCompat.ACTION_PLAY_PAUSE
        )

    lateinit var mediaSession: MediaSessionCompat

    private val mThread = object : Thread(){
        override fun run() {
            main()
        }
    }

    inner class MyBinder : Binder() {
        fun getService() : PlayAudioService{
            return this@PlayAudioService
        }
        fun getMediaSessionToken(): MediaSessionCompat.Token{
            return mediaSession.sessionToken
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSessionCompat(this, "PlayAudioService")
        mediaSession.setCallback(mediaSessionCallback)
        val activityIntent = Intent(this, MainActivity::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaSession.setSessionActivity(
                PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            )
        } else {
            mediaSession.setSessionActivity(
                PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            )
        }

        val metadata = metadataBuilder
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, event)
            .build()
        mediaSession.setMetadata(metadata)
        mediaSession.setPlaybackState(
            stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED,
                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1F).build())

        FMOD.init(this)
        mThread.start()
        setStateCreate()
    }

    private val mediaSessionCallback = object : MediaSessionCompat.Callback(){
        override fun onPlay() {
            super.onPlay()
            startEvent()
            val metadata = metadataBuilder
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, event)
                .build()
            mediaSession.setMetadata(metadata)
            mediaSession.isActive = true
            mediaSession.setPlaybackState(
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1F).build())
        }

        override fun onPause() {
            super.onPause()
            val metadata = metadataBuilder
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, event)
                .build()
            mediaSession.setMetadata(metadata)
            mediaSession.setPlaybackState(
                stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1F).build())
            pauseEvent()
        }

        override fun onStop() {
            super.onStop()
            mediaSession.isActive = false
            mediaSession.setPlaybackState(
                stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1F).build())
            stopEvent()
        }
    }

    private fun refreshNotificationAndForegroundStatus(playbackState: Int) {
        when(playbackState) {
            PlaybackStateCompat.STATE_PLAYING -> startForeground(1, sendNotification(
                this,
                mediaSession,
                playbackState
            ))
            PlaybackStateCompat.STATE_PAUSED -> {
                NotificationManagerCompat.from(this)
                    .notify(1, sendNotification(this, mediaSession, playbackState))
                stopForeground(false)
            }
            else -> stopForeground(true)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setStateStart()
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.controller.transportControls.stop()
        setStateDestroy()
        try {
            mThread.join()
        } catch (e: InterruptedException) {}
        FMOD.close()
    }

    fun setEvent(event: String) {
        this.event = event
    }

    fun startEvent() {
        if (event != mediaSession.controller.metadata.description.title || mediaSession.controller.playbackState.state != PlaybackStateCompat.STATE_PLAYING){
            when(event){
                "Forest" -> {
                    setEventState(0, 1)
                    setEventState(1, 0)
                    setEventState(2, 0)
                    refreshNotificationAndForegroundStatus(PlaybackState.STATE_PLAYING)
                }
                "Country" -> {
                    setEventState(0, 0)
                    setEventState(1, 1)
                    setEventState(2, 0)
                    refreshNotificationAndForegroundStatus(PlaybackState.STATE_PLAYING)
                }
                "City" -> {
                    setEventState(0, 0)
                    setEventState(1, 0)
                    setEventState(2, 1)
                    refreshNotificationAndForegroundStatus(PlaybackState.STATE_PLAYING)
                }
                else -> pauseEvent()
            }
        }

    }

    fun pauseEvent() {
        setEventState(0, 0)
        setEventState(1, 0)
        setEventState(2, 0)
        refreshNotificationAndForegroundStatus(PlaybackStateCompat.STATE_PAUSED)
    }

    fun stopEvent() {
        setEventState(0, 0)
        setEventState(1, 0)
        setEventState(2, 0)
        refreshNotificationAndForegroundStatus(PlaybackStateCompat.STATE_STOPPED)
    }

    fun setParameter(forestDataModel: ForestDataModel) {
        forestSetParameter(
            forestDataModel.getRainFloat(),
            forestDataModel.getWindFloat(),
            forestDataModel.getCoverFloat())
    }

    fun setParameter(countryDataModel: CountryDataModel) {
        countrySetParameter(countryDataModel.getHourFloat())
    }

    fun setParameter(cityDataModel: CityDataModel) {
        citySetParameter(
            cityDataModel.getTrafficFloat(),
            cityDataModel.getWallaFloat())
    }


    private external fun getEventState(): Int
    private external fun setEventState(event: Int, state: Int)
    external fun getButtonLabel(index: Int): String?
    external fun buttonDown(index: Int)
    external fun buttonUp(index: Int)
    private external fun forestSetParameter(rain: Float, wind: Float, cover: Float)
    private external fun countrySetParameter(index: Float)
    private external fun citySetParameter(traffic: Float, walla: Float)
    private external fun setStateCreate()
    private external fun setStateStart()
    private external fun setStateStop()
    private external fun setStateDestroy()
    private external fun main()

    init {
        for (lib:String in BuildConfig.FMOD_LIBS)
        {
            System.loadLibrary(lib)
        }
        System.loadLibrary("example")
    }


}