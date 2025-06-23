package com.hiremarknolan.wsq.presentation.game

import com.hiremarknolan.wsq.mvi.MviEffect
import com.hiremarknolan.wsq.mvi.MviIntent
import com.hiremarknolan.wsq.mvi.MviState
import com.hiremarknolan.wsq.models.Difficulty
import com.hiremarknolan.wsq.models.InvalidWord
import com.hiremarknolan.wsq.models.Tile

/**
 * Game screen MVI contract
 */
class GameContract {
    
    /**
     * Game screen state
     */
    data class State(
        val difficulty: Difficulty = Difficulty.NORMAL,
        val currentGridSize: Int = 4,
        val tiles: Array<Array<Tile>> = emptyArray(),
        val selectedPosition: Pair<Int, Int>? = null,
        val score: Int = 0,
        val isGameOver: Boolean = false,
        val isGameWon: Boolean = false,
        val guessCount: Int = 0,
        val errorMessage: String? = null,
        val invalidWords: List<InvalidWord> = emptyList(),
        val hasNetworkError: Boolean = false,
        val previousGuesses: List<String> = emptyList(),
        val isLoading: Boolean = false,
        val currentPuzzleDate: String = "",
        val completionTime: Long = 0L,
        val elapsedTime: Long = 0L,
        val showTutorial: Boolean = false,
        val showVictoryModal: Boolean = false,
        val showInvalidWordsModal: Boolean = false,
        val showGuessesModal: Boolean = false,
        val showHamburgerMenu: Boolean = false,
        val targetWords: Map<String, String> = emptyMap()
    ) : MviState {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as State

            if (difficulty != other.difficulty) return false
            if (currentGridSize != other.currentGridSize) return false
            if (!tiles.contentDeepEquals(other.tiles)) return false
            if (selectedPosition != other.selectedPosition) return false
            if (score != other.score) return false
            if (isGameOver != other.isGameOver) return false
            if (isGameWon != other.isGameWon) return false
            if (guessCount != other.guessCount) return false
            if (errorMessage != other.errorMessage) return false
            if (invalidWords != other.invalidWords) return false
            if (hasNetworkError != other.hasNetworkError) return false
            if (previousGuesses != other.previousGuesses) return false
            if (isLoading != other.isLoading) return false
            if (currentPuzzleDate != other.currentPuzzleDate) return false
            if (completionTime != other.completionTime) return false
            if (elapsedTime != other.elapsedTime) return false
            if (showTutorial != other.showTutorial) return false
            if (showVictoryModal != other.showVictoryModal) return false
            if (showInvalidWordsModal != other.showInvalidWordsModal) return false
            if (showGuessesModal != other.showGuessesModal) return false
            if (showHamburgerMenu != other.showHamburgerMenu) return false
            if (targetWords != other.targetWords) return false

            return true
        }

        override fun hashCode(): Int {
            var result = difficulty.hashCode()
            result = 31 * result + currentGridSize
            result = 31 * result + tiles.contentDeepHashCode()
            result = 31 * result + (selectedPosition?.hashCode() ?: 0)
            result = 31 * result + score
            result = 31 * result + isGameOver.hashCode()
            result = 31 * result + isGameWon.hashCode()
            result = 31 * result + guessCount
            result = 31 * result + (errorMessage?.hashCode() ?: 0)
            result = 31 * result + invalidWords.hashCode()
            result = 31 * result + hasNetworkError.hashCode()
            result = 31 * result + previousGuesses.hashCode()
            result = 31 * result + isLoading.hashCode()
            result = 31 * result + currentPuzzleDate.hashCode()
            result = 31 * result + completionTime.hashCode()
            result = 31 * result + elapsedTime.hashCode()
            result = 31 * result + showTutorial.hashCode()
            result = 31 * result + showVictoryModal.hashCode()
            result = 31 * result + showInvalidWordsModal.hashCode()
            result = 31 * result + showGuessesModal.hashCode()
            result = 31 * result + showHamburgerMenu.hashCode()
            result = 31 * result + targetWords.hashCode()
            return result
        }
    }
    
    /**
     * Game screen intents
     */
    sealed class Intent : MviIntent {
        // Game actions
        object LoadGame : Intent()
        object SubmitWord : Intent()
        object ResetGame : Intent()
        data class ChangeDifficulty(val difficulty: Difficulty) : Intent()
        data class SelectPosition(val row: Int, val col: Int) : Intent()
        object ClearSelection : Intent()
        data class EnterLetter(val letter: Char) : Intent()
        object DeleteLetter : Intent()
        
        // UI actions
        object ShowTutorial : Intent()
        object HideTutorial : Intent()
        object ShowVictoryModal : Intent()
        object HideVictoryModal : Intent()
        object ShowInvalidWordsModal : Intent()
        object HideInvalidWordsModal : Intent()
        object ShowGuessesModal : Intent()
        object HideGuessesModal : Intent()
        object ShowHamburgerMenu : Intent()
        object HideHamburgerMenu : Intent()
        
        // Time tracking
        data class UpdateElapsedTime(val elapsedTime: Long) : Intent()
        
        // Error handling
        object ClearErrors : Intent()
    }
    
    /**
     * Game screen effects (one-time events)
     */
    sealed class Effect : MviEffect {
        data class ShowError(val message: String) : Effect()
        data class ShowSuccess(val message: String) : Effect()
        object GameCompleted : Effect()
        object NavigateToNextDifficulty : Effect()
        data class RequestFocus(val row: Int, val col: Int) : Effect()
        object SaveGameState : Effect()
        object VibrateFeedback : Effect()
    }
} 