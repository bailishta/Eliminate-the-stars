package com.example.stareliminator.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Color
import com.example.stareliminator.audio.SoundManager
import com.example.stareliminator.data.local.HighScoreEntity
import com.example.stareliminator.data.model.Cell
import com.example.stareliminator.data.repository.GameRepository
import com.example.stareliminator.domain.BoardGenerator
import com.example.stareliminator.domain.CellMove
import com.example.stareliminator.domain.FloodFill
import com.example.stareliminator.domain.GameEngine
import com.example.stareliminator.domain.MoveResult
import com.example.stareliminator.util.COLLAPSE_ANIM_DURATION_MS
import com.example.stareliminator.util.COMBO_MULTIPLIER_INCREMENT
import com.example.stareliminator.util.EMPTY
import com.example.stareliminator.util.GRAVITY_ANIM_DURATION_MS
import com.example.stareliminator.util.GRID_COLS
import com.example.stareliminator.util.GRID_ROWS
import com.example.stareliminator.util.MAX_COMBO_MULTIPLIER
import com.example.stareliminator.util.STAR_COLORS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class AnimationPhase { IDLE, GRAVITY, COLLAPSE }

data class ScorePopup(
    val id: Long,
    val score: Int,
    val centerX: Float,
    val centerY: Float,
    val startTimeMillis: Long
)

data class EliminationParticle(
    val id: Long,
    val startX: Float,
    val startY: Float,
    val color: Color,
    val angle: Float,
    val speed: Float,
    val startTimeMillis: Long
)

data class GameUiState(
    val grid: Array<IntArray> = Array(GRID_ROWS) { IntArray(GRID_COLS) },
    val score: Int = 0,
    val bonus: Int = 0,
    val isGameOver: Boolean = false,
    val isBoardCleared: Boolean = false,
    val selectedGroup: Set<Cell>? = null,
    val lastEliminatedCount: Int = 0,
    val hasSavedGame: Boolean = false,
    val highestScore: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    // Combo
    val comboCount: Int = 0,
    val comboMultiplier: Float = 1.0f,
    val showCombo: Boolean = false,
    // Animation
    val isAnimating: Boolean = false,
    val animationPhase: AnimationPhase = AnimationPhase.IDLE,
    val animationProgress: Float = 0f,
    val gravityMoves: List<CellMove> = emptyList(),
    val collapseMoves: List<CellMove> = emptyList(),
    val preAnimationGrid: Array<IntArray>? = null,
    val postGravityGrid: Array<IntArray>? = null,
    // Level system
    val currentLevel: Int = 1,
    val levelTargetScore: Int = 300,
    val showLevelComplete: Boolean = false,
    // Score popups
    val scorePopups: List<ScorePopup> = emptyList(),
    // Two-tap system
    val lockedGroup: Set<Cell>? = null,
    // Elimination particles
    val eliminationParticles: List<EliminationParticle> = emptyList()
)

class GameViewModel(
    private val repository: GameRepository,
    private val soundManager: SoundManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var starsEliminatedTotal = 0
    private var comboCount = 0

    init {
        viewModelScope.launch {
            val savedGame = repository.loadGame()
            val highestScore = repository.getHighestScore() ?: 0
            _uiState.update {
                it.copy(
                    hasSavedGame = savedGame != null && !savedGame.isGameOver,
                    highestScore = highestScore,
                    isLoading = false
                )
            }
        }
    }

    fun newGame() {
        starsEliminatedTotal = 0
        comboCount = 0
        val grid = BoardGenerator.generateWithValidMoves()
        _uiState.update {
            GameUiState(
                grid = grid,
                score = 0,
                currentLevel = 1,
                levelTargetScore = 450,
                isLoading = false,
                lockedGroup = null,
                eliminationParticles = emptyList()
            )
        }
    }

    fun tapCell(row: Int, col: Int, tapCenterX: Float = 0f, tapCenterY: Float = 0f) {
        val current = _uiState.value
        if (current.isGameOver || current.isLoading || current.isAnimating) return

        val lockedGroup = current.lockedGroup
        val tappedCell = Cell(row, col, current.grid[row][col])

        if (lockedGroup != null) {
            if (tappedCell in lockedGroup) {
                executeElimination(current, lockedGroup, tapCenterX, tapCenterY)
                _uiState.update { it.copy(lockedGroup = null, selectedGroup = null) }
            } else {
                val newGroup = FloodFill.findConnectedGroup(current.grid, row, col)
                if (newGroup.size >= 2) {
                    _uiState.update { it.copy(lockedGroup = newGroup, selectedGroup = newGroup) }
                } else {
                    soundManager.play(SoundManager.SoundType.INVALID_TAP)
                    _uiState.update { it.copy(lockedGroup = null, selectedGroup = null) }
                }
            }
        } else {
            val group = FloodFill.findConnectedGroup(current.grid, row, col)
            if (group.size >= 2) {
                _uiState.update { it.copy(lockedGroup = group, selectedGroup = group) }
            } else {
                soundManager.play(SoundManager.SoundType.INVALID_TAP)
            }
        }
    }

    private fun executeElimination(
        current: GameUiState,
        group: Set<Cell>,
        tapCenterX: Float,
        tapCenterY: Float
    ) {
        val firstCell = group.first()
        val result = GameEngine.processTapWithAnimation(current.grid, firstCell.row, firstCell.col)

        when (result) {
            is MoveResult.AnimatedSuccessful -> {
                starsEliminatedTotal += result.eliminatedGroup.size
                comboCount++
                val multiplier = (1.0f + (comboCount - 1) * COMBO_MULTIPLIER_INCREMENT)
                    .coerceAtMost(MAX_COMBO_MULTIPLIER)
                val comboBonus = (result.scoreGained * (multiplier - 1.0f)).toInt()
                val totalGained = result.scoreGained + comboBonus + result.bonusGained
                val showCombo = comboCount >= 2

                if (showCombo) {
                    soundManager.play(SoundManager.SoundType.COMBO)
                } else {
                    soundManager.play(SoundManager.SoundType.ELIMINATE)
                }
                if (result.isBoardCleared) {
                    soundManager.play(SoundManager.SoundType.BOARD_CLEAR)
                }

                val newPopup = ScorePopup(
                    id = System.currentTimeMillis(),
                    score = totalGained,
                    centerX = tapCenterX,
                    centerY = tapCenterY,
                    startTimeMillis = System.currentTimeMillis()
                )

                // Generate elimination particles
                val particles = generateEliminationParticles(result.eliminatedGroup)
                val combinedParticles = _uiState.value.eliminationParticles + particles

                _uiState.update {
                    val gridWithHoles = Array(GRID_ROWS) { r -> current.grid[r].copyOf() }
                    for (cell in result.eliminatedGroup) {
                        gridWithHoles[cell.row][cell.col] = EMPTY
                    }
                    it.copy(
                        isAnimating = true,
                        animationPhase = AnimationPhase.GRAVITY,
                        animationProgress = 0f,
                        gravityMoves = result.gravityMoves,
                        collapseMoves = result.collapseMoves,
                        preAnimationGrid = gridWithHoles,
                        postGravityGrid = result.gravityGrid,
                        score = it.score + totalGained,
                        bonus = result.bonusGained,
                        lastEliminatedCount = result.eliminatedGroup.size,
                        selectedGroup = null,
                        comboCount = comboCount,
                        comboMultiplier = multiplier,
                        showCombo = showCombo,
                        scorePopups = it.scorePopups + newPopup,
                        eliminationParticles = combinedParticles
                    )
                }

                viewModelScope.launch {
                    launch {
                        kotlinx.coroutines.delay(600)
                        _uiState.update { state ->
                            state.copy(eliminationParticles = state.eliminationParticles.filter { p ->
                                particles.none { it.id == p.id }
                            })
                        }
                    }

                    launch {
                        kotlinx.coroutines.delay(1000)
                        _uiState.update { state ->
                            state.copy(scorePopups = state.scorePopups.filter { it.id != newPopup.id })
                        }
                    }

                    runAnimation(GRAVITY_ANIM_DURATION_MS) { progress ->
                        _uiState.update { it.copy(animationProgress = progress) }
                    }

                    _uiState.update {
                        it.copy(animationPhase = AnimationPhase.COLLAPSE, animationProgress = 0f)
                    }

                    runAnimation(COLLAPSE_ANIM_DURATION_MS) { progress ->
                        _uiState.update { it.copy(animationProgress = progress) }
                    }

                    val newScore = _uiState.value.score
                    if (result.isGameOver) {
                        val levelTarget = _uiState.value.levelTargetScore
                        if (newScore >= levelTarget) {
                            soundManager.play(SoundManager.SoundType.LEVEL_COMPLETE)
                            _uiState.update {
                                it.copy(
                                    grid = result.finalGrid,
                                    isBoardCleared = result.isBoardCleared,
                                    isAnimating = false,
                                    animationPhase = AnimationPhase.IDLE,
                                    preAnimationGrid = null,
                                    postGravityGrid = null,
                                    gravityMoves = emptyList(),
                                    collapseMoves = emptyList(),
                                    showLevelComplete = true
                                )
                            }
                            launch {
                                kotlinx.coroutines.delay(2500)
                                nextLevel()
                            }
                        } else {
                            soundManager.play(SoundManager.SoundType.GAME_OVER)
                            _uiState.update {
                                it.copy(
                                    grid = result.finalGrid,
                                    isGameOver = true,
                                    isBoardCleared = result.isBoardCleared,
                                    isAnimating = false,
                                    animationPhase = AnimationPhase.IDLE,
                                    preAnimationGrid = null,
                                    postGravityGrid = null,
                                    gravityMoves = emptyList(),
                                    collapseMoves = emptyList()
                                )
                            }
                            endGame()
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                grid = result.finalGrid,
                                isBoardCleared = result.isBoardCleared,
                                isAnimating = false,
                                animationPhase = AnimationPhase.IDLE,
                                preAnimationGrid = null,
                                postGravityGrid = null,
                                gravityMoves = emptyList(),
                                collapseMoves = emptyList()
                            )
                        }
                    }
                }
            }

            else -> {}
        }
    }

    private fun generateEliminationParticles(group: Set<Cell>): List<EliminationParticle> {
        val now = System.currentTimeMillis()
        val particles = mutableListOf<EliminationParticle>()
        for (cell in group) {
            val color = STAR_COLORS[cell.color] ?: Color.Gray
            val count = 4 + Random.nextInt(4)
            for (i in 0 until count) {
                val angle = (2.0 * Math.PI * i / count) + Random.nextDouble(-0.3, 0.3)
                particles.add(
                    EliminationParticle(
                        id = now + cell.row * 100 + cell.col + i,
                        startX = cell.col.toFloat(),
                        startY = cell.row.toFloat(),
                        color = color,
                        angle = angle.toFloat(),
                        speed = 3f + Random.nextFloat() * 5f,
                        startTimeMillis = now
                    )
                )
            }
        }
        return particles
    }

    fun previewGroup(row: Int, col: Int) {
        val current = _uiState.value
        if (current.isGameOver || current.isAnimating) return

        val group = FloodFill.findConnectedGroup(current.grid, row, col)
        _uiState.update {
            it.copy(selectedGroup = if (group.size >= 2) group else null)
        }
    }

    fun clearPreview() {
        val current = _uiState.value
        // Don't clear the locked group highlight
        _uiState.update { it.copy(selectedGroup = current.lockedGroup) }
    }

    fun nextLevel() {
        val current = _uiState.value
        val newLevel = current.currentLevel + 1
        val newTarget = 700 + newLevel * newLevel * 400
        val newGrid = BoardGenerator.generateWithValidMoves()
        _uiState.update {
            it.copy(
                grid = newGrid,
                currentLevel = newLevel,
                levelTargetScore = newTarget,
                showLevelComplete = false,
                isGameOver = false,
                isBoardCleared = false,
                selectedGroup = null,
                lockedGroup = null,
                eliminationParticles = emptyList()
            )
        }
    }

    fun saveGame() {
        val current = _uiState.value
        if (current.isGameOver) return
        viewModelScope.launch {
            repository.saveGame(
                grid = current.grid,
                score = current.score,
                starsEliminated = starsEliminatedTotal
            )
            _uiState.update { it.copy(hasSavedGame = true) }
        }
    }

    fun resumeGame() {
        viewModelScope.launch {
            val savedGame = repository.loadGame()
            if (savedGame != null) {
                val grid = com.example.stareliminator.util.GridSerializer.fromJson(savedGame.gridJson)
                starsEliminatedTotal = savedGame.starsEliminated
                comboCount = 0
                _uiState.update {
                    it.copy(
                        grid = grid,
                        score = savedGame.score,
                        isGameOver = false,
                        isLoading = false,
                        hasSavedGame = false,
                        lockedGroup = null,
                        eliminationParticles = emptyList()
                    )
                }
            }
        }
    }

    fun loadHighScores(block: (List<HighScoreEntity>) -> Unit) {
        viewModelScope.launch {
            repository.getTopScores().collect { scores ->
                block(scores)
            }
        }
    }

    private fun endGame() {
        viewModelScope.launch {
            val finalScore = _uiState.value.score
            repository.saveHighScore(
                score = finalScore,
                eliminated = starsEliminatedTotal,
                boardCleared = _uiState.value.isBoardCleared
            )
            repository.clearSavedGame()
            _uiState.update {
                it.copy(
                    hasSavedGame = false,
                    highestScore = maxOf(it.highestScore, finalScore)
                )
            }
        }
    }

    private suspend fun runAnimation(durationMs: Int, onProgress: (Float) -> Unit) {
        val frameMs = 16L
        val startTime = System.currentTimeMillis()
        val duration = durationMs.toLong()
        while (true) {
            val elapsed = System.currentTimeMillis() - startTime
            val raw = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
            val eased = fastOutSlowIn(raw)
            onProgress(eased)
            if (elapsed >= duration) break
            kotlinx.coroutines.delay(frameMs)
        }
    }

    private fun fastOutSlowIn(t: Float): Float = if (t < 0.5f) {
        4f * t * t * t
    } else {
        val f = -2f * t + 2f
        1f - f * f * f / 2f
    }

    class Factory(
        private val repository: GameRepository,
        private val soundManager: SoundManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                return GameViewModel(repository, soundManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
