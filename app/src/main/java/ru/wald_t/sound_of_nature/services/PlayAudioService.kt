package ru.wald_t.sound_of_nature.services

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
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
import ru.wald_t.sound_of_nature.screens.MainActivity
import ru.wald_t.sound_of_nature.dataModels.CityDataModel
import ru.wald_t.sound_of_nature.dataModels.CountryDataModel
import ru.wald_t.sound_of_nature.dataModels.ForestDataModel
import ru.wald_t.sound_of_nature.notification.sendNotification
import android.media.AudioAttributes

import android.media.AudioFocusRequest
import android.media.AudioManager.OnAudioFocusChangeListener


class PlayAudioService : Service() {
    private val mBinder: IBinder = MyBinder()
    private var event: String = "Nothing"

    lateinit var metadataBuilder: MediaMetadataCompat.Builder
    lateinit var stateBuilder: PlaybackStateCompat.Builder
    lateinit var mediaSession: MediaSessionCompat
    lateinit var audioManager: AudioManager
    lateinit var audioFocusChangeListener: OnAudioFocusChangeListener
    lateinit var becomingNoisyReceiver: BroadcastReceiver

    private val mThread = object : Thread(){
        override fun run() {
            main() //FMOD main()
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

        // for Change AudioFocus
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioFocusChangeListener =
            OnAudioFocusChangeListener { focusChange ->
                when(focusChange){
                    AudioManager.AUDIOFOCUS_GAIN -> mediaSessionCallback.onPlay()
                    else -> mediaSessionCallback.onPause()
                }
            }

        // onPause() after unplug headphones
        becomingNoisyReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                        mediaSessionCallback.onPause()
                    }
                }
            }
        }

        // for AudioService controlling
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

        metadataBuilder = MediaMetadataCompat.Builder()
        val metadata = metadataBuilder
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, event)
            .build()
        mediaSession.setMetadata(metadata)

        stateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_STOP
                        or PlaybackStateCompat.ACTION_PAUSE
            )
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

            //request Audio Focus
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.requestAudioFocus(
                    AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(audioFocusChangeListener)
                        .build()
                )
            } else {
                audioManager.requestAudioFocus(
                    audioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
            }

            startEvent()
            val metadata = metadataBuilder
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, event)
                .build()
            mediaSession.setMetadata(metadata)
            mediaSession.isActive = true
            mediaSession.setPlaybackState(
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1F).build())

            //Stop after unplug headphones
            registerReceiver(
                becomingNoisyReceiver,
                IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            )
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

            //Abandon Audio Focus
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.abandonAudioFocusRequest(AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).build())
            } else {
                audioManager.abandonAudioFocus(audioFocusChangeListener)
            }

            unregisterReceiver(becomingNoisyReceiver)
        }

        override fun onStop() {
            super.onStop()
            mediaSession.isActive = false
            mediaSession.setPlaybackState(
                stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1F).build())
            stopEvent()

            //Abandon Audio Focus
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.abandonAudioFocusRequest(AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).build())
            } else {
                audioManager.abandonAudioFocus(audioFocusChangeListener)
            }

            unregisterReceiver(becomingNoisyReceiver)
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


    private external fun setEventState(event: Int, state: Int)
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