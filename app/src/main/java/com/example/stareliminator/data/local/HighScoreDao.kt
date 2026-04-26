package com.example.stareliminator.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HighScoreDao {
    @Insert
    suspend fun insert(score: HighScoreEntity)

    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT 20")
    fun getTopScores(): Flow<List<HighScoreEntity>>

    @Query("SELECT MAX(score) FROM high_scores")
    suspend fun getHighestScore(): Int?
}
