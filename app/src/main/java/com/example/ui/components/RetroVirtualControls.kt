package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BallForm
import com.example.ui.theme.*

@Composable
fun RetroVirtualControls(
  ballForm: BallForm,
  onMoveChange: (Float) -> Unit,
  onJumpChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  var isLeftPressed by remember { mutableStateOf(false) }
  var isRightPressed by remember { mutableStateOf(false) }
  var isJumpPressed by remember { mutableStateOf(false) }

  // Sync horizontal movement
  LaunchedEffect(isLeftPressed, isRightPressed) {
    val dir = when {
      isLeftPressed && !isRightPressed -> -1f
      isRightPressed && !isLeftPressed -> 1f
      else -> 0f
    }
    onMoveChange(dir)
  }

  // Sync jump state
  LaunchedEffect(isJumpPressed) {
    onJumpChange(isJumpPressed)
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.navigationBars)
      .padding(horizontal = 20.dp, vertical = 12.dp)
  ) {
    // Left & Right D-pad controls
    Row(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .testTag("dpad_container"),
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      TactileArcadeButton(
        icon = Icons.AutoMirrored.Filled.ArrowBack,
        label = "LEFT",
        isPressed = isLeftPressed,
        testTag = "control_left",
        buttonColor = Color(0xFF334155),
        activeColor = Color(0xFF64748B),
        onPressChange = { isLeftPressed = it }
      )

      TactileArcadeButton(
        icon = Icons.AutoMirrored.Filled.ArrowForward,
        label = "RIGHT",
        isPressed = isRightPressed,
        testTag = "control_right",
        buttonColor = Color(0xFF334155),
        activeColor = Color(0xFF64748B),
        onPressChange = { isRightPressed = it }
      )
    }

    // Right Action: BOUNCE / JUMP & Form Indicator
    Row(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .testTag("action_container"),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Ball Form Badge
      val formColor = when (ballForm) {
        BallForm.NORMAL -> BounceRed
        BallForm.FLOAT -> Color(0xFFFFB300)
        BallForm.STONE -> Color(0xFF78909C)
      }
      val formName = when (ballForm) {
        BallForm.NORMAL -> "RUBBER"
        BallForm.FLOAT -> "FLOAT"
        BallForm.STONE -> "STONE"
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = formColor.copy(alpha = 0.25f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, formColor),
        modifier = Modifier.padding(bottom = 6.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.ElectricBolt,
            contentDescription = null,
            tint = formColor,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = formName,
            color = formColor,
            fontSize = 10.sp,
            fontFamily = PixelFontFamily
          )
        }
      }

      // Large Jump / Bounce Button
      TactileArcadeButton(
        icon = Icons.Default.ArrowUpward,
        label = "JUMP",
        isPressed = isJumpPressed,
        testTag = "control_jump",
        size = 72,
        buttonColor = BounceRed,
        activeColor = BounceRedDark,
        onPressChange = { isJumpPressed = it }
      )
    }
  }
}

@Composable
private fun TactileArcadeButton(
  icon: ImageVector,
  label: String,
  isPressed: Boolean,
  testTag: String,
  buttonColor: Color,
  activeColor: Color,
  size: Int = 64,
  onPressChange: (Boolean) -> Unit
) {
  val currentBg = if (isPressed) activeColor else buttonColor
  val elevation = if (isPressed) 2.dp else 6.dp

  Box(
    modifier = Modifier
      .size(size.dp)
      .testTag(testTag)
      .shadow(elevation, CircleShape)
      .clip(CircleShape)
      .background(
        Brush.verticalGradient(
          colors = listOf(
            currentBg,
            currentBg.copy(alpha = 0.8f)
          )
        )
      )
      .pointerInput(Unit) {
        detectTapGestures(
          onPress = {
            onPressChange(true)
            tryAwaitRelease()
            onPressChange(false)
          }
        )
      },
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = Color.White,
        modifier = Modifier.size((size * 0.45).dp)
      )
      Text(
        text = label,
        color = Color.White.copy(alpha = 0.85f),
        fontSize = 8.sp,
        fontFamily = PixelFontFamily
      )
    }
  }
}
