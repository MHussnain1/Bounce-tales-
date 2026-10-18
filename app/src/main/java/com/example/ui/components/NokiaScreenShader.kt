package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NokiaLcdGreen

/**
 * Authentic Nokia 3310 monochrome LCD overlay.
 * Applies green LCD matrix tinting and fine scanline pixelation over the canvas.
 */
@Composable
fun NokiaScreenFilter(
  enabled: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Box(modifier = modifier.fillMaxSize()) {
    content()

    if (enabled) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. LCD Green Color Matrix Blend
        drawRect(
          color = NokiaLcdGreen.copy(alpha = 0.45f),
          size = size,
          blendMode = BlendMode.Color
        )

        // 2. LCD Dot Grid Scanlines
        val scanlineSpacing = 4f
        var y = 0f
        while (y < height) {
          drawLine(
            color = Color(0x330F380F),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.2f
          )
          y += scanlineSpacing
        }

        // 3. Subtle LCD Vignette Border
        drawRect(
          color = Color(0x220F380F),
          size = size
        )
      }
    }
  }
}
