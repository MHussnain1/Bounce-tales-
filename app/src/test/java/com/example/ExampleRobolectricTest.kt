package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.RetroAudioSynthesizer
import com.example.data.BallForm
import com.example.data.LevelsRepository
import com.example.game.GameEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Bounce Tales", appName)
  }

  @Test
  fun `verify level 1 definitions and entities`() {
    val level1 = LevelsRepository.getLevel(1)
    assertEquals(1, level1.number)
    assertEquals("Emerald Meadow", level1.title)
    assertTrue("Level 1 should have golden rings", level1.rings.isNotEmpty())
    assertNotNull("Level 1 should have exit gate", level1.exitGate)
  }

  @Test
  fun `verify game engine physics initialization`() {
    val level1 = LevelsRepository.getLevel(1)
    val audio = RetroAudioSynthesizer()
    val engine = GameEngine(
      level = level1,
      audio = audio,
      onLevelComplete = { _, _, _ -> },
      onGameOver = {}
    )

    assertEquals(BallForm.NORMAL, engine.ballForm)
    assertEquals(3, engine.lives)
    assertEquals(0, engine.score)

    // Simulate 10 frames of gravity
    for (i in 0 until 10) {
      engine.update(0.016f)
    }
    assertTrue("Ball Y should update with gravity", engine.ballY >= level1.spawnY)
  }
}
