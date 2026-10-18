package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.RetroAudioSynthesizer
import com.example.ui.theme.*

@Composable
fun MainMenuScreen(
  audio: RetroAudioSynthesizer,
  isNokiaMode: Boolean,
  onNokiaToggle: (Boolean) -> Unit,
  onStartGame: () -> Unit,
  onLevelSelect: () -> Unit,
  onLeaderboard: () -> Unit,
  onOpenSettings: () -> Unit
) {
  // Start upbeat background music when entering menu
  LaunchedEffect(Unit) {
    audio.startMusic()
  }

  // Animated Bouncing Ball in Menu
  val infiniteTransition = rememberInfiniteTransition(label = "menu_bounce")
  val bounceOffsetY by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = -35f,
    animationSpec = infiniteRepeatable(
      animation = tween(450, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "ball_bounce_y"
  )
  val squashX by infiniteTransition.animateFloat(
    initialValue = 1.15f,
    targetValue = 0.9f,
    animationSpec = infiniteRepeatable(
      animation = tween(450, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "ball_squash_x"
  )

  Scaffold(
    containerColor = RetroDarkBg
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
      // Background Grid & Parallax Starfield Canvas
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Subtle retro grid
        for (x in 0..w.toInt() step 50) {
          drawLine(
            color = Color(0x153A506B),
            start = Offset(x.toFloat(), 0f),
            end = Offset(x.toFloat(), h),
            strokeWidth = 1f
          )
        }
        for (y in 0..h.toInt() step 50) {
          drawLine(
            color = Color(0x153A506B),
            start = Offset(0f, y.toFloat()),
            end = Offset(w, y.toFloat()),
            strokeWidth = 1f
          )
        }
      }

      // Top Header with Nokia Mode Quick Switch
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = RetroSurface,
          border = androidx.compose.foundation.BorderStroke(1.dp, RetroSurfaceLight)
        ) {
          Text(
            text = "CLASSIC NOKIA EDITION",
            fontFamily = PixelFontFamily,
            fontSize = 9.sp,
            color = RingGold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }

        IconButton(
          onClick = {
            audio.playClick()
            onOpenSettings()
          },
          modifier = Modifier.testTag("menu_settings_button")
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = Color.White
          )
        }
      }

      // Main Menu Center Content
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.Center)
          .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // Animated Bouncy Red Ball Mascot
        Box(
          modifier = Modifier
            .size(110.dp)
            .padding(bottom = 16.dp),
          contentAlignment = Alignment.Center
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f + bounceOffsetY)
            val radius = 38f

            // Shadow on ground
            drawOval(
              color = Color(0x44000000),
              topLeft = Offset(center.x - radius * squashX, size.height - 14f),
              size = androidx.compose.ui.geometry.Size(radius * 2f * squashX, 10f)
            )

            // Golden Ring behind ball
            drawCircle(
              color = RingGold,
              radius = radius * 1.35f,
              center = center,
              style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
            )

            // Red Glossy Rubber Ball
            drawCircle(
              brush = Brush.radialGradient(
                colors = listOf(BounceRedLight, BounceRed, BounceRedDark),
                center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
                radius = radius * 1.2f
              ),
              radius = radius,
              center = center
            )

            // Glossy highlight shine
            drawCircle(
              color = Color.White.copy(alpha = 0.75f),
              radius = radius * 0.28f,
              center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f)
            )
          }
        }

        // Title
        Text(
          text = "BOUNCE TALES",
          fontFamily = PixelFontFamily,
          fontSize = 24.sp,
          color = BounceRedLight,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "RETRO RED BALL PLATFORMER",
          fontFamily = PixelFontFamily,
          fontSize = 9.sp,
          color = RingGold,
          modifier = Modifier.padding(top = 6.dp, bottom = 28.dp)
        )

        // Menu Action Buttons
        MenuButton(
          title = "PLAY GAME",
          icon = Icons.Default.PlayArrow,
          color = BounceRed,
          testTag = "play_game_button",
          onClick = {
            audio.playClick()
            onStartGame()
          }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MenuButton(
          title = "SELECT LEVEL",
          icon = Icons.Default.List,
          color = RetroSurface,
          testTag = "select_level_button",
          onClick = {
            audio.playClick()
            onLevelSelect()
          }
        )

        Spacer(modifier = Modifier.height(12.dp))

        MenuButton(
          title = "HALL OF FAME",
          icon = Icons.Default.EmojiEvents,
          color = RetroSurface,
          testTag = "hall_of_fame_button",
          onClick = {
            audio.playClick()
            onLeaderboard()
          }
        )
      }

      // Bottom Footer
      Text(
        text = "Inspired by classic Nokia Bounce • Faithful physics",
        color = Color.White.copy(alpha = 0.45f),
        fontSize = 11.sp,
        fontFamily = BouncyFontFamily,
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 16.dp)
      )
    }
  }
}

@Composable
private fun MenuButton(
  title: String,
  icon: ImageVector,
  color: Color,
  testTag: String,
  onClick: () -> Unit
) {
  val isPrimary = color == BounceRed
  val border = if (isPrimary) {
    androidx.compose.foundation.BorderStroke(2.dp, RingGold.copy(alpha = 0.8f))
  } else {
    androidx.compose.foundation.BorderStroke(1.dp, RetroSurfaceLight)
  }

  Button(
    onClick = onClick,
    colors = ButtonDefaults.buttonColors(containerColor = color),
    shape = RoundedCornerShape(16.dp),
    border = border,
    modifier = Modifier
      .fillMaxWidth()
      .height(54.dp)
      .testTag(testTag)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isPrimary) Color.White else RingGold,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = title,
        fontFamily = PixelFontFamily,
        fontSize = 13.sp,
        color = Color.White
      )
    }
  }
}
