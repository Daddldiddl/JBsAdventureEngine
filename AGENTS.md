# AGENTS.md – JB's Adventure Engine

## Project Overview
Data-driven, text-based adventure game engine written in **Kotlin 2.4.0** / **Java 21**, built with Maven.  
Game content is defined entirely in JSON – no code changes required to create a new adventure.

**Key dependency:** `kotlinx-serialization-json:1.11.0` – all JSON serialization uses this library with custom surrogate serializers for polymorphic types.

## Build & Run

```bash
mvn -DskipTests package                                                          # fat JAR (standard)
mvn -DskipTests -Pdocs package                                                   # + KDoc via Dokka
java -jar target/jbs-adventure-engine-1.0-SNAPSHOT.jar                          # bundled adventure
java -jar target/jbs-adventure-engine-1.0-SNAPSHOT.jar --data ./data.json       # external data file
java -jar target/jbs-adventure-engine-1.0-SNAPSHOT.jar --consoleLog             # enable console logging
java -jar target/jbs-adventure-engine-1.0-SNAPSHOT.jar --fileLog --debug        # debug logging to file
java -jar target/jbs-adventure-engine-1.0-SNAPSHOT.jar --lang en                # language (default: en)
java -jar target/jbs-adventure-engine-1.0-SNAPSHOT.jar --help                   # show help (-h, -? also work)
```

No automated tests exist; `mvn -DskipTests package` is the standard workflow.

**Windows note:** When a command must be run with Bash, execute it via WSL on Windows (for example: `wsl bash ./testEN.sh`).

**Note:** A visual editor (Jetpack Compose Desktop) is planned. This will require refactoring into three modules:
- `model-lib` (Maven): Shared data models and serializers
- `engine` (Maven): Game runtime (current code, refactored)
- `editor` (Gradle): Visual editor for data.json

See `EDITOR_DESIGN.md` and `REFACTORING_GUIDE.md` for details. Until then, the engine remains a single Maven module.

## Architecture & Key Files

| Layer | File(s) | Role |
|---|---|---|
| Entry point | `Main.kt` | Wires globals, CLI args, game loop |
| Game controller | `Game.kt` | Parses input, dispatches all player commands |
| Runtime state | `model/GameData.kt` | Central runtime model (rooms, items, states, containers) |
| Data model | `model/Room.kt`, `model/Item.kt`, `model/Exit.kt`, `model/Container.kt`, `model/State.kt` | Core game entities |
| Actions | `model/actions/Action.kt` | `ActionType` enum + all action `data class`es |
| Item interaction | `model/actions/ItemUsage.kt` | Links item IDs to lists of `Action`s within a room |
| Preconditions | `model/actions/Precondition.kt` | Polymorphic precondition hierarchy (`model.actions` package) |
| Serialization | `tools/serializers/` | Custom surrogate serializers for all polymorphic types |
| Persistence | `tools/SaveManager.kt` | Saves/loads `savegame.json` in the working directory |
| Validation | `tools/DataValidator.kt` | Validates game data integrity (rooms, items, actions, preconditions) on load |
| i18n | `lang/LanguageData.kt`, `lang/Keys.kt` | All user-facing text via keyed templates |
| Config | `tools/Config.kt`, `config.json` | Log level / language persisted in `config.json` in working directory |
| Game data | `src/main/resources/lang/<code>/data.json` | Bundled adventure per language |
| Language data | `src/main/resources/lang/<code>/lang.json` | Strings, command aliases, pronoun groups per language |

## Package Note
`Action.kt` and `Precondition.kt` live in the `model/actions/` folder and correctly declare `package net.daddldiddl.jbsadventure.model.actions`.  
`ItemUsage.kt` lives in the same `model/actions/` folder but declares `package net.daddldiddl.jbsadventure.model` (misplaced but functional). On Windows the folder is case-insensitive (`Actions/` == `actions/`).

## Global Singletons (accessors in `GlobalContext.kt`)

```kotlin
LOG: ILogger         // typically backed by SimpleFileLog
CONSOLE: ConsoleOutput
LANG: LanguageData   // all i18n text; access via LANG.getTemplate(Keys.Message.*)
DATA: GameData       // runtime game state; DATA.currentRoom tracks player position
```

These are convenience accessors declared in `GlobalContext.kt`:
- `LOG` delegates to `ILogger.current`
- `CONSOLE` delegates to `GlobalContext.console`
- `LANG` delegates to `LanguageData.current`
- `DATA` delegates to `GameData.current`

They are initialized during startup via `GlobalContext.initLog(...)`, `GlobalContext.initialize(...)`, and `GlobalContext.setGameData(...)`.

## Content Parity Rule (DE/EN)

When changing gameplay-relevant JSON content, mirror the change in both language datasets:
- `src/main/resources/lang/de/data.json`
- `src/main/resources/lang/en/data.json`

This includes (at minimum) exits, containers, item usages/actions, preconditions, and state transitions so behavior stays aligned across languages.

### Logging System

`SimpleFileLog` supports five log levels: `ERROR(5)`, `WARN(4)`, `INFO(3)`, `CONSOLE(2)`, `DEBUG(1)` (priority in parentheses).
- Log files are created in working directory with pattern `JBsBigAdventure_yyMMddHHmmss.log`
- Console logging and file logging can be independently enabled/disabled
- Log output automatically strips ANSI color codes when writing to file
- Access via `LOG.debug()`, `LOG.info()`, `LOG.warn()`, `LOG.error()` methods

### Console Output

`ConsoleOutput` (global `CONSOLE`) handles all player-visible text with color support:
- `CONSOLE.print(message, color)` – colored output to player
- `ConsoleColor` enum provides ANSI color constants: `WHITE`, `LIGHTGREEN`, `LIGHTCYAN`, `LIGHTYELLOW`, `LIGHTRED`, `RESET`, etc.
- Always use `CONSOLE.print()` for player-facing text (not `println()`) to maintain consistent output channel

### Config Initialization Pattern

`Main.kt` loads persisted `config.json` via `Config.current`, then applies command-line argument overrides, then saves the effective configuration back:

```kotlin
val effectiveConfig = Config.current.copy()  // Load persisted config
if (args.contains("--consoleLog")) {
    effectiveConfig.writeLogToConsole = true
}
if (args.contains("--fileLog")) {
    effectiveConfig.writeFileLog = true
}
if (args.contains("--debug")) {
    effectiveConfig.logLevel = LogLevel.DEBUG
}
// ... other overrides ...
Config.current = effectiveConfig
Config.save()  // Persist for next run
```

This pattern ensures user preferences persist across runs while allowing per-launch overrides.

**Config fields:**
- `writeFileLog` – enable file logging
- `writeLogToConsole` – enable console logging
- `logLevel` – `DEBUG`, `INFO`, `WARN`, `ERROR`, `CONSOLE`
- `languageCode` – language file to load (default: `"en"`)
- `ignoreActionDelays` – skip `delayInMillis` sleeps in actions (for testing)

## i18n Pattern – Always Use `LANG` for Player-Facing Text

Never hardcode strings shown to the player. Use:

```kotlin
LANG.getTemplate(Keys.Message.msgItemNotUsable)
    .replace(Keys.StandIn.definiteName, item.getDescriptiveName(definite = true))
```

All placeholder constants live in `Keys.StandIn` (e.g. `<name>`, `<direction>`, `<items>`).  
All message key constants live in `Keys.Message` and `Keys.Part`.

### Name and NamedEntity

The `Name` class holds localizable name metadata (`name`, optional `definiteName`, optional `indefiniteName`, `aliases`, `genderKey`, `isPlural`) for game entities.  
`name` remains backward-compatible and acts as fallback for both forms when `definiteName`/`indefiniteName` are not provided (useful for English files).  
For languages with adjective inflection (e.g. German), provide both fields to correctly render article-dependent forms (e.g. `der mumifizierte Bergmann` vs `ein mumifizierter Bergmann`).
`NamedEntity` interface provides language-aware helpers for articles, pronouns, and formatted names:
- `getArticle(definite)` – returns definite/indefinite article based on gender
- `getIndefiniteName()` / `getDefiniteName()` – name with appropriate article
- `getPronounSubject()` / `getPronounObject()` – gender-aware pronouns
- `nameMatches(lookupName)` – case-insensitive lookup including aliases
- `debugName()` – returns debug-friendly representation (e.g., `'wooden door' (id=42)`)

Implemented by `Room`, `Item`, `Container`, and `Exit`.

## Item Location System

Items are placed by integer `location` on the `Item` class:

| Value | Meaning |
|---|---|
| `> 0` | Room ID |
| `FixedLocation.INVENTORY (-1)` | Player's inventory |
| `FixedLocation.CONTAINER (-2)` | Inside a container |
| `FixedLocation.NOT_ASSIGNED (0)` | Removed from play |
| `FixedLocation.INVALID (-666)` | Deserialization sentinel – never valid in game data |

Always use `GameData.setItemLocation()` – it keeps container membership lists in sync.

## State System

States (`model/State.kt`) represent game variables with constrained values:
- `stateKey` – unique identifier for the state
- `currentValue` – current runtime value (must be in `possibleValues`)
- `possibleValues` – list of allowed values for validation
- `description` – human-readable explanation (not shown to player)
- `comment` – optional designer note (ignored at runtime)

Access states via `GameData.States` map or helper methods:
- `GameData.getStateByKey(key)` – retrieve state object
- `GameData.isStateByKey(key, value)` – check current value
- `GameData.setCurrentStateValue(key, value)` – update state (validates against `possibleValues`)

Items can reference a state via `stateKey` to display state-dependent descriptions. Use `ChangeState` actions to modify state values at runtime.

## Save System

`SaveManager` persists runtime state to `savegame.json` in the working directory using `Json { prettyPrint = true }`. The `SaveState` data class contains:
- `currentRoomId` – player's current room
- `itemStates` – location, usability, number of uses, container open/locked state, contained items, mutable name/description
- `stateValues` – current values of all game state keys
- `exitStates` – mutable open/locked/blocked/visible flags per room+direction, mutable name/description
- `roomStates` – mutable name/description for rooms (when changed at runtime)

Only divergence from `data.json` defaults is saved, making save files compact and human-readable.

## Actions & ItemUsage

`ItemUsage` is attached to a `Room` (not an `Item`): a room declares what happens when a specific item is used there.  
Each `ItemUsage` contains:
- `itemId` – the item that can be used
- `actions` – list of `Action` objects executed in order when item is used
- `becomesUsable` (optional) – if true, item becomes reusable after exhausting uses
- `consumeUsedItem` (optional) – if true, item is removed from game after use

An action is skipped when its `preconditions` are not met.

### Item.onUse vs Room.itemUsages

**Room-specific actions** (`itemUsages`): Defined in the room JSON, these actions only execute when the item is used in that specific room.

**Item-global actions** (`onUse`): Defined directly on the `Item` in JSON, these actions execute whenever the item is used, regardless of location. The `onUse` actions run **after** any room-specific `itemUsages` actions.

Use `onUse` for items with location-independent behavior (e.g., a magic amulet that always teleports the player to a safe room). Combine with `numberOfUses` and `consumeUsedItem` in `ItemUsage` for multi-use items.

### Action Lists on Entities

Different entities support various action lists that trigger automatically:

**All Entities** (`Room`, `Item`, `Container`, `Exit`):
- `onExamine` – Executes when the entity is examined

**Items** (including Containers):
- `onUse` – Executes whenever the item is used (location-independent, runs after room-specific `ItemUsage` actions)

**Containers and Exits** (`OpenLockEnabledNamedEntity`):
- `onOpen` – Executes when opened (after state change)
- `onClose` – Executes when closed (after state change)
- `onLock` – Executes when locked (after state change)
- `onUnlock` – Executes when unlocked (after state change)

These action lists create dynamic, reactive game elements. All lists are optional and serialize to/from `null` when empty.

Available `ActionType` values and their required JSON fields:

| Type | Key JSON fields |
|---|---|
| `MoveTo` | `moveToRoomId` |
| `SetItemRoom` | `affectedItemIds`, `moveToRoomIdForItems` |
| `ChangeState` | `changedStateKey`, `newStateValue` |
| `TransformIntoItem` | `affectedItemIds`, `transformsIntoItemIds` |
| `ModifyExit` | `roomId`, `direction`, `open?`, `locked?`, `blocked?`, `visible?`, `newName?` |
| `ModifyContainer` | `containerId`, `open?`, `locked?` |
| `Message` | (none - only uses `description` field) |

All actions support optional `preconditions`, `delayInMillis`, `description` (shown to player), and `comment` (ignored at runtime).

## Precondition System

Actions carry a `preconditions` list. Each precondition has a `type` field that selects the subclass:

| `type` | Class | Key fields |
|---|---|---|
| `PreconditionState` | `PreconditionState` | `requiredStateKey`, `requiredStateValues` |
| `PreconditionItem` | `PreconditionItem` | `itemId`, `location?`, `usable?`, `carriable?`, `driveable?`, `numberOfUses?` |
| `PreconditionContainer` | `PreconditionContainer` | `itemId`, `location?`, `open?`, `locked?`, `containsItems?`, `excludesItems?` |
| `PreconditionExit` | `PreconditionExit` | `roomId`, `direction`, `open?`, `locked?`, `blocked?`, `visible?` |
| `PreconditionItemsLocation` | `PreconditionItemsLocation` | `requiredItems`, `requiredRoomForItems?`, `requiredContainerForItems?` |
| `PreconditionPlayer` | `PreconditionPlayer` | `location?`, `hasItems?`, `doesntHaveItems?` |

**PreconditionPlayer** checks player state:
- `location` – player must be in specified room (or use `null` to negate specific room checks)
- `hasItems` – player must have all specified items in inventory
- `doesntHaveItems` – player must NOT have any specified items in inventory
- At least one field must be set; all conditions are AND-combined

Serialization is handled by `PreconditionSerializer` (surrogate pattern in `tools/serializers/`).  
The `@Serializable(PreconditionSerializer::class)` annotation belongs **only on the abstract base class** – do not repeat it on subclasses.

## Adding a New Action Type

1. Add the enum value to `ActionType` in `model/Actions/Action.kt`.
2. Create a `data class FooAction(...) : Action(type = ActionType.Foo, ...)` in the same file with `override fun execute(gameData: GameData): Boolean`.
3. Register both `deserialize` and `serialize` branches in `tools/serializers/ActionSerializer.kt`.

## Adding a New Precondition Type

1. Add the enum value to `PreconditionType` in `model/Actions/Precondition.kt`.
2. Create `class FooPrecondition(...) : Precondition()` (no `@Serializable` annotation on the subclass) with `override fun isSatisfied(gameData: GameData): Boolean` and `override fun validate(gameData: GameData): Boolean`.
3. Add the required fields to `PreconditionSurrogate` in `tools/serializers/PreconditionSerializer.kt`.
4. Register both `deserialize` and `serialize` branches in `PreconditionSerializer`.

## Serialization Notes

- All polymorphic model classes use **custom surrogate serializers** in `tools/serializers/`.
- `GameLoader` uses `Json { ignoreUnknownKeys = true }` – unknown JSON fields are silently ignored.
- `SaveManager` uses `Json { prettyPrint = true }` and writes `savegame.json` to the **working directory**.
- `SetItemRoom` accepts both `moveToRoomId` (old) and `moveToRoomIdForItems` (new) for backward compatibility.
- `Name` JSON supports `name`, optional `definiteName`, and optional `indefiniteName`; if only `name` exists, runtime falls back automatically.

## Data Validation

`DataValidator.validate(gameData)` is automatically called by `GameLoader` after deserializing game data. It checks:
- Room exits pointing to non-existent rooms
- Items referencing invalid locations or non-existent state keys
- Item usages with invalid target room IDs or item IDs
- Action preconditions referencing non-existent entities

Validation warnings are logged but **do not prevent the game from loading** – this allows for rapid iteration during development. Check the log output for potential data issues.

## Language File Lookup Order (`GameLoader.loadLanguageData`)

1. `./lang.json` (filesystem, relative to working directory)
2. `./<code>.json` (filesystem)
3. `./<code>/lang.json` (filesystem)
4. `/lang/<code>/lang.json` (classpath / bundled in JAR)

The bundled `data.json` is also language-specific and loaded from `/lang/<languageKey>/data.json` on the classpath. External data files are loaded directly from the path given via `--data`.

## Container Items

`Container` extends `Item` and implements `OpenLockEnabledNamedEntity`.  
**Supports both open/close and lock/unlock** – `supportsOpenClose = true` (always), `supportsLockUnlock` is configurable via the JSON field `"supportsLockUnlock"` (defaults to `false`).  
Items inside have `location = FixedLocation.CONTAINER (-2)` and are tracked in `container.containedItems` (list of IDs).  
Use `GameData.getOpenContainerItemsForRoom()` / `getAllAccessibleItemsForRoom()` to reach them.  
`GameData.Containers` (computed property) gives a `Map<Int, Container>` of all containers in the game.

## Open/Lock Entity Hierarchy

`Exit` and `Container` both implement `OpenLockEnabledNamedEntity` → `OpenLockEnabledEntity` + `NamedEntity`.  
`supportsOpenClose` / `supportsLockUnlock` guard all open/close/lock operations. `keyId` specifies the item ID required to lock/unlock (null if no key required). `getOpenLockState()` returns the localized state string for display in descriptions.

**Key consumption:**
- `consumeKeyOnLock` (Boolean, default: `false`) – If true, the key item is removed from the game (location set to 0) after successfully locking
- `consumeKeyOnUnlock` (Boolean, default: `false`) – If true, the key item is removed from the game (location set to 0) after successfully unlocking

These fields allow for single-use keys or keys that break/disappear after use.

**Action Lists:**
Both `Exit` and `Container` support four optional action lists that trigger after state changes:
- `onOpen` – executes when opened
- `onClose` – executes when closed
- `onLock` – executes when locked
- `onUnlock` – executes when unlocked

**Implementation notes:**
- `Exit` accepts both `supportsOpenClose` and `supportsLockUnlock` as direct constructor parameters from JSON
- `Container` has `supportsOpenClose = true` (always) and accepts `supportsLockUnlock` from JSON (mapped to internal `configuredSupportsLockUnlock`)
- All four action lists are `List<Action>?` (nullable) – serialize to `null` when empty for compact JSON

