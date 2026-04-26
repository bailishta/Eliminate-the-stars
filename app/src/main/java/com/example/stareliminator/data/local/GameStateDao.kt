package com.example.stareliminator.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GameStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGame(game: SavedGameEntity)

    @Query("SELECT * FROM saved_game WHERE id = 1")
    suspend fun loadGame(): SavedGameEntity?

    @Query("DELETE FROM saved_game WHERE id = 1")
    suspend fun clearGame()
}
