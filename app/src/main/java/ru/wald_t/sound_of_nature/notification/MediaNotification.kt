package ru.wald_t.sound_of_nature.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.session.MediaButtonReceiver
import android.R.drawable

fun sendNotification(context: Context, mediaSession: MediaSessionCompat, playbackState: Int): Notification {
    val controller: MediaControllerCompat = mediaSession.controller
    val mediaMetadata = controller.metadata
    val description = mediaMetadata.description
    val builder = getNotificationBuilder(context).apply {
        // Обязательные параметры, без которые Notification не будет работать
        setSmallIcon(android.R.mipmap.sym_def_app_icon)
        setContentTitle(description.title)
        setShowWhen(false)
        priority = NotificationCompat.PRIORITY_HIGH
        setOnlyAlertOnce(true)
        setStyle(androidx.media.app.NotificationCompat.MediaStyle()
            .setShowCancelButton(true)
            .setCancelButtonIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_STOP))
            .setMediaSession(mediaSession.sessionToken))
        setContentIntent(controller.sessionActivity)
        setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

    }

    if (playbackState == PlaybackStateCompat.STATE_PLAYING) builder.addAction(
        NotificationCompat.Action(
            drawable.ic_media_pause, "Pause",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                context,
                PlaybackStateCompat.ACTION_PAUSE
            )
        )
    ) else builder.addAction(
        NotificationCompat.Action(
            drawable.ic_media_play, "Play",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                context,
                PlaybackStateCompat.ACTION_PLAY
            )
        )
    )

    return builder.build()
}

fun getNotificationBuilder(context: Context): NotificationCompat.Builder {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = getNotificationChannel()
        val manager = getNotificationManager(context)
        manager.createNotificationChannel(channel)
        NotificationCompat.Builder(context, channel.id)
    } else {
        NotificationCompat.Builder(context)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getNotificationChannel(): NotificationChannel {
    val channelId = "3939"
    val name = "Channel"
    val description = "Description"
    val importance = NotificationManager.IMPORTANCE_HIGH
    val channel = NotificationChannel(channelId, name, importance)
    channel.description = description
    channel.enableLights(true)
    channel.lightColor = Color.BLUE
    return channel
}

fun getNotificationManager(context: Context): NotificationManager {
    return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}