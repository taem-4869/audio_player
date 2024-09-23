package com.taemallah.playme.mainScreen.domain.model

import android.net.Uri
import androidx.core.net.toUri

data class MyAudio (
    val uri: Uri,
    val id: Long,
    val displayName: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val data: String,
){
    companion object{
        val EmptyAudio = MyAudio(
            uri = "".toUri(),
            displayName = "",
            id = 0L,
            artist = "",
            data = "",
            duration = 0L,
            title = ""
        )
    }
}