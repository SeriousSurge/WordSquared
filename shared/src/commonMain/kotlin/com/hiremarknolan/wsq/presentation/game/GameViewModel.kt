package com.hiremarknolan.wsq.presentation.game

import androidx.lifecycle.viewModelScope
import com.hiremarknolan.wsq.domain.repository.GameStateData
import com.hiremarknolan.wsq.domain.repository.PuzzleTargetsData
import com.hiremarknolan.wsq.domain.repository.TileData
import com.hiremarknolan.wsq.domain.usecase.*
import com.hiremarknolan.wsq.game.GameGrid
import com.hiremarknolan.wsq.models.*
import com.hiremarknolan.wsq.mvi.MviViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import com.hiremarknolan.wsq.game.WordBoard
import com.hiremarknolan.wsq.PlatformSettings

/**
 * ViewModel for the game screen following MVI pattern
 */
class GameViewModel(
    private val loadTodaysPuzzleUseCase: LoadTodaysPuzzleUseCase,
    private val validateWordsUseCase: ValidateWordsUseCase,
    private val calculateScoreUseCase: CalculateScoreUseCase,
    private val saveGameStateUseCase: SaveGameStateUseCase,
    private val loadGameStateUseCase: LoadGameStateUseCase,
    private val getSavedElapsedTimeUseCase: GetSavedElapsedTimeUseCase,
    private val difficultyPreferencesUseCase: DifficultyPreferencesUseCase,
    private val getInitialDifficultyUseCase: GetInitialDifficultyUseCase,
    private val completeGameUseCase: CompleteGameUseCase,
    private val settings: com.russhwolf.settings.Settings
) : MviViewModel<GameContract.Intent, GameContract.State, GameContract.Effect>(
    GameContract.State()
) {

    private var gameBoard: com.hiremarknolan.wsq.game.WordBoard? = null
    private var startTime: Long = 0L

    init {
        initializeGame()
    }

    override fun handleIntent(intent: GameContract.Intent) {
        when (intent) {
            is GameContract.Intent.LoadGame -> { /* Game loads automatically in init */ }
            is GameContract.Intent.SubmitWord -> submitWord()
            is GameContract.Intent.ResetGame -> resetGame()
            is GameContract.Intent.ChangeDifficulty -> changeDifficulty(intent.difficulty)
            is GameContract.Intent.SelectPosition -> selectPosition(intent.row, intent.col)
            is GameContract.Intent.ClearSelection -> clearSelection()
            is GameContract.Intent.EnterLetter -> enterLetter(intent.letter)
            is GameContract.Intent.DeleteLetter -> deleteLetter()
            is GameContract.Intent.ShowTutorial -> updateState { copy(showTutorial = true) }
            is GameContract.Intent.HideTutorial -> updateState { copy(showTutorial = false) }
            is GameContract.Intent.ShowVictoryModal -> updateState { copy(showVictoryModal = true) }
            is GameContract.Intent.HideVictoryModal -> updateState { copy(showVictoryModal = false) }
            is GameContract.Intent.ShowInvalidWordsModal -> updateState { copy(showInvalidWordsModal = true) }
            is GameContract.Intent.HideInvalidWordsModal -> updateState { copy(showInvalidWordsModal = false) }
            is GameContract.Intent.ShowGuessesModal -> updateState { copy(showGuessesModal = true) }
            is GameContract.Intent.HideGuessesModal -> updateState { copy(showGuessesModal = false) }
            is GameContract.Intent.ShowHamburgerMenu -> updateState { copy(showHamburgerMenu = true) }
            is GameContract.Intent.HideHamburgerMenu -> updateState { copy(showHamburgerMenu = false) }
            is GameContract.Intent.UpdateElapsedTime -> updateState { copy(elapsedTime = intent.elapsedTime) }
            is GameContract.Intent.ClearErrors -> clearErrors()
        }
    }

    private fun initializeGame() {
        viewModelScope.launch {
            try {
                val initialDifficulty = getInitialDifficultyUseCase()
                updateState { 
                    copy(
                        difficulty = initialDifficulty,
                        currentGridSize = initialDifficulty.gridSize,
                        isLoading = true
                    )
                }
                
                // Create WordBoard with the initial difficulty
                gameBoard = WordBoard(settings, initialDifficulty.gridSize)
                
                // Initial state sync
                syncGameBoardState()
                
            } catch (e: Exception) {
                updateState { 
                    copy(
                        errorMessage = "Failed to initialize game: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }



    private fun submitWord() {
        viewModelScope.launch {
            try {
                gameBoard?.submitWord()
                syncGameBoardState()
                
                // Check if game was completed
                gameBoard?.let { board ->
                    if (board.isGameWon) {
                        sendEffect(GameContract.Effect.GameCompleted)
                        updateState { copy(showVictoryModal = true) }
                    } else if (board.invalidWords.isNotEmpty()) {
                        updateState { copy(showInvalidWordsModal = true) }
                    }
                }
            } catch (e: Exception) {
                updateState { 
                    copy(
                        errorMessage = "Submission failed: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun resetGame() {
        viewModelScope.launch {
            gameBoard?.newGame()
            syncGameBoardState()
        }
    }

    private fun syncGameBoardState() {
        gameBoard?.let { board ->
            updateState { 
                copy(
                    tiles = board.tiles,
                    selectedPosition = board.selectedPosition,
                    score = board.score,
                    isGameOver = board.isGameOver,
                    isGameWon = board.isGameWon,
                    guessCount = board.guessCount,
                    errorMessage = board.errorMessage,
                    invalidWords = board.invalidWords,
                    hasNetworkError = board.hasNetworkError,
                    previousGuesses = board.previousGuesses,
                    isLoading = board.isLoading,
                    currentPuzzleDate = board.currentPuzzleDate,
                    completionTime = board.completionTime,
                    difficulty = board.difficulty,
                    currentGridSize = board.currentGridSize
                )
            }
        }
    }

    private fun changeDifficulty(newDifficulty: Difficulty) {
        viewModelScope.launch {
            updateState { 
                copy(
                    difficulty = newDifficulty,
                    currentGridSize = newDifficulty.gridSize,
                    isLoading = true
                )
            }
            
            difficultyPreferencesUseCase.setLastUsedDifficulty(newDifficulty)
            gameBoard?.changeDifficulty(newDifficulty)
            
            // Sync state after difficulty change
            syncGameBoardState()
        }
    }

    private fun selectPosition(row: Int, col: Int) {
        gameBoard?.selectPosition(row, col)
        syncGameBoardState()
        sendEffect(GameContract.Effect.RequestFocus(row, col))
    }

    private fun clearSelection() {
        gameBoard?.clearSelection()
        syncGameBoardState()
    }

    private fun enterLetter(letter: Char) {
        gameBoard?.enterLetter(letter)
        syncGameBoardState()
        sendEffect(GameContract.Effect.SaveGameState)
    }

    private fun deleteLetter() {
        gameBoard?.deleteLetter()
        syncGameBoardState()
        sendEffect(GameContract.Effect.SaveGameState)
    }

    private fun clearErrors() {
        updateState { 
            copy(
                errorMessage = null,
                invalidWords = emptyList(),
                hasNetworkError = false
            )
        }
    }
}

 