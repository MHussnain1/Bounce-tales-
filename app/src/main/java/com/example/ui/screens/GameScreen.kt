package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.RetroAudioSynthesizer
import com.example.data.*
import com.example.game.GameEngine
import com.example.game.Particle
import com.example.ui.components.NokiaScreenFilter
import com.example.ui.components.RetroVirtualControls
import com.example.ui.theme.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.*

@Composable
fun GameScreen(
  levelNumber: Int,
  scoreDao: ScoreDao,
  audio: RetroAudioSynthesizer,
  isNokiaMode: Boolean,
  onNokiaToggle: (Boolean) -> Unit,
  onOpenSettings: () -> Unit,
  onLevelCompleteNav: (nextLevel: Int) -> Unit,
  onExitToMenu: () -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  var currentLevelNumber by remember(levelNumber) { mutableIntStateOf(levelNumber) }

  // Game Engine instance
  var gameEngine by remember(currentLevelNumber) {
    mutableStateOf(
      GameEngine(
        level = LevelsRepository.getLevel(currentLevelNumber),
        audio = audio,
        onLevelComplete = { _, _, _ -> },
        onGameOver = { }
      )
    )
  }

  // Dialog states
  var showPauseDialog by remember { mutableStateOf(false) }
  var showCompleteDialog by remember { mutableStateOf(false) }
  var showGameOverDialog by remember { mutableStateOf(false) }
  var completedScore by remember { mutableIntStateOf(0) }
  var completedStars by remember { mutableIntStateOf(0) }
  var completedTimeSec by remember { mutableIntStateOf(0) }

  // Wire callbacks
  LaunchedEffect(currentLevelNumber) {
    val levelDef = LevelsRepository.getLevel(currentLevelNumber)
    gameEngine = GameEngine(
      level = levelDef,
      audio = audio,
      onLevelComplete = { score, stars, timeSec ->
        completedScore = score
        completedStars = stars
        completedTimeSec = timeSec
        showCompleteDialog = true
      },
      onGameOver = {
        showGameOverDialog = true
      }
    )
  }

  fun restartCurrentLevel() {
    val levelDef = LevelsRepository.getLevel(currentLevelNumber)
    gameEngine = GameEngine(
      level = levelDef,
      audio = audio,
      onLevelComplete = { score, stars, timeSec ->
        completedScore = score
        completedStars = stars
        completedTimeSec = timeSec
        showCompleteDialog = true
      },
      onGameOver = {
        showGameOverDialog = true
      }
    )
  }

  BackHandler {
    showPauseDialog = true
    gameEngine.isPaused = true
  }

  // Game loop 60 FPS
  var lastFrameTime by remember { mutableLongStateOf(0L) }
  var frameTick by remember { mutableLongStateOf(0L) }

  LaunchedEffect(gameEngine) {
    while (isActive) {
      withInfiniteAnimationFrameMillis { frameTimeMs ->
        if (lastFrameTime > 0L) {
          val dt = ((frameTimeMs - lastFrameTime) / 1000f).coerceIn(0.005f, 0.035f)
          gameEngine.update(dt)
          frameTick++
        }
        lastFrameTime = frameTimeMs
      }
    }
  }

  // Smooth camera tracking
  var cameraX by remember { mutableFloatStateOf(gameEngine.ballX) }
  var cameraY by remember { mutableFloatStateOf(gameEngine.ballY) }

  LaunchedEffect(frameTick) {
    // Lerp camera towards ball position with look-ahead
    val targetCamX = gameEngine.ballX + gameEngine.ballVx * 10f
    val targetCamY = gameEngine.ballY + gameEngine.ballVy * 4f
    cameraX += (targetCamX - cameraX) * 0.12f
    cameraY += (targetCamY - cameraY) * 0.12f
  }

  NokiaScreenFilter(enabled = isNokiaMode) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(RetroDarkBg)
    ) {
      // Game Canvas
      Canvas(modifier = Modifier.fillMaxSize()) {
        val viewWidth = size.width
        val viewHeight = size.height

        val offsetX = (viewWidth / 2f) - cameraX
        val offsetY = (viewHeight / 2f + 40f) - cameraY

        // Draw Parallax Scenery Background
        drawParallaxBackground(
          levelNum = currentLevelNumber,
          cameraX = cameraX,
          cameraY = cameraY,
          viewWidth = viewWidth,
          viewHeight = viewHeight
        )

        // Draw Solid Platforms
        for (plat in gameEngine.level.platforms) {
          drawPlatform(plat, offsetX, offsetY)
        }

        // Draw Moving Platforms
        for (mp in gameEngine.level.movingPlatforms) {
          drawMovingPlatform(mp, offsetX, offsetY)
        }

        // Draw Water & Lava Zones
        for (w in gameEngine.level.waterZones) {
          drawWaterZone(w, offsetX, offsetY, frameTick)
        }

        // Draw Air Vent Updrafts
        for (vent in gameEngine.level.airVents) {
          drawAirVent(vent, offsetX, offsetY, frameTick)
        }

        // Draw Spring Pads
        for (spring in gameEngine.level.springs) {
          drawSpring(spring, offsetX, offsetY)
        }

        // Draw Spikes
        for (spk in gameEngine.level.spikes) {
          drawSpikeHazard(spk, offsetX, offsetY)
        }

        // Draw Breakable Blocks
        for (block in gameEngine.level.breakableBlocks) {
          if (!block.isDestroyed) {
            drawBreakableBlock(block, offsetX, offsetY)
          }
        }

        // Draw Pushable Boxes
        for (box in gameEngine.level.pushableBoxes) {
          drawPushableBox(box, offsetX, offsetY)
        }

        // Draw Pressure Switches
        for (sw in gameEngine.level.switches) {
          drawPressureSwitch(sw, offsetX, offsetY)
        }

        // Draw Doors / Portcullis Gates
        for (door in gameEngine.level.doors) {
          drawDoor(door, offsetX, offsetY)
        }

        // Draw Checkpoints
        for (cp in gameEngine.level.checkpoints) {
          drawCheckpoint(cp, offsetX, offsetY, frameTick)
        }

        // Draw Golden Hoop Rings
        for (ring in gameEngine.level.rings) {
          if (!ring.isCollected) {
            drawGoldenRing(ring, offsetX, offsetY, frameTick)
          }
        }

        // Draw Power-Up Orbs
        for (pu in gameEngine.level.powerUps) {
          if (!pu.isCollected) {
            drawPowerUpOrb(pu, offsetX, offsetY, frameTick)
          }
        }

        // Draw Exit Portal Ring
        drawExitGate(gameEngine.level.exitGate, offsetX, offsetY, frameTick)

        // Draw Particles
        for (p in gameEngine.particles) {
          drawCircle(
            color = p.color.copy(alpha = p.alpha),
            radius = p.radius,
            center = Offset(p.x + offsetX, p.y + offsetY)
          )
        }

        // Draw The Bouncing Red Ball
        drawBall(
          x = gameEngine.ballX + offsetX,
          y = gameEngine.ballY + offsetY,
          radius = gameEngine.ballRadius,
          rotation = gameEngine.ballRotation,
          form = gameEngine.ballForm,
          scaleX = gameEngine.scaleX,
          scaleY = gameEngine.scaleY,
          isInvulnerable = gameEngine.isInvulnerable,
          frameTick = frameTick
        )
      }

      // HUD Overlay at Top
      GameHud(
        levelNumber = currentLevelNumber,
        levelTitle = gameEngine.level.title,
        score = gameEngine.score,
        lives = gameEngine.lives,
        ringsCollected = gameEngine.ringsCollectedCount,
        totalRings = gameEngine.totalRingsCount,
        timeSeconds = gameEngine.elapsedTimeSeconds.toInt(),
        isExitOpen = gameEngine.level.exitGate.isOpen,
        onPause = {
          gameEngine.isPaused = true
          showPauseDialog = true
        }
      )

      // Virtual Controls at Bottom
      RetroVirtualControls(
        ballForm = gameEngine.ballForm,
        onMoveChange = { dir -> gameEngine.moveInput = dir },
        onJumpChange = { pressed -> gameEngine.jumpPressed = pressed },
        modifier = Modifier.align(Alignment.BottomCenter)
      )

      // Pause Dialog
      if (showPauseDialog) {
        PauseDialog(
          onResume = {
            showPauseDialog = false
            gameEngine.isPaused = false
          },
          onRestart = {
            showPauseDialog = false
            restartCurrentLevel()
          },
          onOpenSettings = onOpenSettings,
          onMainMenu = onExitToMenu
        )
      }

      // Level Complete Dialog
      if (showCompleteDialog) {
        val hasNext = currentLevelNumber < LevelsRepository.totalLevels
        LevelCompleteDialog(
          levelNumber = currentLevelNumber,
          levelTitle = gameEngine.level.title,
          score = completedScore,
          stars = completedStars,
          timeSeconds = completedTimeSec,
          ringsCollected = gameEngine.ringsCollectedCount,
          totalRings = gameEngine.totalRingsCount,
          hasNextLevel = hasNext,
          onSaveScoreAndNext = { initials ->
            coroutineScope.launch {
              // Save high score to Room DB
              val record = ScoreRecord(
                playerName = initials,
                levelNumber = currentLevelNumber,
                levelName = gameEngine.level.title,
                score = completedScore,
                ringsCollected = gameEngine.ringsCollectedCount,
                totalRings = gameEngine.totalRingsCount,
                timeSeconds = completedTimeSec,
                stars = completedStars
              )
              scoreDao.insertScore(record)

              // Update Level Progress
              val currentProgress = scoreDao.getProgressForLevel(currentLevelNumber)
              val bestScore = max(currentProgress?.bestScore ?: 0, completedScore)
              val bestStars = max(currentProgress?.starsEarned ?: 0, completedStars)
              scoreDao.upsertLevelProgress(
                LevelProgress(
                  levelNumber = currentLevelNumber,
                  isUnlocked = true,
                  starsEarned = bestStars,
                  bestScore = bestScore,
                  bestTimeSeconds = completedTimeSec
                )
              )

              // Unlock next level
              if (hasNext) {
                val nextLvl = currentLevelNumber + 1
                val nextProgress = scoreDao.getProgressForLevel(nextLvl)
                scoreDao.upsertLevelProgress(
                  LevelProgress(
                    levelNumber = nextLvl,
                    isUnlocked = true,
                    starsEarned = nextProgress?.starsEarned ?: 0,
                    bestScore = nextProgress?.bestScore ?: 0
                  )
                )
                showCompleteDialog = false
                currentLevelNumber = nextLvl
              } else {
                showCompleteDialog = false
                onExitToMenu()
              }
            }
          },
          onRetry = {
            showCompleteDialog = false
            restartCurrentLevel()
          },
          onMainMenu = {
            showCompleteDialog = false
            onExitToMenu()
          }
        )
      }

      // Game Over Dialog
      if (showGameOverDialog) {
        GameOverDialog(
          score = gameEngine.score,
          ringsCollected = gameEngine.ringsCollectedCount,
          totalRings = gameEngine.totalRingsCount,
          onRetry = {
            showGameOverDialog = false
            restartCurrentLevel()
          },
          onMainMenu = {
            showGameOverDialog = false
            onExitToMenu()
          }
        )
      }
    }
  }
}

private fun DrawScope.drawParallaxBackground(
  levelNum: Int,
  cameraX: Float,
  cameraY: Float,
  viewWidth: Float,
  viewHeight: Float
) {
  // Theme gradients based on level
  val (topColor, bottomColor) = when (levelNum) {
    1 -> Pair(Color(0xFF0F172A), Color(0xFF1E3A8A)) // Daytime clear retro twilight
    2 -> Pair(Color(0xFF0A192F), Color(0xFF0D3B66)) // Cavern blue
    3 -> Pair(Color(0xFF1C1917), Color(0xFF44403C)) // Granite fortress
    4 -> Pair(Color(0xFF172554), Color(0xFF38BDF8)) // Skyward spire
    5 -> Pair(Color(0xFF2E0854), Color(0xFF7F1D1D)) // Inferno core
    else -> Pair(Color(0xFF0B132B), Color(0xFF1C2541))
  }

  drawRect(
    brush = Brush.verticalGradient(listOf(topColor, bottomColor)),
    size = size
  )

  // Distant parallax hills (scrolling at 0.15x speed)
  val hillOffset = -(cameraX * 0.15f) % 600f
  var hx = -600f + hillOffset
  while (hx < viewWidth + 600f) {
    drawOval(
      color = Color(0x22FFFFFF),
      topLeft = Offset(hx, viewHeight * 0.55f - (cameraY * 0.05f)),
      size = Size(650f, 400f)
    )
    hx += 500f
  }

  // Mid-ground retro pine hills (scrolling at 0.35x speed)
  val midOffset = -(cameraX * 0.35f) % 400f
  var mx = -400f + midOffset
  while (mx < viewWidth + 400f) {
    drawOval(
      color = Color(0x33000000),
      topLeft = Offset(mx, viewHeight * 0.7f - (cameraY * 0.12f)),
      size = Size(450f, 350f)
    )
    mx += 350f
  }
}

private fun DrawScope.drawPlatform(plat: SolidPlatform, offsetX: Float, offsetY: Float) {
  val rx = plat.x + offsetX
  val ry = plat.y + offsetY

  if (plat.isGrass) {
    // Earth base
    drawRect(
      brush = Brush.verticalGradient(listOf(EarthBrown, DarkEarth), startY = ry, endY = ry + plat.height),
      topLeft = Offset(rx, ry),
      size = Size(plat.width, plat.height)
    )
    // Grass top layer
    drawRect(
      color = GrassGreen,
      topLeft = Offset(rx, ry),
      size = Size(plat.width, 16f)
    )
    // Bright grass highlight line
    drawLine(
      color = Color(0xFF81C784),
      start = Offset(rx, ry),
      end = Offset(rx + plat.width, ry),
      strokeWidth = 3f
    )
  } else {
    // Stone / Fortress platform
    drawRect(
      brush = Brush.verticalGradient(listOf(StoneGray, Color(0xFF37474F)), startY = ry, endY = ry + plat.height),
      topLeft = Offset(rx, ry),
      size = Size(plat.width, plat.height)
    )
    // Stone top bevel
    drawLine(
      color = Color(0xFFB0BEC5),
      start = Offset(rx, ry),
      end = Offset(rx + plat.width, ry),
      strokeWidth = 3f
    )
  }
}

private fun DrawScope.drawMovingPlatform(mp: MovingPlatform, offsetX: Float, offsetY: Float) {
  val rx = mp.x + offsetX
  val ry = mp.y + offsetY

  // Metallic platform body
  drawRoundRect(
    brush = Brush.verticalGradient(listOf(Color(0xFF64748B), Color(0xFF334155)), startY = ry, endY = ry + mp.height),
    topLeft = Offset(rx, ry),
    size = Size(mp.width, mp.height),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
  )

  // Glowing yellow warning arrows
  drawRect(
    color = RingGold,
    topLeft = Offset(rx + 6f, ry + 4f),
    size = Size(mp.width - 12f, 4f)
  )
}

private fun DrawScope.drawWaterZone(w: WaterZone, offsetX: Float, offsetY: Float, tick: Long) {
  val rx = w.x + offsetX
  val ry = w.y + offsetY

  val mainColor = if (w.isLava) Color(0xCCFF3D00) else WaterBlue
  val deepColor = if (w.isLava) Color(0xEEBF360C) else DeepWaterBlue

  // Water body
  drawRect(
    brush = Brush.verticalGradient(listOf(mainColor, deepColor), startY = ry, endY = ry + w.height),
    topLeft = Offset(rx, ry),
    size = Size(w.width, w.height)
  )

  // Surface Wave Line
  val wavePath = Path()
  wavePath.moveTo(rx, ry)
  var wx = rx
  val waveStep = 24f
  while (wx <= rx + w.width) {
    val phase = (tick * 0.1f + wx * 0.05f)
    val waveY = ry + sin(phase) * 4f
    wavePath.lineTo(wx, waveY)
    wx += waveStep
  }
  drawPath(
    path = wavePath,
    color = if (w.isLava) Color(0xFFFFD54F) else Color(0xEEFFFFFF),
    style = Stroke(width = 3f)
  )
}

private fun DrawScope.drawAirVent(vent: AirVentZone, offsetX: Float, offsetY: Float, tick: Long) {
  val rx = vent.x + offsetX
  val ry = vent.y + offsetY

  // Vent grill at base
  drawRect(
    color = Color(0xFF455A64),
    topLeft = Offset(rx, ry + vent.height - 18f),
    size = Size(vent.width, 18f)
  )

  // Upward animated wind lines
  var vx = rx + 16f
  while (vx < rx + vent.width - 8f) {
    val speedOffset = ((tick * 4f + vx * 3f) % vent.height)
    val lineY = ry + vent.height - speedOffset
    drawLine(
      color = Color(0x665BC0BE),
      start = Offset(vx, lineY),
      end = Offset(vx, lineY - 26f),
      strokeWidth = 2.5f
    )
    vx += 28f
  }
}

private fun DrawScope.drawSpring(spring: SpringPad, offsetX: Float, offsetY: Float) {
  val rx = spring.x + offsetX
  val ry = spring.y + offsetY

  val isCompressed = spring.compressionTimer > 0f
  val currentHeight = if (isCompressed) spring.height * 0.5f else spring.height
  val drawY = if (isCompressed) ry + spring.height * 0.5f else ry

  // Base mount
  drawRect(
    color = Color(0xFF263238),
    topLeft = Offset(rx, ry + spring.height - 4f),
    size = Size(spring.width, 4f)
  )

  // Spring coil zig-zag
  drawLine(color = Color(0xFFB0BEC5), start = Offset(rx + 10f, ry + spring.height - 4f), end = Offset(rx + spring.width - 10f, drawY + 6f), strokeWidth = 3f)

  // Red/Yellow Bumper Pad top
  drawRoundRect(
    color = BounceRed,
    topLeft = Offset(rx, drawY),
    size = Size(spring.width, 8f),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
  )
  drawRect(
    color = RingGold,
    topLeft = Offset(rx + 8f, drawY + 2f),
    size = Size(spring.width - 16f, 4f)
  )
}

private fun DrawScope.drawSpikeHazard(spk: SpikeHazard, offsetX: Float, offsetY: Float) {
  val rx = spk.x + spk.currentOffsetX + offsetX
  val ry = spk.y + offsetY

  val spikeCount = (spk.width / 16f).toInt().coerceAtLeast(1)
  val spikeW = spk.width / spikeCount

  for (i in 0 until spikeCount) {
    val sx = rx + i * spikeW
    val path = Path().apply {
      moveTo(sx, ry + spk.height)
      lineTo(sx + spikeW / 2f, ry)
      lineTo(sx + spikeW, ry + spk.height)
      close()
    }
    drawPath(
      path = path,
      brush = Brush.verticalGradient(listOf(Color(0xFFCFD8DC), Color(0xFF455A64)), startY = ry, endY = ry + spk.height)
    )
    // Red menacing tip
    drawPath(
      path = Path().apply {
        moveTo(sx + spikeW * 0.3f, ry + spk.height * 0.35f)
        lineTo(sx + spikeW / 2f, ry)
        lineTo(sx + spikeW * 0.7f, ry + spk.height * 0.35f)
        close()
      },
      color = BounceRed
    )
  }
}

private fun DrawScope.drawBreakableBlock(block: BreakableBlock, offsetX: Float, offsetY: Float) {
  val rx = block.x + offsetX
  val ry = block.y + offsetY

  // Cracked Stone Block
  drawRoundRect(
    brush = Brush.linearGradient(listOf(Color(0xFF8D6E63), Color(0xFF5D4037))),
    topLeft = Offset(rx, ry),
    size = Size(block.width, block.height),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
  )

  // Fissure cracks
  drawLine(color = Color(0xFF3E2723), start = Offset(rx + 8f, ry + 6f), end = Offset(rx + 24f, ry + 22f), strokeWidth = 2.5f)
  drawLine(color = Color(0xFF3E2723), start = Offset(rx + 24f, ry + 22f), end = Offset(rx + 18f, ry + 42f), strokeWidth = 2.5f)
  drawLine(color = Color(0xFF3E2723), start = Offset(rx + 24f, ry + 22f), end = Offset(rx + 40f, ry + 30f), strokeWidth = 2f)
}

private fun DrawScope.drawPushableBox(box: PushableBox, offsetX: Float, offsetY: Float) {
  val rx = box.x + offsetX
  val ry = box.y + offsetY

  drawRoundRect(
    color = Color(0xFFA1887F),
    topLeft = Offset(rx, ry),
    size = Size(box.width, box.height),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
  )
  // Cross bracing
  drawRect(
    color = Color(0xFF6D4C41),
    topLeft = Offset(rx + 4f, ry + 4f),
    size = Size(box.width - 8f, box.height - 8f),
    style = Stroke(width = 3f)
  )
  drawLine(color = Color(0xFF6D4C41), start = Offset(rx + 4f, ry + 4f), end = Offset(rx + box.width - 4f, ry + box.height - 4f), strokeWidth = 3f)
}

private fun DrawScope.drawPressureSwitch(sw: PressureSwitch, offsetX: Float, offsetY: Float) {
  val rx = sw.x + offsetX
  val ry = sw.y + offsetY

  val drawH = if (sw.isPressed) sw.height * 0.4f else sw.height
  val drawY = if (sw.isPressed) ry + sw.height * 0.6f else ry

  drawRoundRect(
    color = if (sw.isPressed) Color(0xFF00E676) else RingGold,
    topLeft = Offset(rx, drawY),
    size = Size(sw.width, drawH),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
  )
}

private fun DrawScope.drawDoor(door: DoorEntity, offsetX: Float, offsetY: Float) {
  val rx = door.x + offsetX
  val ry = door.y + offsetY

  val effectiveHeight = door.height * (1f - door.openProgress)
  if (effectiveHeight > 2f) {
    drawRoundRect(
      brush = Brush.verticalGradient(listOf(Color(0xFF78909C), Color(0xFF37474F))),
      topLeft = Offset(rx, ry),
      size = Size(door.width, effectiveHeight),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
    // Iron door grate bars
    var by = ry + 8f
    while (by < ry + effectiveHeight - 4f) {
      drawLine(
        color = Color(0xFFCFD8DC),
        start = Offset(rx + 4f, by),
        end = Offset(rx + door.width - 4f, by),
        strokeWidth = 2.5f
      )
      by += 16f
    }
  }
}

private fun DrawScope.drawCheckpoint(cp: Checkpoint, offsetX: Float, offsetY: Float, tick: Long) {
  val rx = cp.x + offsetX
  val ry = cp.y + offsetY

  // Pole
  drawLine(
    color = Color(0xFFB0BEC5),
    start = Offset(rx, ry),
    end = Offset(rx, ry - 42f),
    strokeWidth = 3.5f
  )

  // Flag banner (waves subtly)
  val wave = sin(tick * 0.15f) * 3f
  val flagColor = if (cp.isActivated) Color(0xFF00E676) else BounceRed
  val flagPath = Path().apply {
    moveTo(rx, ry - 42f)
    lineTo(rx + 26f + wave, ry - 30f)
    lineTo(rx, ry - 18f)
    close()
  }
  drawPath(path = flagPath, color = flagColor)
}

private fun DrawScope.drawGoldenRing(ring: Ring, offsetX: Float, offsetY: Float, tick: Long) {
  val rx = ring.x + offsetX
  val ry = ring.y + offsetY

  val pulse = sin(ring.sparklePhase) * 2.5f

  // Outer Golden Ring Halo
  drawCircle(
    color = RingGold.copy(alpha = 0.25f),
    radius = ring.radius + pulse + 4f,
    center = Offset(rx, ry)
  )

  // Golden metallic hoop
  drawCircle(
    brush = Brush.sweepGradient(
      colors = listOf(RingGoldLight, RingGold, RingAmber, RingGoldLight),
      center = Offset(rx, ry)
    ),
    radius = ring.radius + pulse,
    center = Offset(rx, ry),
    style = Stroke(width = 6f)
  )

  // Shimmer sparkle
  val sparkleX = rx + cos(ring.sparklePhase) * ring.radius
  val sparkleY = ry + sin(ring.sparklePhase) * ring.radius
  drawCircle(
    color = Color.White,
    radius = 3f,
    center = Offset(sparkleX, sparkleY)
  )
}

private fun DrawScope.drawPowerUpOrb(pu: PowerUpEntity, offsetX: Float, offsetY: Float, tick: Long) {
  val rx = pu.x + offsetX
  val ry = pu.y + offsetY

  val bobbing = sin(tick * 0.1f) * 4f
  val cy = ry + bobbing

  val (orbColor, glowColor) = when (pu.type) {
    PowerUpType.FLOAT_ORB -> Pair(Color(0xFFFFB300), Color(0xFFFFD54F))
    PowerUpType.STONE_ORB -> Pair(Color(0xFF78909C), Color(0xFFB0BEC5))
    PowerUpType.NORMAL_ORB -> Pair(BounceRed, BounceRedLight)
    PowerUpType.SUPER_BOUNCE -> Pair(Color(0xFF00E5FF), Color(0xFF84FFFF))
    PowerUpType.EXTRA_LIFE -> Pair(Color(0xFFE91E63), Color(0xFFF48FB1))
  }

  // Pulsating glow halo
  drawCircle(
    color = glowColor.copy(alpha = 0.35f),
    radius = pu.radius * 1.5f,
    center = Offset(rx, cy)
  )

  // Orb body
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(Color.White, orbColor),
      center = Offset(rx - pu.radius * 0.3f, cy - pu.radius * 0.3f),
      radius = pu.radius * 1.2f
    ),
    radius = pu.radius,
    center = Offset(rx, cy)
  )
}

private fun DrawScope.drawExitGate(exit: ExitGate, offsetX: Float, offsetY: Float, tick: Long) {
  val rx = exit.x + offsetX
  val ry = exit.y + offsetY

  if (exit.isOpen) {
    // Glowing opened portal ring
    drawCircle(
      color = RingGold.copy(alpha = 0.35f),
      radius = exit.radius * 1.4f,
      center = Offset(rx, ry)
    )

    // Swirling vortex strokes
    rotate(degrees = exit.spinAngle, pivot = Offset(rx, ry)) {
      drawCircle(
        brush = Brush.sweepGradient(
          colors = listOf(RingGoldLight, RingAmber, RetroCyan, RingGoldLight),
          center = Offset(rx, ry)
        ),
        radius = exit.radius,
        center = Offset(rx, ry),
        style = Stroke(width = 8f)
      )
    }

    // Portal inner core
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(Color.White, RingGold, Color.Transparent),
        center = Offset(rx, ry),
        radius = exit.radius * 0.8f
      ),
      radius = exit.radius * 0.8f,
      center = Offset(rx, ry)
    )
  } else {
    // Locked Gate (Dark metallic with red locked indicator)
    drawCircle(
      color = Color(0xFF455A64),
      radius = exit.radius,
      center = Offset(rx, ry),
      style = Stroke(width = 7f)
    )
    drawCircle(
      color = BounceRed,
      radius = exit.radius * 0.35f,
      center = Offset(rx, ry)
    )
  }
}

private fun DrawScope.drawBall(
  x: Float,
  y: Float,
  radius: Float,
  rotation: Float,
  form: BallForm,
  scaleX: Float,
  scaleY: Float,
  isInvulnerable: Boolean,
  frameTick: Long
) {
  // Invulnerability flashing
  if (isInvulnerable && (frameTick % 6 < 3)) return

  scale(scaleX = scaleX, scaleY = scaleY, pivot = Offset(x, y)) {
    rotate(degrees = rotation, pivot = Offset(x, y)) {
      when (form) {
        BallForm.NORMAL -> {
          // Classic Glossy Red Rubber Ball
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(BounceRedLight, BounceRed, BounceRedDark),
              center = Offset(x - radius * 0.35f, y - radius * 0.35f),
              radius = radius * 1.3f
            ),
            radius = radius,
            center = Offset(x, y)
          )
          // Retro seam line to emphasize rolling rotation
          drawLine(
            color = Color(0x44FFFFFF),
            start = Offset(x - radius * 0.7f, y),
            end = Offset(x + radius * 0.7f, y),
            strokeWidth = 2.5f
          )
        }
        BallForm.FLOAT -> {
          // Inflatable Balloon Ball (Yellow-Orange buoyant)
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFB300), Color(0xFFF57C00)),
              center = Offset(x - radius * 0.35f, y - radius * 0.35f),
              radius = radius * 1.3f
            ),
            radius = radius,
            center = Offset(x, y)
          )
          // Beach ball stripes
          drawLine(
            color = Color(0x66FFFFFF),
            start = Offset(x, y - radius),
            end = Offset(x, y + radius),
            strokeWidth = 3f
          )
        }
        BallForm.STONE -> {
          // Heavy Granite Stone Ball
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(Color(0xFFB0BEC5), Color(0xFF78909C), Color(0xFF37474F)),
              center = Offset(x - radius * 0.35f, y - radius * 0.35f),
              radius = radius * 1.3f
            ),
            radius = radius,
            center = Offset(x, y)
          )
          // Granite fleck dots
          drawCircle(color = Color(0xFF263238), radius = 2f, center = Offset(x - 8f, y - 4f))
          drawCircle(color = Color(0xFF263238), radius = 2.5f, center = Offset(x + 7f, y + 6f))
          drawCircle(color = Color(0xFF263238), radius = 1.8f, center = Offset(x - 3f, y + 10f))
        }
      }

      // Glossy shine reflection highlight
      drawCircle(
        color = Color.White.copy(alpha = 0.8f),
        radius = radius * 0.28f,
        center = Offset(x - radius * 0.35f, y - radius * 0.35f)
      )
    }
  }
}

@Composable
private fun GameHud(
  levelNumber: Int,
  levelTitle: String,
  score: Int,
  lives: Int,
  ringsCollected: Int,
  totalRings: Int,
  timeSeconds: Int,
  isExitOpen: Boolean,
  onPause: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.statusBars)
      .padding(horizontal = 16.dp, vertical = 10.dp)
  ) {
    // Left: Hearts Lives & Rings
    Row(
      modifier = Modifier.align(Alignment.TopStart),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Lives Hearts
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = RetroSurface.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(1.dp, RetroSurfaceLight)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
          repeat(lives.coerceAtLeast(0)) {
            Icon(
              imageVector = Icons.Default.Favorite,
              contentDescription = null,
              tint = BounceRed,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // Rings Counter
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = RetroSurface.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isExitOpen) RingGold else RetroSurfaceLight)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .size(14.dp)
              .clip(CircleShape)
              .border(2.5.dp, if (isExitOpen) RingGold else Color(0xFFFFD54F), CircleShape)
          )
          Text(
            text = "$ringsCollected/$totalRings",
            fontFamily = PixelFontFamily,
            fontSize = 11.sp,
            color = if (isExitOpen) RingGold else Color.White
          )
        }
      }
    }

    // Right: Score & Timer & Pause
    Row(
      modifier = Modifier.align(Alignment.TopEnd),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = RetroSurface.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(1.dp, RetroSurfaceLight)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "$score",
            fontFamily = PixelFontFamily,
            fontSize = 12.sp,
            color = RingGold
          )
          Text(
            text = "${timeSeconds}s",
            fontFamily = BouncyFontFamily,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
          )
        }
      }

      IconButton(
        onClick = onPause,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(RetroSurface.copy(alpha = 0.85f))
          .testTag("pause_button")
      ) {
        Icon(
          imageVector = Icons.Default.Pause,
          contentDescription = "Pause",
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}
