# Word Squared - Desktop App

This is the desktop version of Word Squared built with Compose Multiplatform.

## Running the Desktop App

To run the desktop app in development mode:

```bash
./gradlew :desktopApp:run
```

## Packaging the Desktop App

### For your current OS:
```bash
./gradlew :desktopApp:packageDistributionForCurrentOS
```

### For specific platforms:

#### macOS DMG:
```bash
./gradlew :desktopApp:packageDmg
```

#### Windows MSI:
```bash
./gradlew :desktopApp:packageMsi
```

#### Linux DEB:
```bash
./gradlew :desktopApp:packageDeb
```

The packaged apps will be available in `desktopApp/build/compose/binaries/main/`

## System Requirements

- JVM 17 or higher
- Compose Multiplatform
- The app supports Windows, macOS, and Linux 