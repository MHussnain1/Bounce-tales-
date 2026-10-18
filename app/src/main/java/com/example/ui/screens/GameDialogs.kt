package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun LevelCompleteDialog(
  levelNumber: Int,
  levelTitle: String,
  score: Int,
  stars: Int,
  timeSeconds: Int,
  ringsCollected: Int,
  totalRings: Int,
  hasNextLevel: Boolean,
  onSaveScoreAndNext: (initials: String) -> Unit,
  onRetry: () -> Unit,
  onMainMenu: () -> Unit
) {
  var char1 by remember { mutableStateOf('A') }
  var char2 by remember { mutableStateOf('C') }
  var char3 by remember { mutableStateOf('E') }

  Dialog(onDismissRequest = {}) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = RetroSurface,
      border = androidx.compose.foundation.BorderStroke(2.dp, RingGold),
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
        .testTag("level_complete_dialog")
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Trophy Icon
        Box(
          modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(RingGold.copy(alpha = 0.2f))
            .border(2.dp, RingGold, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = RingGold,
            modifier = Modifier.size(32.dp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "LEVEL COMPLETE!",
          fontFamily = PixelFontFamily,
          fontSize = 16.sp,
          color = RingGold
        )
        Text(
          text = "Level $levelNumber: $levelTitle",
          fontFamily = BouncyFontFamily,
          fontSize = 14.sp,
          color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Stars
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          for (i in 1..3) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = null,
              tint = if (i <= stars) RingGold else Color(0xFF475569),
              modifier = Modifier.size(28.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Stats Card
        Card(
          colors = CardDefaults.cardColors(containerColor = RetroSurfaceLight),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            StatRow(label = "FINAL SCORE", value = "$score PTS", color = RingGold)
            StatRow(label = "TIME", value = "${timeSeconds}s", color = Color.White)
            StatRow(label = "RINGS COLLECTED", value = "$ringsCollected / $totalRings", color = RetroCyan)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Arcade Initials Selector (Classic AAA style)
        Text(
          text = "ENTER ARCADE INITIALS",
          fontFamily = PixelFontFamily,
          fontSize = 10.sp,
          color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          ArcadeCharSlot(char = char1, onUp = { char1 = cycleUp(char1) }, onDown = { char1 = cycleDown(char1) })
          ArcadeCharSlot(char = char2, onUp = { char2 = cycleUp(char2) }, onDown = { char2 = cycleDown(char2) })
          ArcadeCharSlot(char = char3, onUp = { char3 = cycleUp(char3) }, onDown = { char3 = cycleDown(char3) })
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        Button(
          onClick = {
            val initials = "$char1$char2$char3"
            onSaveScoreAndNext(initials)
          },
          colors = ButtonDefaults.buttonColors(containerColor = BounceRed),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("next_level_button")
        ) {
          Text(
            text = if (hasNextLevel) "SAVE & NEXT LEVEL" else "SAVE SCORE",
            fontFamily = PixelFontFamily,
            fontSize = 11.sp,
            color = Color.White
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("retry_level_button")
          ) {
            Text("RETRY", fontFamily = PixelFontFamily, fontSize = 10.sp, color = Color.White)
          }

          OutlinedButton(
            onClick = onMainMenu,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("dialog_menu_button")
          ) {
            Text("MENU", fontFamily = PixelFontFamily, fontSize = 10.sp, color = Color.White)
          }
        }
      }
    }
  }
}

@Composable
fun GameOverDialog(
  score: Int,
  ringsCollected: Int,
  totalRings: Int,
  onRetry: () -> Unit,
  onMainMenu: () -> Unit
) {
  Dialog(onDismissRequest = {}) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = RetroSurface,
      border = androidx.compose.foundation.BorderStroke(2.dp, BounceRed),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("game_over_dialog")
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "GAME OVER",
          fontFamily = PixelFontFamily,
          fontSize = 18.sp,
          color = BounceRed
        )
        Text(
          text = "The red ball popped!",
          fontFamily = BouncyFontFamily,
          fontSize = 14.sp,
          color = Color.White.copy(alpha = 0.8f),
          modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
        )

        Card(
          colors = CardDefaults.cardColors(containerColor = RetroSurfaceLight),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            StatRow(label = "SCORE", value = "$score PTS", color = RingGold)
            StatRow(label = "RINGS", value = "$ringsCollected / $totalRings", color = RetroCyan)
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onRetry,
          colors = ButtonDefaults.buttonColors(containerColor = BounceRed),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("game_over_retry_button")
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("TRY AGAIN", fontFamily = PixelFontFamily, fontSize = 12.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
          onClick = onMainMenu,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("game_over_menu_button")
        ) {
          Text("MAIN MENU", fontFamily = PixelFontFamily, fontSize = 11.sp, color = Color.White)
        }
      }
    }
  }
}

@Composable
fun PauseDialog(
  onResume: () -> Unit,
  onRestart: () -> Unit,
  onOpenSettings: () -> Unit,
  onMainMenu: () -> Unit
) {
  Dialog(onDismissRequest = onResume) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = RetroSurface,
      border = androidx.compose.foundation.BorderStroke(2.dp, RetroCyan),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("pause_dialog")
    ) {
      Column(
        modifier = Modifier.padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "GAME PAUSED",
          fontFamily = PixelFontFamily,
          fontSize = 18.sp,
          color = RetroCyan
        )

        Spacer(modifier = Modifier.height(6.dp))

        Button(
          onClick = onResume,
          colors = ButtonDefaults.buttonColors(containerColor = BounceRed),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("resume_game_button")
        ) {
          Text("RESUME", fontFamily = PixelFontFamily, fontSize = 12.sp, color = Color.White)
        }

        OutlinedButton(
          onClick = onRestart,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("restart_level_button")
        ) {
          Text("RESTART LEVEL", fontFamily = PixelFontFamily, fontSize = 11.sp, color = Color.White)
        }

        OutlinedButton(
          onClick = onOpenSettings,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("pause_settings_button")
        ) {
          Text("SETTINGS", fontFamily = PixelFontFamily, fontSize = 11.sp, color = Color.White)
        }

        OutlinedButton(
          onClick = onMainMenu,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("pause_menu_button")
        ) {
          Text("EXIT TO MENU", fontFamily = PixelFontFamily, fontSize = 11.sp, color = Color.White)
        }
      }
    }
  }
}

@Composable
private fun StatRow(label: String, value: String, color: Color) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontFamily = PixelFontFamily)
    Text(text = value, color = color, fontSize = 13.sp, fontFamily = PixelFontFamily)
  }
}

@Composable
private fun ArcadeCharSlot(char: Char, onUp: () -> Unit, onDown: () -> Unit) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    IconButton(onClick = onUp, modifier = Modifier.size(28.dp)) {
      Icon(Icons.Default.ArrowUpward, contentDescription = "Up", tint = RingGold, modifier = Modifier.size(16.dp))
    }
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = Color(0xFF0F172A),
      border = androidx.compose.foundation.BorderStroke(1.5.dp, RingGold),
      modifier = Modifier.size(44.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(text = "$char", fontFamily = PixelFontFamily, fontSize = 18.sp, color = RingGold)
      }
    }
    IconButton(onClick = onDown, modifier = Modifier.size(28.dp)) {
      Icon(Icons.Default.ArrowDownward, contentDescription = "Down", tint = RingGold, modifier = Modifier.size(16.dp))
    }
  }
}

private fun cycleUp(c: Char): Char {
  return if (c == 'Z') 'A' else c + 1
}

private fun cycleDown(c: Char): Char {
  return if (c == 'A') 'Z' else c - 1
}
