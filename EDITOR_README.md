# JB's Adventure Editor

Visual editor for creating and editing adventures for JB's Adventure Engine.

## Status

- Development stage: Phase 0/1 bootstrap in progress
- Initial implementation scope: metadata, rooms, items, exits, states, actions, preconditions, file open/save/new, baseline validation
- UI localization target: English and German from first runnable version

## Quick Start

### Prerequisites

- Java 21 or newer
- Gradle 8.5+ (or Gradle wrapper once generated)
- Built `model-lib` module (from parent project)

### Build

```bash
# From project root (builds model-lib + engine JARs):
mvn -DskipTests package

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

✨ **Initial Editing Scope**
- Create and edit adventure metadata, rooms, items, exits, and states
- Action and precondition builders (MVP scope)
- Item usage editing baseline

🔧 **Validation First**
- Three-level validation (Error/Warning/Info)
- Save blocking for errors only
- Warning/info surfacing in editor panels

🧪 **Compatibility Workflow**
- Save JSON from editor
- Load and run JSON with engine to validate compatibility

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
- Kotlin 2.4.0
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

## MVP Notes

- Graph view, advanced quick-fix automation, deep undo/redo, and full autosave recovery workflow are planned for post-MVP iterations.
- Initial success criteria: author minimal adventure in editor, save JSON, run it in engine without structural errors.

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
**Last Updated:** 2026-06-15

