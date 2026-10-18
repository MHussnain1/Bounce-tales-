package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class ScoreRecord(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val playerName: String,
  val levelNumber: Int,
  val levelName: String,
  val score: Int,
  val ringsCollected: Int,
  val totalRings: Int,
  val timeSeconds: Int,
  val stars: Int,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "level_progress")
data class LevelProgress(
  @PrimaryKey
  val levelNumber: Int,
  val isUnlocked: Boolean = false,
  val starsEarned: Int = 0,
  val bestScore: Int = 0,
  val bestTimeSeconds: Int = 0
)
