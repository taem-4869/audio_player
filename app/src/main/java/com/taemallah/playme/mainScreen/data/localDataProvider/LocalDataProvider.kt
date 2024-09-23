package com.taemallah.playme.mainScreen.data.localDataProvider

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.taemallah.playme.mainScreen.domain.model.LocalDataProviderResult
import com.taemallah.playme.mainScreen.domain.model.MyAudio
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocalDataProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val projection = arrayOf(
        MediaStore.Audio.AudioColumns._ID,
        MediaStore.Audio.AudioColumns.DISPLAY_NAME,
        MediaStore.Audio.AudioColumns.TITLE,
        MediaStore.Audio.AudioColumns.ARTIST,
        MediaStore.Audio.AudioColumns.DURATION,
        MediaStore.Audio.AudioColumns.DATA,
    )
    private val selectionClause = "${MediaStore.Audio.AudioColumns.IS_MUSIC} = ? OR" +
            " ${MediaStore.Audio.AudioColumns.IS_PODCAST} = ? "
    private val selectionArgs = arrayOf("1","1")
    private val sortOrder = "${MediaStore.Audio.AudioColumns.DISPLAY_NAME} ASC"

    fun getLocalAudio(): LocalDataProviderResult {
        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selectionClause,
                selectionArgs,
                sortOrder
            )?.use { cursor->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)
                val displayColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA)

                val audioList = emptyList<MyAudio>().toMutableList()
                while (cursor.moveToNext()){
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayColumn)
                    val title = cursor.getString(titleColumn)
                    val artist = cursor.getString(artistColumn)
                    val duration = cursor.getLong(durationColumn)
                    val data = cursor.getString(dataColumn)
                    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id)
                    audioList.add( MyAudio( uri,id,displayName,title,artist,duration, data ) )
                }

                return LocalDataProviderResult.Success(audioList).also {
                    Log.i("kid_e","audio data loaded successfully : ${audioList.count()} items found")
                }
            }?: return LocalDataProviderResult.Failure( Exception("Null query result") ).also {
                Log.e("kid_e","failed to load local : Null query result")
            }
        }catch (e : Exception){
            return LocalDataProviderResult.Failure(e).also {
                Log.e("kid_e","failed to load local : \n${e.printStackTrace()}")
            }
        }

    }
}