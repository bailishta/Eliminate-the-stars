package com.example.stareliminator.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stareliminator.StarEliminatorApplication
import com.example.stareliminator.data.repository.GameRepository
import com.example.stareliminator.ui.screens.game.GameScreen
import com.example.stareliminator.ui.screens.game.GameViewModel
import com.example.stareliminator.ui.screens.highscore.HighScoreScreen
import com.example.stareliminator.ui.screens.highscore.HighScoreViewModel
import com.example.stareliminator.ui.screens.menu.MenuScreen

object Routes {
    const val MENU = "menu"
    const val GAME = "game"
    const val HIGH_SCORES = "highscores"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as StarEliminatorApplication
    val database = app.database

    val gameRepository = GameRepository(
        gameStateDao = database.gameStateDao(),
        highScoreDao = database.highScoreDao()
    )

    val soundManager = app.soundManager

    val gameViewModel: GameViewModel = viewModel(
        factory = GameViewModel.Factory(gameRepository, soundManager)
    )

    val highScoreViewModel: HighScoreViewModel = viewModel(
        factory = HighScoreViewModel.Factory(gameRepository)
    )

    NavHost(navController = navController, startDestination = Routes.MENU) {
        composable(Routes.MENU) {
            val menuState = gameViewModel.uiState.collectAsState()
            MenuScreen(
                hasSavedGame = menuState.value.hasSavedGame,
                highestScore = menuState.value.highestScore,
                onNewGame = {
                    gameViewModel.newGame()
                    navController.navigate(Routes.GAME)
                },
                onContinue = {
                    gameViewModel.resumeGame()
                    navController.navigate(Routes.GAME)
                },
                onHighScores = {
                    navController.navigate(Routes.HIGH_SCORES)
                }
            )
        }

        composable(Routes.GAME) {
            GameScreen(
                viewModel = gameViewModel,
                onBackToMenu = {
                    navController.popBackStack(Routes.MENU, false)
                }
            )
        }

        composable(Routes.HIGH_SCORES) {
            HighScoreScreen(
                viewModel = highScoreViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
