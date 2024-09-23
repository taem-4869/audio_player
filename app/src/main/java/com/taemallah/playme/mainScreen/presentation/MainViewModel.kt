package com.taemallah.playme.mainScreen.presentation

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import com.taemallah.playme.R
import com.taemallah.playme.audioPlayer.audioController.AudioHandlerEvents
import com.taemallah.playme.audioPlayer.audioController.AudioServiceHandler
import com.taemallah.playme.mainScreen.domain.model.LocalDataProviderResult
import com.taemallah.playme.mainScreen.domain.repository.LocalAudioRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: LocalAudioRepo,
    private val audioServiceHandler: AudioServiceHandler,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _loadingResult = MutableStateFlow<LocalDataProviderResult>(LocalDataProviderResult.Idle)
    private val _state = MutableStateFlow(MainState())
    val state = combine(_loadingResult,_state){loadingResult, state->
        state.copy(
            loadingResult = loadingResult,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainState())

    init {
        loadLocalAudioData()

        viewModelScope.launch {
            _loadingResult.collect{result->
                when(result){
                    is LocalDataProviderResult.Failure -> {
                        audioServiceHandler.clearMediaItems()
                    }
                    is LocalDataProviderResult.Success -> {
                        audioServiceHandler.setMediaItemsFromAudios(result.audioList)
                    }
                    else -> Unit
                }
            }
        }

        viewModelScope.launch {
            audioServiceHandler.audioHandlerEventsFlow.collect{event->
                when(event){
                    is AudioHandlerEvents.MediaItemsChanged -> {
                        _state.update {
                            it.copy(
                                audioList = event.newItems
                            )
                        }
                    }
                    is AudioHandlerEvents.CurrentItemIndexChanged -> {
                        _state.update {
                            it.copy(
                                currentAudioState = it.currentAudioState.copy(position = event.index)
                            )
                        }
                    }
                    is AudioHandlerEvents.ErrorUnableToPlayAudio -> {
                        Toast.makeText(
                            context,
                            context.getString(R.string.couldn_t_play_this_audio),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    null -> Unit
                }
            }
        }

    }

    fun onEvent(event: MainEvent){
        when(event){
            is MainEvent.Start -> {
                audioServiceHandler.startAudio(index = event.audioPosition)
            }
            is MainEvent.Progress -> {
                audioServiceHandler.seekTo(state.value.currentAudioState.getProgressByPercentage(event.audioProgress))
            }
            MainEvent.Backward -> {
                audioServiceHandler.seekBack()
            }
            MainEvent.Forward -> {
                audioServiceHandler.seekForward()
            }

            MainEvent.PlayPause -> audioServiceHandler.playOrPause()
        }
    }

    private fun loadLocalAudioData(){
        _loadingResult.update {
            LocalDataProviderResult.Loading
        }
        viewModelScope.launch {
            _loadingResult.update {
                repo.getLocalAudio()
            }
        }
    }

    fun getExoPlayer(): ExoPlayer {
        return audioServiceHandler.exoPlayer
    }

}