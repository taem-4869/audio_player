package com.taemallah.playme.mainScreen.presentation

sealed interface MainEvent {
    data class Start(val audioPosition: Int): MainEvent
    data class Progress(val audioProgress: Float): MainEvent
    data object Forward: MainEvent
    data object Backward: MainEvent
    data object PlayPause: MainEvent
}