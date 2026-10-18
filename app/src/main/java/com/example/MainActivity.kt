package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.audio.RetroAudioSynthesizer
import com.example.data.AppDatabase
import com.example.ui.screens.*
import com.example.ui.theme.BounceTalesTheme
import com.example.ui.theme.RetroDarkBg

sealed interface AppScreen {
  data object MainMenu : AppScreen
  data object LevelSelect : AppScreen
  data class Game(val levelNumber: Int) : AppScreen
  data object Leaderboard : AppScreen
}

class MainActivity : ComponentActivity() {
  private val audioSynthesizer = RetroAudioSynthesizer()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      BounceTalesTheme(darkTheme = true) {
        val context = LocalContext.current
        val database = remember { AppDatabase.getDatabase(context) }
        val scoreDao = remember { database.scoreDao() }

        var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.MainMenu) }
        var isNokiaMode by remember { mutableStateOf(false) }
        var showSettingsDialog by remember { mutableStateOf(false) }

        Surface(
          modifier = Modifier.fillMaxSize(),
          color = RetroDarkBg
        ) {
          when (val screen = currentScreen) {
            is AppScreen.MainMenu -> {
              MainMenuScreen(
                audio = audioSynthesizer,
                isNokiaMode = isNokiaMode,
                onNokiaToggle = { isNokiaMode = it },
                onStartGame = {
                  currentScreen = AppScreen.Game(levelNumber = 1)
                },
                onLevelSelect = {
                  currentScreen = AppScreen.LevelSelect
                },
                onLeaderboard = {
                  currentScreen = AppScreen.Leaderboard
                },
                onOpenSettings = {
                  showSettingsDialog = true
                }
              )
            }

            is AppScreen.LevelSelect -> {
              LevelSelectScreen(
                scoreDao = scoreDao,
                audio = audioSynthesizer,
                onBack = {
                  currentScreen = AppScreen.MainMenu
                },
                onSelectLevel = { levelNum ->
                  currentScreen = AppScreen.Game(levelNumber = levelNum)
                }
              )
            }

            is AppScreen.Game -> {
              GameScreen(
                levelNumber = screen.levelNumber,
                scoreDao = scoreDao,
                audio = audioSynthesizer,
                isNokiaMode = isNokiaMode,
                onNokiaToggle = { isNokiaMode = it },
                onOpenSettings = {
                  showSettingsDialog = true
                },
                onLevelCompleteNav = { nextLvl ->
                  currentScreen = AppScreen.Game(levelNumber = nextLvl)
                },
                onExitToMenu = {
                  currentScreen = AppScreen.MainMenu
                  audioSynthesizer.startMusic()
                }
              )
            }

            is AppScreen.Leaderboard -> {
              LeaderboardScreen(
                scoreDao = scoreDao,
                audio = audioSynthesizer,
                onBack = {
                  currentScreen = AppScreen.MainMenu
                },
                onPlayLevel = { lvl ->
                  currentScreen = AppScreen.Game(levelNumber = lvl)
                }
              )
            }
          }

          if (showSettingsDialog) {
            SettingsDialog(
              audio = audioSynthesizer,
              isNokiaMode = isNokiaMode,
              onNokiaModeToggle = { isNokiaMode = it },
              onDismiss = { showSettingsDialog = false }
            )
          }
        }
      }
    }
  }

  override fun onPause() {
    super.onPause()
    audioSynthesizer.stopMusic()
  }

  override fun onResume() {
    super.onResume()
    audioSynthesizer.startMusic()
  }

  override fun onDestroy() {
    super.onDestroy()
    audioSynthesizer.stopMusic()
  }
}
