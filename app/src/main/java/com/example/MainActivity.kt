package com.example

import android.os.Bundle
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
import androidx.compose.runtime.remember
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: AziGameViewModel = viewModel()
                var currentScreen by remember { mutableStateOf(ScreenState.LOBBY) }

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

