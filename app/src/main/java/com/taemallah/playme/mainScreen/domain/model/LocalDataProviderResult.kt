package com.taemallah.playme.mainScreen.domain.model

sealed interface LocalDataProviderResult {
    data class Success(val audioList: List<MyAudio>): LocalDataProviderResult
    data class Failure(val exception: Exception): LocalDataProviderResult
    data object Loading: LocalDataProviderResult
    data object Idle: LocalDataProviderResult
}