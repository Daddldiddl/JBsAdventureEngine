# Refactoring Guide: Current Multi-Module Structure

## Goal
This guide describes the **current** architecture after the split into `model-lib`, `engine`, and the separate `editor/` module, and documents the next practical refactoring steps.

## Current Status
- Split is complete for Maven modules `model-lib` and `engine`
- Root `pom.xml` is a parent reactor (`packaging = pom`)
- `engine` depends on `model-lib`
- `editor/` exists as a separate Gradle/Compose Desktop project with its own wrapper, persistence layer, i18n bundle, and JSON round-trip model

## Current Repository Structure
```text
JBsAdventureEngine/
|- pom.xml                         (parent)
|- model-lib/
|  |- pom.xml
|  '- src/main/kotlin/net/daddldiddl/jbsadventure/
|     |- ILogger.kt
|     |- lang/
|     |- model/
|     '- tools/
|        |- DataValidator.kt
|        |- GameLoader.kt
|        '- serializers/
|- engine/
|  |- pom.xml
|  '- src/
|     |- main/kotlin/net/daddldiddl/jbsadventure/
|     |  |- Main.kt
|     |  |- Game.kt
|     |  |- GlobalContext.kt
|     |  |- IConsole.kt
|     |  '- tools/
|     |     |- Config.kt
|     |     |- ConsoleOutput.kt
|     |     |- SaveManager.kt
|     |     '- SimpleFileLog.kt
|     '- main/resources/lang/
|        |- de/
|        '- en/
|- editor/
|  |- build.gradle.kts
|  |- settings.gradle.kts
|  '- src/
|     |- main/kotlin/net/daddldiddl/jbsadventure/editor/
|     |  |- Main.kt
|     |  |- i18n/Messages.kt
|     |  |- io/
|     |  |  |- AdventureFileFormat.kt
|     |  |  |- EditorPersistence.kt
|     |  |  '- FileDialogs.kt
|     |  |- model/EditorSession.kt
|     |  '- ui/EditorApp.kt
|     '- main/resources/i18n/
|        |- messages_de.properties
|        '- messages_en.properties
|- AGENTS.md
|- README.md
|- TUTORIAL.md
'- EDITOR_DESIGN.md
```

## Build and Run Workflow
From repository root:

```bash
mvn -DskipTests package
```

Run engine fat JAR:

```bash
java -jar engine/target/jbs-adventure-engine-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Run with external data:

```bash
java -jar engine/target/jbs-adventure-engine-1.0-SNAPSHOT-jar-with-dependencies.jar --data ./data.json
```

Run the editor from its own project directory:

```bash
cd editor
./gradlew run
```

On Windows use `gradlew.bat` instead of `./gradlew`.

## Module Responsibilities
### `model-lib`
- Domain model (`Room`, `Item`, `Exit`, `Container`, `State`, `GameData`)
- Action/precondition model
- Language model (`LanguageData`, `Keys`)
- Serialization layer and data loader/validator
- Shared logging contract (`ILogger`)

### `engine`
- Program entry point and game loop
- Input parsing and command dispatch
- Runtime infrastructure (console output, config, save, concrete logger)
- Bundled language/game resources

### `editor`
- Compose Desktop UI for adventure authoring
- Adventure JSON load/save bridge for the engine-compatible file format
- Locale-aware editor strings and validation feedback
- Draft models for metadata, rooms, items, exits, states, actions, and preconditions

## Design Rules Going Forward
1. Keep game domain + serialization logic in `model-lib`.
2. Keep UI/runtime plumbing in `engine`.
3. Avoid `engine`-specific imports from `model-lib`.
4. Continue using language templates (`LANG.getTemplate(...)`) for player-facing text.
5. Keep DE/EN gameplay data in sync (`engine/src/main/resources/lang/de|en/data.json`).

## Completed Verification (2026-06-16)
- [x] `mvn -DskipTests package` succeeds at root
- [x] Fat JAR is produced under `engine/target/`
- [x] Game starts from fat JAR with bundled data
- [x] `--data` loading still works
- [x] Save/load still works with unchanged save format expectations
- [x] No new `engine` coupling introduced in `model-lib`
- [x] `editor/` is a separate Gradle project with a Compose Desktop entry point
- [x] Editor persistence converts between editor drafts and engine-compatible JSON
- [x] Editor ships EN/DE resource bundles and switches locale in the UI

## Next Refactoring Steps
1. **Warning cleanup:** remove or justify remaining compiler warnings in utility classes.
2. **Editor packaging:** keep `editor/` independent and decide whether to publish or consume the `model-lib` artifact via a local path, Maven coordinates, or a composite build.
3. **Boundary hardening (optional):** gradually reduce companion-singleton coupling where practical.

## Editor Module (Current)
- `editor/` is already a real MVP, not just a stub
- Keep `editor/` as independent Gradle project
- The current UI covers metadata, rooms, items, exits, states, actions, and preconditions
- Keep editor-only UI concerns out of `engine` and `model-lib`
- Start with EN+DE UI resource bundles from day one

## Notes
- This guide replaces the old pre-implementation migration plan.
- Historical migration details are no longer tracked here; use git history for that.
