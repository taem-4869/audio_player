package com.taemallah.playme.audioPlayer.playBackService

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper.MediaStyle
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.taemallah.playme.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlayBackService : MediaSessionService() {

    @Inject
    lateinit var mediaSession : MediaSession

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    @OptIn(UnstableApi::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sessionToken = SessionToken(this, ComponentName(this, PlayBackService::class.java))
        val controllerFuture = MediaController.Builder(this,sessionToken)
            .setListener(object : MediaController.Listener{
            })
            .buildAsync()

        val startActivityIntent = Intent(this,MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,0,startActivityIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        MediaStyle(mediaSession).setCancelButtonIntent(pendingIntent)
            .build()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mediaSession.run {
            player.release()
            release()
        }
        INSTANCE = null
        super.onDestroy()
    }

    companion object{
        var INSTANCE: PlayBackService? = null
    }
}