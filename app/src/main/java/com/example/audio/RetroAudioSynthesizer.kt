package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Pure Kotlin Real-Time Audio Synthesizer using Android AudioTrack.
 * Generates authentic upbeat retro 8-bit / polyphonic arcade music and bouncy sound effects.
 */
class RetroAudioSynthesizer {
  private val sampleRate = 22050
  private val scope = CoroutineScope(Dispatchers.Default)

  var isMusicEnabled = true
    set(value) {
      field = value
      if (!value) stopMusic() else startMusic()
    }

  var isSfxEnabled = true
  var musicVolume = 0.6f
  var sfxVolume = 0.8f

  private var musicJob: Job? = null
  private var isPlayingMusic = false

  fun startMusic() {
    if (!isMusicEnabled || isPlayingMusic) return
    isPlayingMusic = true
    musicJob = scope.launch {
      runMusicLoop()
    }
  }

  fun stopMusic() {
    isPlayingMusic = false
    musicJob?.cancel()
    musicJob = null
  }

  private suspend fun CoroutineScope.runMusicLoop() {
    // Upbeat classic Bounce arcade melody notes (frequencies in Hz) and durations in beats
    // Bouncy C-Major / Pentatonic arcade loop
    val melody = listOf(
      // Measure 1: Bouncy C-E-G motif
      Pair(523.25f, 150), Pair(659.25f, 150), Pair(783.99f, 200), Pair(659.25f, 100),
      Pair(523.25f, 150), Pair(783.99f, 150), Pair(1046.50f, 250), Pair(0f, 100),
      // Measure 2: F-A-C run
      Pair(698.46f, 150), Pair(880.00f, 150), Pair(1046.50f, 200), Pair(880.00f, 100),
      Pair(783.99f, 150), Pair(659.25f, 150), Pair(587.33f, 250), Pair(0f, 100),
      // Measure 3: G-B-D high bounce
      Pair(783.99f, 150), Pair(987.77f, 150), Pair(1174.66f, 200), Pair(987.77f, 100),
      Pair(783.99f, 150), Pair(1046.50f, 200), Pair(1318.51f, 300), Pair(0f, 100),
      // Measure 4: Playful resolution
      Pair(1046.50f, 150), Pair(783.99f, 150), Pair(659.25f, 150), Pair(587.33f, 150),
      Pair(523.25f, 350), Pair(0f, 200)
    )

    // Parallel Bassline frequencies
    val bassline = listOf(
      130.81f, 130.81f, 164.81f, 196.00f,
      174.61f, 174.61f, 220.00f, 196.00f,
      196.00f, 196.00f, 246.94f, 196.00f,
      130.81f, 196.00f, 130.81f, 0f
    )

    var bassIdx = 0

    while (isActive && isPlayingMusic) {
      for ((freq, durationMs) in melody) {
        if (!isActive || !isPlayingMusic) break
        val bassFreq = bassline[bassIdx % bassline.size]
        bassIdx++

        if (freq > 0f) {
          playPolyphonicTone(leadFreq = freq, bassFreq = bassFreq, durationMs = durationMs, volume = musicVolume * 0.45f)
        } else {
          delay(durationMs.toLong())
        }
      }
    }
  }

  private fun playPolyphonicTone(leadFreq: Float, bassFreq: Float, durationMs: Int, volume: Float) {
    try {
      val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(100)
      val buffer = ShortArray(numSamples)
      for (i in 0 until numSamples) {
        val t = i.toDouble() / sampleRate
        // Retro pulse/square wave for lead + triangle/sine wave for bass
        val leadPhase = (t * leadFreq) % 1.0
        val leadSample = if (leadPhase < 0.5) 0.6 else -0.6

        val bassPhase = (t * bassFreq) % 1.0
        val bassSample = sin(2.0 * PI * bassFreq * t) * 0.7

        // Envelope: quick attack and smooth decay
        val attackSamples = (numSamples * 0.1).toInt().coerceAtLeast(1)
        val decaySamples = (numSamples * 0.9).toInt().coerceAtLeast(1)
        val env = when {
          i < attackSamples -> i.toDouble() / attackSamples
          else -> (numSamples - i).toDouble() / decaySamples
        }.coerceIn(0.0, 1.0)

        val mixed = (leadSample * 0.6 + bassSample * 0.4) * env * volume * Short.MAX_VALUE
        buffer[i] = mixed.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
      }

      val track = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        )
        .setBufferSizeInBytes(buffer.size * 2)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

      track.write(buffer, 0, buffer.size)
      track.play()
      // Let it play, track automatically cleans up
      scope.launch {
        delay(durationMs.toLong() + 30)
        try {
          track.stop()
          track.release()
        } catch (_: Exception) {}
      }
    } catch (_: Exception) {}
  }

  // --- Sound Effects ---

  fun playBounce() {
    if (!isSfxEnabled) return
    scope.launch {
      // Frequency slides from 260Hz up to 580Hz with quick decay
      generateToneSweep(startFreq = 260f, endFreq = 580f, durationMs = 110, volume = sfxVolume * 0.7f)
    }
  }

  fun playRing() {
    if (!isSfxEnabled) return
    scope.launch {
      // High bright sparkling chime
      generateTone(freq = 1174.66f, durationMs = 80, volume = sfxVolume * 0.75f)
      generateTone(freq = 1760.00f, durationMs = 140, volume = sfxVolume * 0.85f)
    }
  }

  fun playSpring() {
    if (!isSfxEnabled) return
    scope.launch {
      // High-energy boing sweep
      generateToneSweep(startFreq = 200f, endFreq = 950f, durationMs = 220, volume = sfxVolume * 0.9f)
    }
  }

  fun playPowerUp() {
    if (!isSfxEnabled) return
    scope.launch {
      // Ascending heroic arpeggio
      val notes = listOf(523.25f, 659.25f, 783.99f, 1046.50f)
      for (n in notes) {
        generateTone(freq = n, durationMs = 60, volume = sfxVolume * 0.8f)
        delay(40)
      }
    }
  }

  fun playSpikeHurt() {
    if (!isSfxEnabled) return
    scope.launch {
      // Crunchy low pitch drop
      generateToneSweep(startFreq = 450f, endFreq = 90f, durationMs = 180, volume = sfxVolume * 0.85f, isNoise = true)
    }
  }

  fun playBreakBlock() {
    if (!isSfxEnabled) return
    scope.launch {
      // Heavy crunch
      generateToneSweep(startFreq = 180f, endFreq = 60f, durationMs = 140, volume = sfxVolume * 0.9f, isNoise = true)
    }
  }

  fun playVictory() {
    if (!isSfxEnabled) return
    scope.launch {
      val fanfare = listOf(
        Pair(523.25f, 100),
        Pair(659.25f, 100),
        Pair(783.99f, 100),
        Pair(1046.50f, 250),
        Pair(880.00f, 120),
        Pair(1046.50f, 400)
      )
      for ((f, d) in fanfare) {
        generateTone(freq = f, durationMs = d, volume = sfxVolume * 0.9f)
        delay(d.toLong() + 20)
      }
    }
  }

  fun playClick() {
    if (!isSfxEnabled) return
    scope.launch {
      generateTone(freq = 880f, durationMs = 30, volume = sfxVolume * 0.4f)
    }
  }

  private fun generateTone(freq: Float, durationMs: Int, volume: Float) {
    generateToneSweep(freq, freq, durationMs, volume, isNoise = false)
  }

  private fun generateToneSweep(
    startFreq: Float,
    endFreq: Float,
    durationMs: Int,
    volume: Float,
    isNoise: Boolean = false
  ) {
    try {
      val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(50)
      val buffer = ShortArray(numSamples)
      var currentFreq = startFreq
      var phase = 0.0

      for (i in 0 until numSamples) {
        val fraction = i.toDouble() / numSamples
        currentFreq = (startFreq + (endFreq - startFreq) * fraction).toFloat()
        val deltaPhase = 2.0 * PI * currentFreq / sampleRate
        phase += deltaPhase

        val rawSample = if (isNoise) {
          (sin(phase) * 0.6 + (Math.random() * 2.0 - 1.0) * 0.4)
        } else {
          // Retro sine + triangle overtone
          sin(phase) * 0.7 + sin(2.0 * phase) * 0.3
        }

        val envelope = (1.0 - fraction).coerceIn(0.0, 1.0)
        val sample = (rawSample * envelope * volume * Short.MAX_VALUE).toInt()
        buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
      }

      val track = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        )
        .setBufferSizeInBytes(buffer.size * 2)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

      track.write(buffer, 0, buffer.size)
      track.play()

      scope.launch {
        delay(durationMs.toLong() + 50)
        try {
          track.stop()
          track.release()
        } catch (_: Exception) {}
      }
    } catch (_: Exception) {}
  }
}
