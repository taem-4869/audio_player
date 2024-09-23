package com.taemallah.playme.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import com.taemallah.playme.MainActivity
import com.taemallah.playme.audioPlayer.audioController.AudioServiceHandler
import com.taemallah.playme.mainScreen.data.localDataProvider.LocalDataProvider
import com.taemallah.playme.mainScreen.data.repository.LocalAudioRepoImpl
import com.taemallah.playme.mainScreen.domain.repository.LocalAudioRepo
import com.taemallah.playme.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioModule {

    @Provides
    @Singleton
    fun providesAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun providesExoPlayer(@ApplicationContext context: Context, audioAttributes: AudioAttributes): ExoPlayer =
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(Constants.AUDIO_SEEK_BACK_INCREMENT)
            .setSeekForwardIncrementMs(Constants.AUDIO_SEEK_FORWARD_INCREMENT)
            .setAudioAttributes(audioAttributes,true)
            .setTrackSelector(DefaultTrackSelector(context))
            .build()

    @Provides
    @Singleton
    fun providesMediaSession(@ApplicationContext context: Context, exoPlayer: ExoPlayer): MediaSession {
        val intent = Intent(context,MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context,1,intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return MediaSession.Builder(context,exoPlayer)
            .setSessionActivity(pendingIntent)
            .build()
    }

    @Provides
    @Singleton
    fun providesLocalAudioRepo(provider : LocalDataProvider): LocalAudioRepo = LocalAudioRepoImpl(provider)

}