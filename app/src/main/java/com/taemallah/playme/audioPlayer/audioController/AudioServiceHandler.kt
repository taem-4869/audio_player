package com.taemallah.playme.audioPlayer.audioController

import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.taemallah.playme.mainScreen.domain.model.MyAudio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class AudioServiceHandler @Inject constructor(
    val exoPlayer: ExoPlayer
) : Player.Listener {

    private val audioList :MutableList<MyAudio> = mutableListOf()
    private val audioHandlerScope = CoroutineScope(Dispatchers.Main)
    val audioHandlerEventsFlow = MutableStateFlow<AudioHandlerEvents?>(null)

    init {
        exoPlayer.addListener(this)
    }

    fun setMediaItems(mediaItems: List<MediaItem>){
        audioList.clear()
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
        audioHandlerScope.launch {
            audioHandlerEventsFlow.emit(AudioHandlerEvents.MediaItemsChanged(audioList))
        }
    }

    fun setMediaItemsFromAudios(audios: List<MyAudio>){
        audioList.clear()
        audioList.addAll(audios)
        val mediaItems = audios.map { MediaItem.fromUri(it.uri) }
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
        audioHandlerScope.launch {
            audioHandlerEventsFlow.emit(AudioHandlerEvents.MediaItemsChanged(audioList))
        }
    }

    fun setMediaItemsFromUris(mediaUris: List<Uri>){
        audioList.clear()
        val mediaItems = mediaUris.map { MediaItem.fromUri(it) }
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
        audioHandlerScope.launch {
            audioHandlerEventsFlow.emit(AudioHandlerEvents.MediaItemsChanged(audioList))
        }
    }

    fun seekTo(position: Long){
        exoPlayer.seekTo(position)
    }

    fun seekBack() {
        exoPlayer.seekBack()
    }

    fun seekForward() {
        exoPlayer.seekForward()
    }

    fun playOrPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        }else{
            exoPlayer.play()
        }
    }

    fun startAudio(index: Int) {
        if (exoPlayer.currentMediaItemIndex == index) {
            playOrPause()
        }else{
            exoPlayer.seekToDefaultPosition(index)
            exoPlayer.playWhenReady = true
        }
    }

    fun clearMediaItems() {
        exoPlayer.clearMediaItems()
    }

    override fun onPlayerError(error: PlaybackException) {
        Log.e("kid_e","play back error : \n"+error.errorCodeName)
        if(error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND){
            exoPlayer.seekToNextMediaItem()
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            audioHandlerScope.launch {
                audioHandlerEventsFlow.emit(AudioHandlerEvents.ErrorUnableToPlayAudio)
            }
        }
    }

    override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
        val isCurrentItemIndexChanged = availableCommands.containsAny(
            Player.COMMAND_SEEK_TO_MEDIA_ITEM,
            Player.COMMAND_SEEK_TO_DEFAULT_POSITION,
            Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
            Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
        )
        Log.i("kid_e","commands changed\n$availableCommands")
        if (isCurrentItemIndexChanged){
            audioHandlerScope.launch {
                try{
                    audioHandlerEventsFlow.emit(AudioHandlerEvents.CurrentItemIndexChanged(exoPlayer.currentMediaItemIndex))
                }catch (e:Exception){
                    Log.e("kid_e","error in AudioServiceHandler\n${e.message}")
                }
            }
        }
    }

}
sealed interface AudioHandlerEvents{
    data class MediaItemsChanged(val newItems : List<MyAudio>):  AudioHandlerEvents
    data class CurrentItemIndexChanged(val index: Int):  AudioHandlerEvents
    data object ErrorUnableToPlayAudio:  AudioHandlerEvents
}