package com.hiremarknolan.wsq.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Base interface for MVI Intents
 */
interface MviIntent

/**
 * Base interface for MVI States
 */
interface MviState

/**
 * Base interface for MVI Effects (one-time events)
 */
interface MviEffect

/**
 * Abstract base ViewModel for MVI pattern
 */
abstract class MviViewModel<Intent : MviIntent, State : MviState, Effect : MviEffect>(
    initialState: State
) : ViewModel() {

    // Private mutable state
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    // Private effects channel
    private val _effects = Channel<Effect>(Channel.BUFFERED)
    val effects: Flow<Effect> = _effects.receiveAsFlow()

    // Current state accessor
    protected val currentState: State
        get() = _state.value

    /**
     * Updates the state
     */
    protected fun updateState(newState: State) {
        _state.value = newState
    }

    /**
     * Updates the state using a reducer function
     */
    protected fun updateState(reducer: State.() -> State) {
        _state.value = currentState.reducer()
    }

    /**
     * Sends a one-time effect
     */
    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch {
            _effects.send(effect)
        }
    }

    /**
     * Handles intents
     */
    abstract fun handleIntent(intent: Intent)

    /**
     * Processes an intent
     */
    fun processIntent(intent: Intent) {
        handleIntent(intent)
    }
} 