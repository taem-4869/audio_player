package com.taemallah.playme.mainScreen.data.repository

import com.taemallah.playme.mainScreen.data.localDataProvider.LocalDataProvider
import com.taemallah.playme.mainScreen.domain.model.LocalDataProviderResult
import com.taemallah.playme.mainScreen.domain.model.MyAudio
import com.taemallah.playme.mainScreen.domain.repository.LocalAudioRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalAudioRepoImpl @Inject constructor(
    private val localDataProvider: LocalDataProvider
) : LocalAudioRepo {
    override suspend fun getLocalAudio(): LocalDataProviderResult {
        return localDataProvider.getLocalAudio()
    }
}