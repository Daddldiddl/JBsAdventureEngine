# JB's Adventure Editor

Visual editor for creating and editing adventures for JB's Adventure Engine.

## Quick Start

### Prerequisites

- Java 21 or newer
- Gradle 8.5+ (wrapper included)
- Built `model-lib` module (from parent project)

### Build

```bash
# From editor directory:
gradle build

# Or from project root:
cd editor && gradle build
```

### Run

```bash
# From editor directory:
gradle run

# Or run JAR directly:
java -jar build/compose/jars/jbs-adventure-editor-1.0-SNAPSHOT.jar
```

## Features

✨ **Visual Editing**
- Create and edit rooms, items, exits, containers, states
- Drag-and-drop room graph
- Action and precondition builders

🔧 **Smart Tools**
- Three-level validation (Error/Warning/Info)
- Quick Fix buttons for common issues
- Auto-save every 3 minutes
- Undo/Redo support

🧪 **Integrated Testing**
- Launch game directly from editor (F5)
- Auto-save before testing
- Separate console window

🌍 **Localization**
- German and English UI
- System theme with manual override

## Project Dependencies

```
editor (this project)
  └── depends on: model-lib JAR
                  (from ../model-lib/target/)
```

The editor uses the shared model library to ensure 100% compatibility with the game engine.

## Build Configuration

The editor is built with Gradle for optimal Jetpack Compose Desktop support:

```kotlin
// Key dependencies:
- Jetpack Compose Desktop 1.6.0
- Kotlin 2.3.21
- kotlinx-serialization-json 1.11.0
- model-lib (from Maven build)
```

## Keyboard Shortcuts

| Shortcut | Action |
|---|---|
| Ctrl+N | New Adventure |
| Ctrl+O | Open Adventure |
| Ctrl+S | Save |
| Ctrl+Shift+S | Save As |
| Ctrl+Z | Undo |
| Ctrl+Y | Redo |
| Ctrl+F | Find/Search |
| Ctrl+G | Show Graph View |
| F5 | Test in Engine |
| Ctrl+Shift+V | Validate Adventure |

## Development

### Project Structure

```
editor/
├── build.gradle.kts          # Gradle build configuration
├── settings.gradle.kts       # Gradle settings
└── src/main/kotlin/
    └── net/daddldiddl/jbsadventure/editor/
        ├── Main.kt           # Application entry point
        ├── ui/               # UI components and screens
        ├── viewmodel/        # State management
        ├── model/            # Editor-specific models
        └── util/             # Utilities
```

### Running from Source

```bash
gradle run
```

### Building Distribution JAR

```bash
gradle packageUberJarForCurrentOS

# Output: build/compose/jars/jbs-adventure-editor-1.0-SNAPSHOT.jar
```

### Native Packaging (Future)

```bash
# macOS:
gradle packageDmg

# Windows:
gradle packageMsi

# Linux:
gradle packageDeb
```

## Validation Levels

The editor uses a three-level validation system:

🛑 **ERROR (Red)**
- Blocks saving
- Critical issues that prevent game launch
- Example: Duplicate entity IDs

⚠️ **WARNING (Yellow)**
- Allows saving but game won't work correctly
- Shows Quick Fix buttons
- Example: Exit to non-existent room

ℹ️ **INFO (Blue)**
- Informational only
- Suggestions and best practices
- Example: Consider adding aliases

## Auto-Save

Automatic backups are saved every 3 minutes to `.autosave/` folder:
- Keeps last 10 auto-saves
- Recovery dialog on crash detection
- Manual recovery via File → Restore from Auto-Save

## Configuration

Editor preferences are stored in:
- Linux/macOS: `~/.config/jbs-adventure-editor/preferences.json`
- Windows: `%APPDATA%\jbs-adventure-editor\preferences.json`

## Testing Configuration

Configure the engine JAR path in Preferences:
- Default: `../engine/target/jbs-adventure-engine-1.0-SNAPSHOT-jar-with-dependencies.jar`
- Custom: Point to any compatible engine build

## Troubleshooting

### "Cannot find model-lib JAR"

**Solution:** Build model-lib first:
```bash
cd ../model-lib
mvn install
cd ../editor
gradle build
```

### "Engine JAR not found for testing"

**Solution:** Build engine first:
```bash
cd ../engine
mvn package
```

Then configure path in editor Settings.

### Performance Issues

For adventures with 200+ entities:
- Enable "Fast Validation" in Preferences
- Use Search (Ctrl+F) instead of scrolling
- Close unused editor tabs

## Contributing

See main project README for contribution guidelines.

## License

MIT License - See LICENSE file in project root.

## Links

- [Main Project](../README.md)
- [Design Document](../EDITOR_DESIGN.md)
- [Refactoring Guide](../REFACTORING_GUIDE.md)
- [Engine Documentation](../AGENTS.md)

---

**Version:** 1.0-SNAPSHOT  
**Status:** In Development  
**Last Updated:** 2026-06-08

