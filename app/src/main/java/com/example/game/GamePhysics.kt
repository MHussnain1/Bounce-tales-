package com.example.game

import com.example.audio.RetroAudioSynthesizer
import com.example.data.*
import kotlin.math.*

data class Particle(
  var x: Float,
  var y: Float,
  var vx: Float,
  var vy: Float,
  var color: androidx.compose.ui.graphics.Color,
  var radius: Float,
  var alpha: Float = 1f,
  var lifeTime: Float = 1f,
  var maxLife: Float = 1f
)

class GameEngine(
  val level: LevelDefinition,
  val audio: RetroAudioSynthesizer,
  val onLevelComplete: (score: Int, stars: Int, timeSec: Int) -> Unit,
  val onGameOver: () -> Unit
) {
  // Ball State
  var ballX = level.spawnX
  var ballY = level.spawnY
  var ballVx = 0f
  var ballVy = 0f
  var ballRotation = 0f
  var ballForm = BallForm.NORMAL
  var scaleX = 1f
  var scaleY = 1f

  var lastCheckpointX = level.spawnX
  var lastCheckpointY = level.spawnY

  var lives = 3
  var score = 0
  var ringsCollectedCount = 0
  val totalRingsCount = level.rings.size
  var elapsedTimeSeconds = 0f
  var isCompleted = false
  var isGameOver = false
  var isPaused = false

  var isGrounded = false
  var isInWater = false
  var isInvulnerable = false
  private var invulnerableTimer = 0f

  val particles = mutableListOf<Particle>()

  // Input states
  var moveInput = 0f // -1f = left, 1f = right
  var jumpPressed = false
  private var jumpConsumed = false

  // Ball physical properties by form
  val ballRadius: Float
    get() = when (ballForm) {
      BallForm.NORMAL -> 22f
      BallForm.FLOAT -> 26f
      BallForm.STONE -> 28f
    }

  private val gravity: Float
    get() = when (ballForm) {
      BallForm.NORMAL -> 0.85f
      BallForm.FLOAT -> 0.35f
      BallForm.STONE -> 1.45f
    }

  private val restitution: Float
    get() = when (ballForm) {
      BallForm.NORMAL -> 0.72f
      BallForm.FLOAT -> 0.88f
      BallForm.STONE -> 0.32f
    }

  private val rollAcceleration: Float
    get() = when (ballForm) {
      BallForm.NORMAL -> 0.95f
      BallForm.FLOAT -> 0.75f
      BallForm.STONE -> 1.15f
    }

  private val maxSpeed: Float
    get() = when (ballForm) {
      BallForm.NORMAL -> 11f
      BallForm.FLOAT -> 8.5f
      BallForm.STONE -> 13f
    }

  fun update(dt: Float = 0.016f) {
    if (isCompleted || isGameOver || isPaused) return

    elapsedTimeSeconds += dt

    if (invulnerableTimer > 0f) {
      invulnerableTimer -= dt
      if (invulnerableTimer <= 0f) isInvulnerable = false
    }

    // 1. Process Horizontal Roll Input
    if (moveInput != 0f) {
      ballVx += moveInput * rollAcceleration
      ballVx = ballVx.coerceIn(-maxSpeed, maxSpeed)
    } else {
      // Natural rolling friction
      ballVx *= 0.92f
      if (abs(ballVx) < 0.05f) ballVx = 0f
    }

    // 2. Water check
    isInWater = false
    for (water in level.waterZones) {
      if (ballX + ballRadius > water.x && ballX - ballRadius < water.x + water.width &&
        ballY + ballRadius > water.y && ballY - ballRadius < water.y + water.height
      ) {
        if (water.isLava) {
          killBall("lava")
          return
        }
        isInWater = true
        break
      }
    }

    // 3. Air vent updrafts
    for (vent in level.airVents) {
      if (ballX + ballRadius > vent.x && ballX - ballRadius < vent.x + vent.width &&
        ballY + ballRadius > vent.y && ballY - ballRadius < vent.y + vent.height
      ) {
        val multiplier = if (ballForm == BallForm.FLOAT) 1.5f else if (ballForm == BallForm.STONE) 0.4f else 1.0f
        ballVy += vent.updraftForce * 0.07f * multiplier
        if (Math.random() < 0.2) {
          spawnWindParticles(vent.x + (Math.random() * vent.width).toFloat(), vent.y + vent.height - 20f)
        }
      }
    }

    // 4. Gravity and Buoyancy
    if (isInWater) {
      when (ballForm) {
        BallForm.FLOAT -> {
          // Floats rapidly upward
          ballVy -= 0.75f
          ballVx *= 0.88f
          ballVy *= 0.88f
        }
        BallForm.NORMAL -> {
          // Neutrally buoyant, slow sink
          ballVy += 0.15f
          ballVx *= 0.85f
          ballVy *= 0.85f
        }
        BallForm.STONE -> {
          // Sinks quickly to seabed
          ballVy += 0.85f
          ballVx *= 0.90f
        }
      }
    } else {
      ballVy += gravity
    }

    // Terminal velocity
    ballVy = ballVy.coerceIn(-28f, 26f)

    // 5. Jump logic
    if (jumpPressed && !jumpConsumed) {
      if (isGrounded) {
        val jumpImpulse = when (ballForm) {
          BallForm.NORMAL -> -16.5f
          BallForm.FLOAT -> -18.5f
          BallForm.STONE -> -13.0f
        }
        ballVy = jumpImpulse
        isGrounded = false
        jumpConsumed = true
        scaleX = 0.8f
        scaleY = 1.25f
        audio.playBounce()
        spawnDustParticles(ballX, ballY + ballRadius)
      } else if (isInWater && ballForm == BallForm.FLOAT) {
        ballVy = -15f
        jumpConsumed = true
        audio.playBounce()
      }
    }
    if (!jumpPressed) {
      jumpConsumed = false
    }

    // 6. Update position
    ballX += ballVx
    ballY += ballVy

    // Ball rolling rotation based on distance travelled
    ballRotation += (ballVx / ballRadius) * (180f / PI.toFloat())

    // Elastic squash & stretch recovery
    scaleX += (1f - scaleX) * 0.18f
    scaleY += (1f - scaleY) * 0.18f

    // 7. Update Moving Platforms
    for (mp in level.movingPlatforms) {
      val dx = mp.endX - mp.startX
      val dy = mp.endY - mp.startY
      val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
      val dirX = (dx / dist) * mp.speed
      val dirY = (dy / dist) * mp.speed

      if (mp.forward) {
        mp.x += dirX
        mp.y += dirY
        val currentDist = sqrt((mp.x - mp.startX) * (mp.x - mp.startX) + (mp.y - mp.startY) * (mp.y - mp.startY))
        if (currentDist >= dist) mp.forward = false
      } else {
        mp.x -= dirX
        mp.y -= dirY
        val currentDist = sqrt((mp.x - mp.startX) * (mp.x - mp.startX) + (mp.y - mp.startY) * (mp.y - mp.startY))
        if (currentDist <= 2f) mp.forward = true
      }
    }

    // 8. Update Moving Spikes
    for (spk in level.spikes) {
      if (spk.isMoving) {
        spk.currentOffsetX += spk.moveDir * spk.moveSpeed
        if (spk.currentOffsetX >= spk.moveRangeX) spk.moveDir = -1f
        if (spk.currentOffsetX <= 0f) spk.moveDir = 1f
      }
    }

    // 9. Update Spring Pads compression timer
    for (spring in level.springs) {
      if (spring.compressionTimer > 0f) {
        spring.compressionTimer -= dt * 4f
      }
    }

    // 10. Update Doors (open/closed animation)
    for (door in level.doors) {
      val targetOpen = door.isOpen
      if (targetOpen && door.openProgress < 1f) {
        door.openProgress = (door.openProgress + dt * 2.5f).coerceAtMost(1f)
      } else if (!targetOpen && door.openProgress > 0f) {
        door.openProgress = (door.openProgress - dt * 2.5f).coerceAtLeast(0f)
      }
    }

    // 11. Collisions
    isGrounded = false

    // Collide with Solid Platforms
    for (plat in level.platforms) {
      resolvePlatformCollision(plat.x, plat.y, plat.width, plat.height)
    }

    // Collide with Moving Platforms
    for (mp in level.movingPlatforms) {
      resolvePlatformCollision(mp.x, mp.y, mp.width, mp.height)
    }

    // Collide with Doors (if not fully open)
    for (door in level.doors) {
      if (door.openProgress < 0.95f) {
        val effectiveHeight = door.height * (1f - door.openProgress)
        resolvePlatformCollision(door.x, door.y, door.width, effectiveHeight)
      }
    }

    // Collide with Breakable Blocks
    for (block in level.breakableBlocks) {
      if (!block.isDestroyed) {
        val hit = checkCircleRect(ballX, ballY, ballRadius, block.x, block.y, block.width, block.height)
        if (hit) {
          // Check if stone ball hits with speed or dropping
          val canBreak = ballForm == BallForm.STONE || abs(ballVy) > 12f
          if (canBreak) {
            block.isDestroyed = true
            audio.playBreakBlock()
            spawnBlockBreakParticles(block.x + block.width / 2f, block.y + block.height / 2f)
            score += 250
          } else {
            resolvePlatformCollision(block.x, block.y, block.width, block.height)
          }
        }
      }
    }

    // Collide & Push Boxes
    for (box in level.pushableBoxes) {
      // Box gravity & ground check
      box.vy += 0.8f
      box.y += box.vy
      box.x += box.vx
      box.vx *= 0.85f

      // Box platform collisions
      for (plat in level.platforms) {
        if (box.x + box.width > plat.x && box.x < plat.x + plat.width &&
          box.y + box.height > plat.y && box.y < plat.y + plat.height
        ) {
          box.y = plat.y - box.height
          box.vy = 0f
        }
      }

      // Ball pushing box
      if (checkCircleRect(ballX, ballY, ballRadius, box.x, box.y, box.width, box.height)) {
        if (ballX < box.x) {
          box.vx = max(2.5f, abs(ballVx) * 0.8f)
          ballX = box.x - ballRadius
          ballVx *= 0.5f
        } else if (ballX > box.x + box.width) {
          box.vx = -max(2.5f, abs(ballVx) * 0.8f)
          ballX = box.x + box.width + ballRadius
          ballVx *= 0.5f
        } else if (ballY < box.y) {
          ballY = box.y - ballRadius
          ballVy = 0f
          isGrounded = true
        }
      }

      // Check pressure switches activated by box
      for (sw in level.switches) {
        if (box.x + box.width > sw.x && box.x < sw.x + sw.width &&
          box.y + box.height >= sw.y - 4f && box.y <= sw.y + sw.height
        ) {
          sw.isPressed = true
          level.doors.find { it.id == sw.targetDoorId }?.isOpen = true
        }
      }
    }

    // Pressure Switches activated by Ball
    for (sw in level.switches) {
      val isBallOnSwitch = checkCircleRect(ballX, ballY, ballRadius, sw.x, sw.y, sw.width, sw.height)
      if (isBallOnSwitch) {
        if (!sw.requiresHeavy || ballForm == BallForm.STONE) {
          if (!sw.isPressed) {
            sw.isPressed = true
            audio.playClick()
            level.doors.find { it.id == sw.targetDoorId }?.isOpen = true
          }
        }
      }
    }

    // Spring Pads
    for (spring in level.springs) {
      if (checkCircleRect(ballX, ballY, ballRadius, spring.x, spring.y, spring.width, spring.height)) {
        if (ballVy > 0f) {
          spring.compressionTimer = 1f
          ballVy = spring.bounceForce * (if (ballForm == BallForm.FLOAT) 1.25f else 1.0f)
          audio.playSpring()
          scaleX = 0.7f
          scaleY = 1.35f
          spawnDustParticles(ballX, ballY + ballRadius)
        }
      }
    }

    // Spike Hazards
    if (!isInvulnerable) {
      for (spike in level.spikes) {
        val spkX = spike.x + spike.currentOffsetX
        if (checkCircleRect(ballX, ballY, ballRadius * 0.75f, spkX, spike.y, spike.width, spike.height)) {
          killBall("spike")
          return
        }
      }
    }

    // Checkpoint flags
    for (cp in level.checkpoints) {
      if (!cp.isActivated) {
        val dist = hypot(ballX - cp.x, ballY - cp.y)
        if (dist < 40f) {
          cp.isActivated = true
          lastCheckpointX = cp.x
          lastCheckpointY = cp.y - 10f
          audio.playClick()
          spawnCheckpointSparkles(cp.x, cp.y)
          score += 100
        }
      }
    }

    // Collect Rings
    for (ring in level.rings) {
      if (!ring.isCollected) {
        ring.sparklePhase = (ring.sparklePhase + dt * 5f) % (2f * PI.toFloat())
        val dist = hypot(ballX - ring.x, ballY - ring.y)
        if (dist < ring.radius + ballRadius) {
          ring.isCollected = true
          ringsCollectedCount++
          score += 500
          audio.playRing()
          spawnRingParticles(ring.x, ring.y)

          if (ringsCollectedCount >= totalRingsCount) {
            level.exitGate.isOpen = true
            audio.playPowerUp()
          }
        }
      }
    }

    // Power-Up Orbs
    for (pu in level.powerUps) {
      if (!pu.isCollected) {
        val dist = hypot(ballX - pu.x, ballY - pu.y)
        if (dist < pu.radius + ballRadius) {
          pu.isCollected = true
          audio.playPowerUp()
          spawnPowerUpParticles(pu.x, pu.y)
          score += 300
          when (pu.type) {
            PowerUpType.FLOAT_ORB -> ballForm = BallForm.FLOAT
            PowerUpType.STONE_ORB -> ballForm = BallForm.STONE
            PowerUpType.NORMAL_ORB -> ballForm = BallForm.NORMAL
            PowerUpType.SUPER_BOUNCE -> ballVy = -24f
            PowerUpType.EXTRA_LIFE -> lives = (lives + 1).coerceAtMost(5)
          }
        }
      }
    }

    // Exit Gate Check
    val exit = level.exitGate
    exit.spinAngle = (exit.spinAngle + dt * 90f) % 360f
    if (exit.isOpen) {
      val dist = hypot(ballX - exit.x, ballY - exit.y)
      if (dist < exit.radius + ballRadius * 0.7f) {
        // Level Completed!
        isCompleted = true
        audio.playVictory()
        val parTime = level.targetParTimeSeconds
        val timeSec = elapsedTimeSeconds.toInt()
        val timeBonus = max(0, (parTime - timeSec) * 40)
        val livesBonus = lives * 400
        val finalScore = score + 1000 + timeBonus + livesBonus

        var stars = 1
        if (ringsCollectedCount >= totalRingsCount) stars = 2
        if (ringsCollectedCount >= totalRingsCount && timeSec <= parTime && lives == 3) stars = 3

        onLevelComplete(finalScore, stars, timeSec)
      }
    }

    // Bottom out-of-bounds void check
    if (ballY > level.worldHeight + 60f) {
      killBall("pit")
    }

    // Update Particles
    val iterator = particles.iterator()
    while (iterator.hasNext()) {
      val p = iterator.next()
      p.x += p.vx
      p.y += p.vy
      p.vy += 0.2f // particle gravity
      p.lifeTime -= dt
      p.alpha = (p.lifeTime / p.maxLife).coerceIn(0f, 1f)
      if (p.lifeTime <= 0f) iterator.remove()
    }
  }

  private fun resolvePlatformCollision(platX: Float, platY: Float, platW: Float, platH: Float) {
    // AABB vs Circle test
    val closestX = ballX.coerceIn(platX, platX + platW)
    val closestY = ballY.coerceIn(platY, platY + platH)
    val distX = ballX - closestX
    val distY = ballY - closestY
    val distanceSq = distX * distX + distY * distY

    if (distanceSq < ballRadius * ballRadius) {
      val distance = sqrt(distanceSq).coerceAtLeast(0.001f)
      val overlap = ballRadius - distance
      val normalX = distX / distance
      val normalY = distY / distance

      ballX += normalX * overlap
      ballY += normalY * overlap

      val dot = ballVx * normalX + ballVy * normalY
      if (dot < 0f) {
        ballVx -= (1f + restitution) * dot * normalX
        ballVy -= (1f + restitution) * dot * normalY

        // Ground contact
        if (normalY < -0.6f) {
          isGrounded = true
          // If low vertical velocity, prevent jittery micro-bouncing
          if (abs(ballVy) < 1.8f) ballVy = 0f
          // Squash effect on landing
          if (abs(dot) > 5f) {
            scaleX = 1.25f
            scaleY = 0.8f
            audio.playBounce()
          }
        }
      }
    }
  }

  private fun checkCircleRect(cx: Float, cy: Float, radius: Float, rx: Float, ry: Float, rw: Float, rh: Float): Boolean {
    val closestX = cx.coerceIn(rx, rx + rw)
    val closestY = cy.coerceIn(ry, ry + rh)
    val distX = cx - closestX
    val distY = cy - closestY
    return (distX * distX + distY * distY) < (radius * radius)
  }

  private fun killBall(cause: String) {
    audio.playSpikeHurt()
    spawnDeathParticles(ballX, ballY)
    lives--
    if (lives <= 0) {
      isGameOver = true
      onGameOver()
    } else {
      // Respawn at checkpoint
      ballX = lastCheckpointX
      ballY = lastCheckpointY
      ballVx = 0f
      ballVy = 0f
      isInvulnerable = true
      invulnerableTimer = 1.5f
    }
  }

  private fun spawnRingParticles(x: Float, y: Float) {
    for (i in 0 until 18) {
      val angle = (Math.random() * 2 * PI).toFloat()
      val speed = (Math.random() * 6 + 2).toFloat()
      particles.add(
        Particle(
          x = x,
          y = y,
          vx = cos(angle) * speed,
          vy = sin(angle) * speed,
          color = androidx.compose.ui.graphics.Color(0xFFFFD700),
          radius = (Math.random() * 4 + 3).toFloat(),
          lifeTime = 0.6f,
          maxLife = 0.6f
        )
      )
    }
  }

  private fun spawnPowerUpParticles(x: Float, y: Float) {
    for (i in 0 until 16) {
      val angle = (Math.random() * 2 * PI).toFloat()
      val speed = (Math.random() * 7 + 2).toFloat()
      particles.add(
        Particle(
          x = x,
          y = y,
          vx = cos(angle) * speed,
          vy = sin(angle) * speed,
          color = androidx.compose.ui.graphics.Color(0xFF00E5FF),
          radius = (Math.random() * 5 + 3).toFloat(),
          lifeTime = 0.7f,
          maxLife = 0.7f
        )
      )
    }
  }

  private fun spawnDeathParticles(x: Float, y: Float) {
    val ballColor = when (ballForm) {
      BallForm.NORMAL -> androidx.compose.ui.graphics.Color(0xFFE53935)
      BallForm.FLOAT -> androidx.compose.ui.graphics.Color(0xFFFFB300)
      BallForm.STONE -> androidx.compose.ui.graphics.Color(0xFF78909C)
    }
    for (i in 0 until 24) {
      val angle = (Math.random() * 2 * PI).toFloat()
      val speed = (Math.random() * 8 + 3).toFloat()
      particles.add(
        Particle(
          x = x,
          y = y,
          vx = cos(angle) * speed,
          vy = sin(angle) * speed,
          color = ballColor,
          radius = (Math.random() * 6 + 3).toFloat(),
          lifeTime = 0.8f,
          maxLife = 0.8f
        )
      )
    }
  }

  private fun spawnDustParticles(x: Float, y: Float) {
    for (i in 0 until 8) {
      val vx = (Math.random() * 4 - 2).toFloat()
      val vy = (-Math.random() * 2.5).toFloat()
      particles.add(
        Particle(
          x = x,
          y = y,
          vx = vx,
          vy = vy,
          color = androidx.compose.ui.graphics.Color(0xFFB0BEC5),
          radius = (Math.random() * 3 + 2).toFloat(),
          lifeTime = 0.4f,
          maxLife = 0.4f
        )
      )
    }
  }

  private fun spawnBlockBreakParticles(x: Float, y: Float) {
    for (i in 0 until 16) {
      val angle = (Math.random() * 2 * PI).toFloat()
      val speed = (Math.random() * 7 + 2).toFloat()
      particles.add(
        Particle(
          x = x,
          y = y,
          vx = cos(angle) * speed,
          vy = sin(angle) * speed,
          color = androidx.compose.ui.graphics.Color(0xFF8D6E63),
          radius = (Math.random() * 5 + 3).toFloat(),
          lifeTime = 0.6f,
          maxLife = 0.6f
        )
      )
    }
  }

  private fun spawnCheckpointSparkles(x: Float, y: Float) {
    for (i in 0 until 12) {
      particles.add(
        Particle(
          x = x,
          y = y,
          vx = (Math.random() * 4 - 2).toFloat(),
          vy = (-Math.random() * 5 - 1).toFloat(),
          color = androidx.compose.ui.graphics.Color(0xFF00E676),
          radius = 3.5f,
          lifeTime = 0.5f,
          maxLife = 0.5f
        )
      )
    }
  }

  private fun spawnWindParticles(x: Float, y: Float) {
    particles.add(
      Particle(
        x = x,
        y = y,
        vx = (Math.random() * 1 - 0.5).toFloat(),
        vy = (-Math.random() * 8 - 4).toFloat(),
        color = androidx.compose.ui.graphics.Color(0x88FFFFFF),
        radius = 2.5f,
        lifeTime = 0.7f,
        maxLife = 0.7f
      )
    )
  }
}
