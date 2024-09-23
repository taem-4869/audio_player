package com.taemallah.playme.mainScreen.presentation

import androidx.annotation.StringRes
import com.taemallah.playme.mainScreen.domain.model.LocalDataProviderResult
import com.taemallah.playme.mainScreen.domain.model.MyAudio

data class MainState (
    val loadingResult: LocalDataProviderResult = LocalDataProviderResult.Idle,
    val audioList: List<MyAudio> = emptyList(),
    val currentAudioState: CurrentAudioState = CurrentAudioState(),
){
    data class CurrentAudioState(
        val position: Int = 0,
        val progress: Long = 0,
        val duration: Long = 0,
        val isPlaying: Boolean = false,
    ){
        fun getProgressInPercent(): Float {
            if (duration == 0L) return 0F
            return ((progress/duration)*100).toFloat()
        }

        fun getProgressByPercentage(percentage: Float): Long {
            if (duration == 0L) return 0L
            return (duration*percentage).toLong()
        }
    }
}