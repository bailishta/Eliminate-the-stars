package com.example.stareliminator.data.repository

import com.example.stareliminator.data.local.GameStateDao
import com.example.stareliminator.data.local.HighScoreDao
import com.example.stareliminator.data.local.HighScoreEntity
import com.example.stareliminator.data.local.SavedGameEntity
import com.example.stareliminator.util.GridSerializer
import kotlinx.coroutines.flow.Flow

class GameRepository(
    private val gameStateDao: GameStateDao,
    private val highScoreDao: HighScoreDao
) {
    suspend fun saveGame(
        grid: Array<IntArray>,
        score: Int,
        starsEliminated: Int = 0,
        isGameOver: Boolean = false
    ) {
        gameStateDao.saveGame(
            SavedGameEntity(
                gridJson = GridSerializer.toJson(grid),
                score = score,
                starsEliminated = starsEliminated,
                isGameOver = isGameOver
            )
        )
    }

    suspend fun loadGame(): SavedGameEntity? = gameStateDao.loadGame()

    suspend fun clearSavedGame() = gameStateDao.clearGame()

    suspend fun saveHighScore(score: Int, eliminated: Int, boardCleared: Boolean) {
        highScoreDao.insert(
            HighScoreEntity(
                score = score,
                starsEliminated = eliminated,
                boardCleared = boardCleared
            )
        )
    }

    fun getTopScores(): Flow<List<HighScoreEntity>> = highScoreDao.getTopScores()

    suspend fun getHighestScore(): Int? = highScoreDao.getHighestScore()
}
