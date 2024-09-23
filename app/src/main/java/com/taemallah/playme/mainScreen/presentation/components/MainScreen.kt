package com.taemallah.playme.mainScreen.presentation.components

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.util.RepeatModeUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.taemallah.playme.R
import com.taemallah.playme.mainScreen.domain.model.LocalDataProviderResult
import com.taemallah.playme.mainScreen.domain.model.MyAudio
import com.taemallah.playme.mainScreen.presentation.MainEvent
import com.taemallah.playme.mainScreen.presentation.MainState
import kotlin.math.floor

@Composable
fun MainScreen(state: MainState, onEvent:(MainEvent)->Unit, player: ExoPlayer? = null) {

    AnimatedContent(
        targetState = state.loadingResult,
        modifier = Modifier
            .fillMaxSize(),
        label = "",
    ) {
        when(it){
            is LocalDataProviderResult.Failure -> FailureScreen()
            LocalDataProviderResult.Idle -> {}
            LocalDataProviderResult.Loading -> LoadingScreen()
            is LocalDataProviderResult.Success -> SuccessResultScreen(state, onEvent, player)
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun SuccessResultScreen(state: MainState, onEvent: (MainEvent) -> Unit, exoPlayer : ExoPlayer?) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) {padding->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
            ){
            AndroidView(
                factory = { context ->
                    PlayerView(context).also {
                        it.player = exoPlayer
                        it.setShowShuffleButton(true)
                        it.setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE or RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL or RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE)
                    }
                },
                modifier = Modifier
                    .fillMaxHeight(.4f)
                    .clip(CardDefaults.outlinedShape)
            )
            val lazyListState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(12.dp),
                state = lazyListState
            ) {
                itemsIndexed(state.audioList){index, audio->
                    AudioItem(audio, index, state.currentAudioState.position==index, onEvent)
                }
            }
            LaunchedEffect(key1 = state.currentAudioState) {
                if (state.audioList.isNotEmpty()){
                    lazyListState.scrollToItem(state.currentAudioState.position)
                }
            }
        }
    }
}

@Composable
fun AudioItem(audio: MyAudio, index: Int, isSelectedItem: Boolean, onEvent: (MainEvent) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onEvent(MainEvent.Start(index))
            },
        colors =
        if(isSelectedItem) CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary
        )
        else CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = audio.displayName,
                style = MaterialTheme.typography.titleSmall.copy(textDirection = TextDirection.Content),
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = audio.title.ifBlank { stringResource(R.string.no_title) },
                style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Content),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = audio.artist.ifBlank { stringResource(R.string.unknown_artist) },
                style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Content),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = timeStampToDuration(audio.duration),
                style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Content),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun timeStampToDuration(duration: Long): String {
    val totalSeconds = floor(duration/1E3).toInt()
    val minutes = totalSeconds/60
    val remainingSeconds = totalSeconds - (minutes*60)
    return if (duration<0) "--:--" else "$minutes:$remainingSeconds"
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        CircularProgressIndicator()
        Text(
            text = "loading",
        )
    }
}

@Composable
fun FailureScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Text(
            text = stringResource(R.string.failed_to_load_audio_data),
            color = MaterialTheme.colorScheme.error,
        )
    }
}