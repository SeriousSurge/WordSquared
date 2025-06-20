package com.hiremarknolan.wsq.mvi

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

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
 * Uses custom coroutine scope for cross-platform compatibility
 */
abstract class MviViewModel<Intent : MviIntent, State : MviState, Effect : MviEffect>(
    initialState: State
) {

        // Create a custom scope for cross-platform compatibility
    private val viewModelJob = SupervisorJob()
    protected val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    
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
     * Handles intents (suspend function for async operations)
     */
    protected abstract suspend fun handleIntent(intent: Intent)

    /**
     * Processes an intent
     */
    fun processIntent(intent: Intent) {
        viewModelScope.launch {
            handleIntent(intent)
        }
    }
    
    /**
     * Cleanup resources when done
     * Call this when the ViewModel is no longer needed
     */
    fun dispose() {
        viewModelJob.cancel()
    }
} 