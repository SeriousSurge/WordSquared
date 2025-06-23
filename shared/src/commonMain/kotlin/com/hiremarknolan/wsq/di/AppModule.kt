package com.hiremarknolan.wsq.di

import com.hiremarknolan.wsq.data.repository.GameRepositoryImpl
import com.hiremarknolan.wsq.data.repository.GamePersistenceRepositoryImpl
import com.hiremarknolan.wsq.domain.repository.GameRepository
import com.hiremarknolan.wsq.domain.repository.GamePersistenceRepository
import com.hiremarknolan.wsq.domain.usecase.*
import com.hiremarknolan.wsq.presentation.game.GameViewModel
import com.hiremarknolan.wsq.network.WordSquareApiClient
import org.koin.dsl.module

/**
 * Main app module containing all dependencies
 */
val appModule = module {
    
    // Network
    single { WordSquareApiClient(get()) }
    
    // Repositories
    single<GameRepository> { GameRepositoryImpl(get()) }
    single<GamePersistenceRepository> { GamePersistenceRepositoryImpl(get()) }
    
    // Use Cases
    single { LoadTodaysPuzzleUseCase(get(), get()) }
    single { ValidateWordsUseCase(get()) }
    single { SubmitWordUseCase(get()) }
    single { CalculateScoreUseCase() }
    single { SaveGameStateUseCase(get()) }
    single { LoadGameStateUseCase(get()) }
    single { GetSavedElapsedTimeUseCase(get()) }
    single { DifficultyPreferencesUseCase(get()) }
    single { GetInitialDifficultyUseCase(get()) }
    single { CompleteGameUseCase(get(), get()) }
    
    // ViewModels - using single instead of viewModel for cross-platform compatibility
    single {
        GameViewModel(
            loadTodaysPuzzleUseCase = get(),
            validateWordsUseCase = get(),
            submitWordUseCase = get(),
            calculateScoreUseCase = get(),
            saveGameStateUseCase = get(),
            loadGameStateUseCase = get(),
            getSavedElapsedTimeUseCase = get(),
            difficultyPreferencesUseCase = get(),
            getInitialDifficultyUseCase = get(),
            completeGameUseCase = get()
        )
    }
}

 