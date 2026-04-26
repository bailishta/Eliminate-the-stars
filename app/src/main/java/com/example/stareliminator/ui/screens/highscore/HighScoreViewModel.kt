package com.example.stareliminator.ui.screens.highscore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stareliminator.data.local.HighScoreEntity
import com.example.stareliminator.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HighScoreUiState(
    val scores: List<HighScoreEntity> = emptyList(),
    val isLoading: Boolean = true
)

class HighScoreViewModel(
    private val repository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HighScoreUiState())
    val uiState: StateFlow<HighScoreUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getTopScores().collect { scores ->
                _uiState.update { it.copy(scores = scores, isLoading = false) }
            }
        }
    }

    class Factory(private val repository: GameRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HighScoreViewModel::class.java)) {
                return HighScoreViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
