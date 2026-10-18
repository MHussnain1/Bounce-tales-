package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
  @Query("SELECT * FROM scores ORDER BY score DESC LIMIT 100")
  fun getAllHighScores(): Flow<List<ScoreRecord>>

  @Query("SELECT * FROM scores WHERE levelNumber = :level ORDER BY score DESC LIMIT 50")
  fun getScoresForLevel(level: Int): Flow<List<ScoreRecord>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertScore(score: ScoreRecord): Long

  @Query("SELECT * FROM level_progress ORDER BY levelNumber ASC")
  fun getAllLevelProgress(): Flow<List<LevelProgress>>

  @Query("SELECT * FROM level_progress WHERE levelNumber = :level LIMIT 1")
  suspend fun getProgressForLevel(level: Int): LevelProgress?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertLevelProgress(progress: LevelProgress)
}
