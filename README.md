---

# Art KMP Test App

## Overview

The Art app KMP Demo is a Kotlin Multiplatform (KMP) project designed to showcase the capabilities of modern cross-platform development. This app fetches data from an API, saves it locally, provides search functionality, and allows users to view detailed information about selected Exhibits.

## Features

- **Fetch Data from API**: Retrieve Collection and  data from a remote API.
- **Local Storage**: Save fetched data locally for offline access.
- **View Details**: Detailed information about each Exhibit.

## Screenshots

### List Screen

| Light Theme                                             | Dark Theme                                            | iOS                                                 |
|---------------------------------------------------------|-------------------------------------------------------|-----------------------------------------------------|
| ![List Screen Light](screenshots/list_android_light.png) | ![List Screen Dark](screenshots/list_android_dark.png) | ![List Screen iOS](screenshots/list_ios.png) |

### Details Screen

| Light Theme                                                  | Dark Theme                                                 | iOS                                                      |
|--------------------------------------------------------------|------------------------------------------------------------|----------------------------------------------------------|
| ![Details Screen Light](screenshots/detail_android_light.png) | ![Details Screen Dark](screenshots/detail_android_dark.png) | ![Details Screen iOS](screenshots/detail_ios.png) |

## Technologies and Libraries

- **Kotlin Multiplatform**: Share code between Android and iOS.
- **Compose Multiplatform**: Build native UI for multiple platforms.
- **Modularization**: Organized codebase into modules for better scalability and maintainability.

- **Compose Navigation**: To handle the Nav Tree.
- **Ktor**: Networking library for API calls.
- **Room**: Local database for storing data.
- **Coil**: Library for image loading and caching.
- **Koin DI**: Dependency injection for managing dependencies.
- **Napier**: For cross-platform logging.

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio)
- [Xcode](https://developer.apple.com/xcode/) (for iOS development)
- [Kotlin Multiplatform Mobile Plugin](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html#install-the-kotlin-multiplatform-mobile-plugin)

### Setup

- get an API key at https://data.rijksmuseum.nl/docs/api/#access-to-apis
- Put that api kee in [API_KEY_HERE] in the `network` module

---

# WordSquared - Kotlin Multiplatform Word Puzzle Game

A daily word puzzle game built with Kotlin Multiplatform and Compose Multiplatform, where players solve word squares by filling in letters to form valid words both horizontally and vertically.

## 🎮 Game Overview

WordSquared is a crossword-style puzzle game where:
- Players receive a daily puzzle with a partially filled grid
- Fill in the missing letters to complete valid words
- Words must work both horizontally and vertically
- Each day brings a new challenging puzzle
- Multiple difficulty levels available (4x4, 5x5, 6x6 grids)

## ✨ Features

### 🎯 Core Gameplay
- **Daily Puzzles**: Fresh word challenges delivered daily via Firebase Cloud Functions
- **Multiple Difficulty Levels**:
  - **Normal**: 4x4 grid
  - **Hard**: 5x5 grid  
  - **Expert**: 6x6 grid
 - **Smart Word Validation**: Real-time validation using a local dictionary file
- **Visual Feedback**: Color-coded tiles show progress and hints
- **Score System**: Points based on completion time and guess efficiency

### 🎨 User Interface
- **Modern Material Design**: Clean, accessible interface with Compose UI
- **Cross-Platform Consistency**: Identical experience across all platforms
- **Responsive Layout**: Adapts to different screen sizes and orientations
- **Virtual Keyboard**: On-screen keyboard for mobile devices
- **Interactive Tutorial**: Learn game mechanics with guided introduction

### 📱 Platform Support
- **Android**: Native Android app with full feature set
- **iOS**: Native iOS app with platform-specific optimizations
- **Desktop**: Windows, macOS, and Linux desktop applications
- **Web**: Browser-based version with WebAssembly

### 🔧 Advanced Features
- **Offline Caching**: Play previous puzzles without internet
- **Game State Persistence**: Resume games across app restarts
- **Progress Tracking**: Track completion times and scores
- **Error Handling**: Graceful handling of network issues

## 🏗️ Technical Architecture

### Core Technologies
- **Kotlin Multiplatform**: Shared business logic across all platforms
- **Compose Multiplatform**: Unified UI framework
- **Ktor Client**: HTTP networking for API calls
- **Kotlinx Serialization**: JSON parsing and data serialization
- **Kotlinx Coroutines**: Asynchronous programming
- **Kotlinx DateTime**: Cross-platform date/time handling

### Backend & Services
- **Firebase Hosting**: Web app deployment
- **Firebase Cloud Functions**: Daily puzzle generation and distribution
 - **Cloud-hosted Dictionary**: Word lists served via Cloud Function
- **Multiplatform Settings**: Cross-platform preferences storage

### Platform-Specific Dependencies
- **Android**: OkHttp client
- **iOS**: Darwin HTTP client  
- **Desktop**: CIO HTTP client
- **Web**: JS HTTP client

## 🚀 Getting Started

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (for Android development)
- [Xcode](https://developer.apple.com/xcode/) (for iOS development)
- JDK 17 or higher
- Kotlin Multiplatform Mobile plugin

### Building the Project

```bash
# Clone the repository
git clone <repository-url>
cd wsq

# Build for Android
./gradlew androidApp:assembleDebug

# Build for Desktop
./gradlew desktopApp:createDistributable

# Build for Web
./gradlew wasmApp:wasmJsBrowserDevelopmentWebpack
```

### Running the Applications

#### Android
1. Open the project in Android Studio
2. Select the `androidApp` configuration
3. Run on device or emulator

#### iOS
1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Select your target device
3. Build and run

#### Desktop
```bash
./gradlew desktopApp:run
```

#### Web
```bash
./gradlew wasmApp:wasmJsBrowserDevelopmentRun
```
Then open `http://localhost:8080` in your browser

## 📁 Project Structure

```
wsq/
├── androidApp/          # Android-specific code
├── iosApp/             # iOS-specific code  
├── desktopApp/         # Desktop application
├── wasmApp/            # Web application
├── shared/             # Shared Kotlin Multiplatform code
│   ├── src/commonMain/kotlin/com/hiremarknolan/wsq/
│   │   ├── App.kt                    # Main app entry point
│   │   ├── game/                     # Game logic and state
│   │   │   ├── GameState.kt         # Core game state management
│   │   │   ├── GameLogic.kt         # Game rules and validation
│   │   │   ├── WordBoard.kt         # Board management
│   │   │   └── GamePersistence.kt   # Save/load functionality
│   │   ├── models/                   # Data models
│   │   │   ├── GameConfiguration.kt # Game settings
│   │   │   └── TileModels.kt        # Tile and grid models
│   │   ├── network/                  # API clients
│   │   │   └── WordSquareApiClient.kt
│   │   ├── ui/                       # Compose UI components
│   │   │   ├── GameScreen.kt        # Main game interface
│   │   │   ├── GameBoard.kt         # Interactive game board
│   │   │   ├── VirtualKeyboard.kt   # Mobile keyboard
│   │   │   └── GameModals.kt        # Dialogs and overlays
│   │   └── PlatformSettings.kt      # Platform abstractions
│   ├── src/androidMain/             # Android-specific implementations
│   ├── src/iosMain/                 # iOS-specific implementations
│   ├── src/desktopMain/             # Desktop-specific implementations
│   └── src/wasmJsMain/              # Web-specific implementations
├── functions/          # Firebase Cloud Functions
│   ├── generate-puzzles/    # Daily puzzle generation
│   └── get-puzzle/          # Puzzle delivery API
└── server/            # Optional Ktor server (development)
```

## 🎲 Game Mechanics

### How to Play
1. **Start**: Launch the app to get today's puzzle
2. **Observe**: Study the partially filled grid and given letters
3. **Fill**: Enter letters in empty cells to form valid words
4. **Validate**: Words are checked in real-time as you type
5. **Complete**: Finish when all words are valid and the grid is full

### Scoring
- Base points for puzzle completion
- Bonus points for fewer guesses
- Time-based scoring for quick completion
- Daily streak bonuses

### Difficulty Levels
- **4x4 Grid**: Perfect for beginners, shorter words
- **5x5 Grid**: Intermediate challenge with medium-length words
- **6x6 Grid**: Expert level with complex word intersections

## 🔧 Development

### Adding New Features
1. Implement shared logic in `shared/src/commonMain`
2. Add platform-specific code in respective platform directories
3. Update UI components in `shared/src/commonMain/kotlin/com/hiremarknolan/wsq/ui`
4. Test across all target platforms

### Building for Production
```bash
# Android Release
./gradlew androidApp:assembleRelease

# iOS Archive (in Xcode)
# Product -> Archive

# Desktop Distribution
./gradlew desktopApp:createDistributable

# Web Production Build
./gradlew wasmApp:wasmJsBrowserProductionWebpack
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Ensure cross-platform compatibility

## 📄 License

This project is open source. See the [LICENSE](LICENSE) file for details.

## 🎯 Roadmap

### Upcoming Features
- [ ] User accounts and cloud save
- [ ] Achievement system
- [ ] Custom puzzle creation
- [ ] Themed puzzle packs
- [ ] Multiplayer competitions
- [ ] Advanced statistics and analytics

### Platform Enhancements
- [ ] Apple Watch companion app
- [ ] Android TV support
- [ ] Progressive Web App features
- [ ] Desktop widget

---

**WordSquared** - Where words meet strategy in daily puzzle challenges!