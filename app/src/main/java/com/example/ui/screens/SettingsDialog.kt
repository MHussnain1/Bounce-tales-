package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.RetroAudioSynthesizer
import com.example.ui.theme.*

@Composable
fun SettingsDialog(
  audio: RetroAudioSynthesizer,
  isNokiaMode: Boolean,
  onNokiaModeToggle: (Boolean) -> Unit,
  onDismiss: () -> Unit
) {
  var musicOn by remember { mutableStateOf(audio.isMusicEnabled) }
  var sfxOn by remember { mutableStateOf(audio.isSfxEnabled) }
  var nokiaOn by remember { mutableStateOf(isNokiaMode) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = RetroSurface,
      border = androidx.compose.foundation.BorderStroke(2.dp, RetroCyan),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("settings_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Title Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "SETTINGS",
            fontFamily = PixelFontFamily,
            fontSize = 18.sp,
            color = RingGold
          )
          IconButton(
            onClick = {
              audio.playClick()
              onDismiss()
            },
            modifier = Modifier.testTag("close_settings_button")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sound Settings Card
        Card(
          colors = CardDefaults.cardColors(containerColor = RetroSurfaceLight),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "AUDIO OPTIONS",
              fontFamily = PixelFontFamily,
              fontSize = 12.sp,
              color = RetroCyan
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Music Toggle
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White)
                Text("Retro Music", color = Color.White, fontFamily = BouncyFontFamily, fontSize = 16.sp)
              }
              Switch(
                checked = musicOn,
                onCheckedChange = {
                  musicOn = it
                  audio.isMusicEnabled = it
                  audio.playClick()
                },
                colors = SwitchDefaults.colors(checkedThumbColor = BounceRed)
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SFX Toggle
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White)
                Text("Sound Effects", color = Color.White, fontFamily = BouncyFontFamily, fontSize = 16.sp)
              }
              Switch(
                checked = sfxOn,
                onCheckedChange = {
                  sfxOn = it
                  audio.isSfxEnabled = it
                  audio.playClick()
                },
                colors = SwitchDefaults.colors(checkedThumbColor = BounceRed)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Visual Nostalgia Card
        Card(
          colors = CardDefaults.cardColors(containerColor = RetroSurfaceLight),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "VISUAL NOSTALGIA",
              fontFamily = PixelFontFamily,
              fontSize = 12.sp,
              color = RingGold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = NokiaLcdGreen)
                Column {
                  Text("Nokia 3310 Mode", color = Color.White, fontFamily = BouncyFontFamily, fontSize = 16.sp)
                  Text("Green LCD & dot matrix", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
              }
              Switch(
                checked = nokiaOn,
                onCheckedChange = {
                  nokiaOn = it
                  onNokiaModeToggle(it)
                  audio.playClick()
                },
                colors = SwitchDefaults.colors(checkedThumbColor = NokiaLcdGreen)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // How to Play Guide Card
        Card(
          colors = CardDefaults.cardColors(containerColor = RetroSurfaceLight),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "HOW TO PLAY",
              fontFamily = PixelFontFamily,
              fontSize = 12.sp,
              color = RingGoldLight
            )

            Spacer(modifier = Modifier.height(8.dp))

            GuideItem(title = "🔴 Red Rubber Ball", desc = "Classic agile ball. Bounce over gaps and collect golden rings.")
            GuideItem(title = "🟡 Float Balloon Orb", desc = "Lightweight! Floats on water and rides upward air drafts.")
            GuideItem(title = "⚪ Stone Giant Orb", desc = "Heavy! Smashes cracked stone blocks and sinks in water.")
            GuideItem(title = "💍 Golden Rings", desc = "Pass through every ring to unlock the exit portal.")
            GuideItem(title = "⚠️ Spikes & Traps", desc = "Avoid touching sharp hazards. Crushing triggers checkpoint respawn.")
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = {
            audio.playClick()
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = BounceRed),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("done_settings_button")
        ) {
          Text("DONE", fontFamily = PixelFontFamily, fontSize = 12.sp, color = Color.White)
        }
      }
    }
  }
}

@Composable
private fun GuideItem(title: String, desc: String) {
  Column(modifier = Modifier.padding(vertical = 4.dp)) {
    Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = BouncyFontFamily)
    Text(text = desc, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
  }
}
