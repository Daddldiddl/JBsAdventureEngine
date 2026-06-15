# Refactoring Guide: Current Multi-Module Structure

## Goal
This guide describes the **current** architecture after the split into `model-lib` and `engine`, and documents the next practical refactoring steps.

## Current Status
- Split is complete for Maven modules `model-lib` and `engine`
- Root `pom.xml` is now a parent reactor (`packaging = pom`)
- `engine` depends on `model-lib`
- `editor` implementation has started as an independent Gradle/Compose Desktop module

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

## Design Rules Going Forward
1. Keep game domain + serialization logic in `model-lib`.
2. Keep UI/runtime plumbing in `engine`.
3. Avoid `engine`-specific imports from `model-lib`.
4. Continue using language templates (`LANG.getTemplate(...)`) for player-facing text.
5. Keep DE/EN gameplay data in sync (`engine/src/main/resources/lang/de|en/data.json`).

## Completed Verification (2026-06-15)
- [x] `mvn -DskipTests package` succeeds at root
- [x] Fat JAR is produced under `engine/target/`
- [x] Game starts from fat JAR with bundled data
- [x] `--data` loading still works
- [x] Save/load still works with unchanged save format expectations
- [x] No new `engine` coupling introduced in `model-lib`

## Active Verification For Editor Track
- [ ] `editor` Gradle module builds after root Maven build (`model-lib` JAR available)
- [ ] Editor can open/save a minimal adventure JSON compatible with engine loader
- [ ] Actions and preconditions edited in editor serialize to engine-compatible JSON
- [ ] EN/DE UI resource bundles load correctly in editor runtime

## Next Refactoring Steps
1. **Warning cleanup:** remove or justify remaining compiler warnings in utility classes.
2. **Editor bootstrap:** keep `editor/` as independent Gradle project and consume local `model-lib` JAR (`../model-lib/target/jbs-adventure-model-1.0-SNAPSHOT.jar`).
3. **Editor MVP delivery:** implement metadata, rooms, items, exits, states, actions, and preconditions with baseline validation and JSON round-trip.
4. **Boundary hardening (optional):** gradually reduce companion-singleton coupling where practical.

## Editor Module (Planned)
When continuing editor implementation:
- Keep `editor/` as independent Gradle project
- Use local JAR dependency strategy for MVP (`../model-lib/target/jbs-adventure-model-1.0-SNAPSHOT.jar`)
- Keep editor-only UI concerns out of `engine` and `model-lib`
- Start with EN+DE UI resource bundles from day one

## Notes
- This guide replaces the old pre-implementation migration plan.
- Historical migration details are no longer tracked here; use git history for that.
