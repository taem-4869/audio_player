package com.taemallah.playme.mainScreen.domain.repository

import com.taemallah.playme.mainScreen.domain.model.LocalDataProviderResult
import kotlinx.coroutines.flow.Flow

interface LocalAudioRepo {
    suspend fun getLocalAudio() : LocalDataProviderResult
}