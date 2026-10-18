package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ScoreRecord::class, LevelProgress::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
  abstract fun scoreDao(): ScoreDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "bounce_tales.db"
        )
          .addCallback(object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
              super.onCreate(db)
              CoroutineScope(Dispatchers.IO).launch {
                val dao = getDatabase(context).scoreDao()
                // Initial Level Unlock: Level 1 is always unlocked
                dao.upsertLevelProgress(LevelProgress(levelNumber = 1, isUnlocked = true, starsEarned = 0))
                dao.upsertLevelProgress(LevelProgress(levelNumber = 2, isUnlocked = false, starsEarned = 0))
                dao.upsertLevelProgress(LevelProgress(levelNumber = 3, isUnlocked = false, starsEarned = 0))
                dao.upsertLevelProgress(LevelProgress(levelNumber = 4, isUnlocked = false, starsEarned = 0))
                dao.upsertLevelProgress(LevelProgress(levelNumber = 5, isUnlocked = false, starsEarned = 0))

                // Initial nostalgic high score entries to compete against
                val sampleScores = listOf(
                  ScoreRecord(playerName = "NOK", levelNumber = 1, levelName = "Emerald Meadow", score = 4850, ringsCollected = 5, totalRings = 5, timeSeconds = 42, stars = 3),
                  ScoreRecord(playerName = "ACE", levelNumber = 1, levelName = "Emerald Meadow", score = 4100, ringsCollected = 5, totalRings = 5, timeSeconds = 55, stars = 2),
                  ScoreRecord(playerName = "RED", levelNumber = 2, levelName = "Aquatic Caverns", score = 6200, ringsCollected = 6, totalRings = 6, timeSeconds = 68, stars = 3),
                  ScoreRecord(playerName = "BOB", levelNumber = 2, levelName = "Aquatic Caverns", score = 5100, ringsCollected = 5, totalRings = 6, timeSeconds = 85, stars = 2),
                  ScoreRecord(playerName = "MAX", levelNumber = 3, levelName = "Granite Fortress", score = 7400, ringsCollected = 7, totalRings = 7, timeSeconds = 92, stars = 3),
                  ScoreRecord(playerName = "NEO", levelNumber = 4, levelName = "Skyward Spire", score = 8900, ringsCollected = 8, totalRings = 8, timeSeconds = 110, stars = 3)
                )
                sampleScores.forEach { dao.insertScore(it) }
              }
            }
          })
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
