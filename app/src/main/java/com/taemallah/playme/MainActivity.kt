package com.taemallah.playme

import android.app.ActivityManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.taemallah.playme.audioPlayer.playBackService.PlayBackService
import com.taemallah.playme.mainScreen.presentation.MainViewModel
import com.taemallah.playme.mainScreen.presentation.components.MainScreen
import com.taemallah.playme.ui.theme.PlayMeTheme
import com.taemallah.playme.utils.getRequiredPermissions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions(getRequiredPermissions(),1)
        enableEdgeToEdge()
        setContent {
            PlayMeTheme {
                Surface {
                    startService()
                    val state by viewModel.state.collectAsState()
                    MainScreen(state = state, onEvent = viewModel::onEvent, player = viewModel.getExoPlayer())
                }
            }
        }
    }

    private fun startService(){
        val manager : ActivityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val isServiceNotRunning = manager.getRunningServices(Int.MAX_VALUE).find { it.service.className.equals((PlayBackService::class.java).name) } == null
        if (isServiceNotRunning){
            val intent = Intent(this,PlayBackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            }else{
                startService(intent)
            }
        }
    }
}