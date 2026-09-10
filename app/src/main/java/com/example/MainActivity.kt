package com.example

import android.os.Bundle
import android.content.pm.ActivityInfo
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.model.GamePhase
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.RoomStake
import com.example.ui.screens.GameScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.theme.KazakhNavyDark
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AziGameViewModel

enum class ScreenState {
    LOBBY,
    GAME
}

internal fun usesLandscapeTable(screen: ScreenState, phase: GamePhase): Boolean =
    screen == ScreenState.GAME && phase != GamePhase.WAITING_FOR_PLAYERS

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
      statusBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
      navigationBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
    )
        setContent {
            MyApplicationTheme {
                val viewModel: AziGameViewModel = viewModel()
                var currentScreen by rememberSaveable { mutableStateOf(ScreenState.LOBBY) }
                val phase by viewModel.gamePhase.collectAsState()
                val landscapeTable = usesLandscapeTable(currentScreen, phase)
                LaunchedEffect(landscapeTable) {
                    requestedOrientation = if (landscapeTable) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    val bars = WindowCompat.getInsetsController(window, window.decorView)
                    bars.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    if (landscapeTable) bars.hide(WindowInsetsCompat.Type.systemBars())
                    else bars.show(WindowInsetsCompat.Type.systemBars())
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(KazakhNavyDark)
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            ScreenState.LOBBY -> {
                                LobbyScreen(
                                    viewModel = viewModel,
                                    onStartGame = {
                                        currentScreen = ScreenState.GAME
                                    }
                                )
                            }
                            ScreenState.GAME -> {
                                GameScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = {
                                        currentScreen = ScreenState.LOBBY
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
