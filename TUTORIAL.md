# JB's Adventure Engine – Complete Guide

## Table of Contents

### Part 1: Building Your First Adventure
1. [Getting Started](#getting-started)
2. [Basic Structure](#basic-structure)
3. [Tutorial: Common Game Features](#tutorial-common-game-features)
   - [Creating a Locked Treasure Chest](#tutorial-locked-treasure-chest)
   - [Clearing a Blocked Passage](#tutorial-clearing-blocked-passage)
   - [Making a Simple Container](#tutorial-simple-container)
   - [Creating Driveable Items](#tutorial-driveable-items)
   - [Using State-Dependent Items](#tutorial-state-dependent-items)
   - [Item Transformations](#tutorial-item-transformations)
   - [Hidden Items with onExamine](#tutorial-onexamine-actions)
   - [Multi-Use Items](#tutorial-multi-use-items)

### Part 2: Technical Reference
- [Architecture & Key Files](#architecture--key-files)
- [Global Singletons](#global-singletons)
- [i18n Pattern](#i18n-pattern--always-use-lang-for-player-facing-text)
- [Item Location System](#item-location-system)
- [Save System](#save-system)
- [Actions & ItemUsage](#actions--itemusage)
- [Precondition System](#precondition-system)
- [Adding New Action Types](#adding-a-new-action-type)
- [Adding New Precondition Types](#adding-a-new-precondition-type)
- [Serialization Notes](#serialization-notes)
- [Data Validation](#data-validation)
- [Container Items](#container-items)
- [Open/Lock Entity Hierarchy](#openlock-entity-hierarchy)

---

# Part 1: Building Your First Adventure

## Getting Started

### Project Overview
**JB's Adventure Engine** is a data-driven, text-based adventure game engine written in **Kotlin 2.3.21** / **Java 21**, built with Maven.  
Game content is defined entirely in **JSON** – no code changes required to create a new adventure.

**Key dependency:** `kotlinx-serialization-json:1.11.0` – all JSON serialization uses this library with custom surrogate serializers for polymorphic types.

### Building the Engine

```bash
# Standard build (creates executable JAR)
mvn -DskipTests package

# Build with documentation (adds KDoc via Dokka)
mvn -DskipTests -Pdocs package
```

**Note:** No automated tests exist; `mvn -DskipTests package` is the standard workflow.  
If `mvn clean` fails, close any running game instances first (they lock the JAR file on Windows).

### Running Your Adventure

```bash
# Run with the bundled example adventure
java -jar target/jbs-adventure-engine-1.0-SNAPSHOT.jar

# Run with your own adventure data file
java -jar target/jbs-adventure-engine-1.0-SNAPSHOT.jar --data ./my-adventure.json

# Run with debug output to console
java -jar target/jbs-adventure-engine-1.0-SNAPSHOT.jar --consoleDebug

# Run with file logging (creates timestamped log files like JBsBigAdventure_240605123045.log)
java -jar target/jbs-adventure-engine-1.0-SNAPSHOT.jar --log --logDebug

# Set language (default: en)
java -jar target/jbs-adventure-engine-1.0-SNAPSHOT.jar --lang en

# Show help
java -jar target/jbs-adventure-engine-1.0-SNAPSHOT.jar --help
```

**Tip:** Command-line options override persisted settings in `config.json`, which is saved in the working directory.

---

## Basic Structure

Every adventure is defined in a single JSON file with this structure:

```json
{
    "title": "Your Adventure Title",
    "introductionMessage": "Welcome message shown at game start",
    "exitMessage": "Goodbye message shown when player quits",
    "Rooms": [ /* array of room objects */ ],
    "Items": [ /* array of item objects */ ],
    "States": [ /* array of game state variables */ ]
}
```

### Minimal Room Example

```json
{
    "id": 1,
    "name": {
        "name": "forest clearing",
        "aliases": ["clearing"]
    },
    "description": "You are standing in a small clearing surrounded by tall trees.",
    "exits": [
        {
            "direction": "north",
            "targetRoomId": 2
        }
    ]
}
```

### Minimal Item Example

```json
{
    "id": 1,
    "name": {
        "name": "rusty key",
        "aliases": ["key"]
    },
    "description": "A small rusty key.",
    "carriable": true,
    "location": 1
}
```

**Location values:**

- Positive number: Room ID where item is located
- `-1`: Player's inventory
- `-2`: Inside a container
- `0`: Not in the game (removed or not yet discovered)

---

## Tutorial: Common Game Features

The following tutorials use real examples from the bundled adventure (see `src/main/resources/data.json`). Each section shows the complete JSON pattern you need.

<a name="tutorial-locked-treasure-chest"></a>
### Tutorial: Creating a Locked Treasure Chest

**Goal:** Create a treasure chest that is locked and requires a specific key to unlock.

**Step 1: Create the key item**

```json
{
    "id": 8,
    "name": {
        "name": "rusty key",
        "aliases": ["key", "rusty key"]
    },
    "description": "A small rusty key, it looks like it could open a simple lock.",
    "carriable": true,
    "location": 0
}
```

Note: `location: 0` means the key is not yet in the game. We'll make it discoverable later.

**Step 2: Create the treasure inside the chest**

```json
{
    "id": 9,
    "name": {
        "name": "shiny gem",
        "aliases": ["gem", "jewel", "treasure"]
    },
    "description": "A shiny gem that sparkles in the light. It looks valuable.",
    "carriable": true,
    "location": -2
}
```

Note: `location: -2` means it's inside a container (the chest).

**Step 3: Create the locked chest container**

```json
{
    "id": 12,
    "name": {
        "name": "treasure chest",
        "aliases": ["chest", "box"]
    },
    "description": "A sturdy looking treasure chest with intricate carvings on its surface.\nIt seems to be locked.",
    "carriable": false,
    "location": 9,
    "containedItems": [9],
    "isContainer": true,
    "supportsLockUnlock": true,
    "open": false,
    "locked": true,
    "keyId": 8
}
```

**Key fields explained:**

- `isContainer: true` – marks this as a container
- `containedItems: [9]` – list of item IDs inside the chest
- `supportsLockUnlock: true` – enables lock/unlock commands
- `open: false` – chest starts closed
- `locked: true` – chest starts locked
- `keyId: 8` – only item #8 (the rusty key) can unlock it

**Player interaction:**
```
> examine chest
A sturdy looking treasure chest with intricate carvings on its surface.
It seems to be locked. The treasure chest is closed and locked.

> unlock chest
You need the rusty key to unlock the treasure chest.

(after finding the key...)
> unlock chest
You unlock the treasure chest.

> open chest
You open the treasure chest.

> look
[...room description...]
You can see a treasure chest (open), a shiny gem.

> take gem
You take the shiny gem.
```

---

<a name="tutorial-clearing-blocked-passage"></a>
### Tutorial: Clearing a Blocked Passage

**Goal:** Create a blocked exit that can be cleared by using dynamite.

**Step 1: Create a room with a blocked exit**

```json
{
    "id": 8,
    "name": {
        "name": "mine cave-in site",
        "aliases": []
    },
    "description": "You have reached a site where a cave-in has occurred.\nThere is a faint glow visible through a crack far above.",
    "exits": [
        {
            "direction": "west",
            "targetRoomId": 7
        },
        {
            "direction": "east",
            "name": {
                "name": "caved-in passage",
                "aliases": ["tunnel", "passage"]
            },
            "description": "A former passage leading east is blocked by the cave-in.",
            "targetRoomId": 11,
            "blocked": true,
            "visible": true,
            "blockedDescription": "A passage leading east that was previously blocked by the cave-in."
        }
    ],
    "itemUsages": [ /* see Step 3 */ ]
}
```

**Key fields explained:**

- `blocked: true` – prevents using this exit
- `visible: true` – players can see the exit (but can't use it)
- `blockedDescription` – shown when examining the blocked exit

**Step 2: Create the dynamite item**

```json
{
    "id": 14,
    "name": {
        "name": "sticks of dynamite",
        "isPlural": true,
        "aliases": ["dynamite", "sticks", "explosives", "explosive"]
    },
    "description": "A few sticks of dynamite, it looks like they could be used to blast through something.",
    "carriable": true,
    "location": -2
}
```

**Step 3: Add ItemUsage to clear the passage**

Add this to the `itemUsages` array in room 8:

```json
{
    "itemId": 14,
    "actions": [
        {
            "type": "ModifyExit",
            "description": "You place the stick of dynamite among the rubble, light the fuse and quickly take cover.\nAfter a moment, the dynamite explodes, blasting away the rubble and clearing the passage to the east.",
            "direction": "east",
            "roomId": 8,
            "blocked": false,
            "newName": {
                "name": "passage leading east",
                "aliases": ["tunnel", "passage"]
            },
            "preconditions": [
                {
                    "type": "PreconditionExit",
                    "roomId": 8,
                    "direction": "east",
                    "blocked": true
                }
            ]
        }
    ],
    "consumeUsedItem": true
}
```

**Key fields explained:**

- `itemId: 14` – what item triggers this action (the dynamite)
- `type: "ModifyExit"` – modifies an exit's properties
- `blocked: false` – makes the exit passable
- `newName` – changes the exit's description after clearing
- `preconditions` – only works if the exit is currently blocked
- `consumeUsedItem: true` – removes the dynamite after use

**Player interaction:**
```
> look
[...room description...]
Exits: west, east (caved-in passage - blocked)

> use dynamite
You place the stick of dynamite among the rubble, light the fuse and quickly take cover.
After a moment, the dynamite explodes, blasting away the rubble and clearing the passage to the east.

> go east
[...player moves to room 11...]
```

---

<a name="tutorial-simple-container"></a>
### Tutorial: Making a Simple Container

**Goal:** Create a wooden crate that can be opened to reveal items inside.

**Step 1: Create the items inside**

```json
{
    "id": 7,
    "name": {
        "name": "fuse pack",
        "aliases": ["fuse", "fuses", "pack"]
    },
    "description": "A small pack of fuses, useful for repairing electrical equipment.\nThere are <numberOfUses> fuses left.",
    "carriable": true,
    "numberOfUses": 2,
    "location": -2
}
```

**Step 2: Create the container**

```json
{
    "id": 11,
    "name": {
        "name": "wooden crate",
        "aliases": ["crate", "box"]
    },
    "description": "A wooden crate that seems to have been left behind by the miners.",
    "carriable": false,
    "location": 3,
    "containedItems": [7],
    "isContainer": true
}
```

**Key fields explained:**

- `isContainer: true` – marks as a container
- `containedItems: [7]` – fuse pack is inside
- No `supportsLockUnlock` – can be opened without a key
- No `open` field – defaults to `false` (closed)
- No `locked` field – defaults to `false` (unlocked)

**Player interaction:**
```
> examine crate
A wooden crate that seems to have been left behind by the miners. The wooden crate is closed.

> open crate
You open the wooden crate.

> look
[...room description...]
You can see a wooden crate (open), a fuse pack.

> take fuse pack
You take the fuse pack.
```

---

<a name="tutorial-driveable-items"></a>
### Tutorial: Creating Driveable Items

**Goal:** Create a boat that transports the player between two locations.

**Step 1: Create rooms at both ends**

```json
{
    "id": 6,
    "name": { "name": "underground lake", "aliases": [] },
    "description": "You have reached an underground lake.\nThere is a small boat at the shore.",
    "exits": [],
    "itemUsages": [ /* see Step 3 */ ]
}
```

```json
{
    "id": 9,
    "name": { "name": "small island", "aliases": [] },
    "description": "You have reached a small island in the middle of the underground lake.\nThere is a wooden pier at the shore.",
    "exits": [],
    "itemUsages": [ /* will mirror the boat usage */ ]
}
```

**Step 2: Create the driveable boat**

```json
{
    "id": 1,
    "name": {
        "name": "boat",
        "aliases": ["boat", "small boat"]
    },
    "description": "A small wooden boat, suitable for crossing the underground lake.",
    "driveable": true,
    "location": 6
}
```

**Key fields explained:**

- `driveable: true` – marks item as usable for transportation
- `location: 6` – boat starts at the lake shore

**Step 3: Add ItemUsage to enable transport**

Add to room 6's `itemUsages`:

```json
{
    "itemId": 1,
    "actions": [
        {
            "type": "MoveTo",
            "description": "You untie the boat and step into it. You use the boat to\ncross the underground lake to reach the island on the other side.",
            "moveToRoomId": 9
        }
    ],
    "becomesUsable": false,
    "consumeUsedItem": false
}
```

Add to room 9's `itemUsages` (reverse direction):

```json
{
    "itemId": 1,
    "actions": [
        {
            "type": "MoveTo",
            "description": "You untie the boat and step into it.\nYou use the boat to cross the underground lake back to the shore.",
            "moveToRoomId": 6
        }
    ]
}
```

**Key fields explained:**

- `type: "MoveTo"` – teleports player to another room
- `moveToRoomId` – destination room ID
- `becomesUsable: false` – item can be used repeatedly
- `consumeUsedItem: false` – item stays in game after use

**Player interaction:**
```
> use boat
You untie the boat and step into it. You use the boat to
cross the underground lake to reach the island on the other side.

[You are now in room 9]

> use boat
You untie the boat and step into it.
You use the boat to cross the underground lake back to the shore.

[You are now back in room 6]
```

---

<a name="tutorial-state-dependent-items"></a>
### Tutorial: Using State-Dependent Items

**Goal:** Create an elevator control panel that requires a fuse to become operational.

**Step 1: Define the game state**

```json
{
    "stateKey": "ELEVATORPANEL_STATE",
    "description": "The elevator panel's fuse looks <currentValue>.",
    "possibleValues": ["operational", "broken"],
    "currentValue": "broken"
}
```

**Step 2: Create the control panel item**

```json
{
    "id": 3,
    "name": {
        "name": "control panel",
        "aliases": ["panel", "controls", "button"]
    },
    "description": "A sturdy looking control panel with a button to call the elevator up.\nThere is a fuse socket on the side of the panel.",
    "location": 5,
    "stateKey": "ELEVATORPANEL_STATE"
}
```

**Key fields explained:**

- `stateKey` – links item to game state for dynamic descriptions

**Step 3: Create the fuse item**

```json
{
    "id": 7,
    "name": {
        "name": "fuse pack",
        "aliases": ["fuse", "fuses", "pack"]
    },
    "description": "A small pack of fuses, useful for repairing electrical equipment.\nThere are <numberOfUses> fuses left.",
    "carriable": true,
    "numberOfUses": 2,
    "location": -2
}
```

**Step 4: Add ItemUsage to repair the panel**

Add to room 5's `itemUsages`:

```json
{
    "itemId": 7,
    "actions": [
        {
            "type": "ChangeState",
            "description": "You plug the fuse into the socket on the elevator's panel.\nThe panel springs to life. You can now call the elevator to this level.",
            "changedStateKey": "ELEVATORPANEL_STATE",
            "newStateValue": "operational",
            "preconditions": [
                {
                    "requiredStateKey": "ELEVATORPANEL_STATE",
                    "requiredStateValues": ["broken"]
                }
            ]
        }
    ],
    "becomesUsable": true,
    "consumeUsedItem": true
}
```

**Key fields explained:**

- `type: "ChangeState"` – modifies a game state variable
- `changedStateKey` – which state to change
- `newStateValue` – new value for the state
- `preconditions` – only works if panel is currently broken
- `becomesUsable: true` – item (fuse) becomes usable again after all uses exhausted
- `consumeUsedItem: true` – removes one use from the fuse pack

**Step 5: Make the panel button work when operational**

Add another ItemUsage for the panel button:

```json
{
    "itemId": 3,
    "actions": [
        {
            "type": "SetItemRoom",
            "description": "You push the button on the control panel.\nThe elevator is now on this level.",
            "affectedItemIds": [2],
            "moveToRoomIdForItems": 5
        }
    ],
    "becomesUsable": false,
    "consumeUsedItem": false
}
```

**Player interaction:**
```
> examine panel
A sturdy looking control panel with a button to call the elevator up.
There is a fuse socket on the side of the panel. The elevator panel's fuse looks broken.

> use panel
Nothing happens.

> use fuse
You plug the fuse into the socket on the elevator's panel.
The panel springs to life. You can now call the elevator to this level.

> examine panel
[...] The elevator panel's fuse looks operational.

> use panel
You push the button on the control panel.
The elevator is now on this level.
```

---

<a name="tutorial-item-transformations"></a>
### Tutorial: Item Transformations

**Goal:** Transform a "shiny gem" into the "Eye of Bengal" when examined multiple times.

**Step 1: Define tracking states**

```json
{
    "stateKey": "GEM_EXAMINED_ONCE",
    "description": "You had a first look at the gem.",
    "possibleValues": ["true", "false"],
    "currentValue": "false"
},
{
    "stateKey": "GEM_EXAMINED_TWICE",
    "description": "You have thoroughly examined the gem.",
    "possibleValues": ["true", "false"],
    "currentValue": "false"
}
```

**Step 2: Create the initial item**

```json
{
    "id": 9,
    "name": {
        "name": "shiny gem",
        "aliases": ["gem", "jewel", "treasure"]
    },
    "description": "A shiny gem that sparkles in the light. It looks valuable.",
    "carriable": true,
    "location": -2,
    "onExamine": [ /* see Step 4 */ ]
}
```

**Step 3: Create the transformed item**

```json
{
    "id": 10,
    "name": {
        "name": "Eye Of Bengal",
        "aliases": ["eye", "eye of bengal", "jewel", "treasure", "gem", "shiny gem"]
    },
    "description": "A beautiful gem known as the Eye Of Bengal. It is said to be worth a fortune.",
    "carriable": true,
    "location": 0
}
```

Note: `location: 0` means it doesn't exist until the transformation happens.

**Step 4: Add onExamine actions to the gem**

```json
"onExamine": [
    {
        "type": "ChangeState",
        "description": "As you quickly examine the gem, it seems familiar. But you can't seem to remember where you could know it from...",
        "changedStateKey": "GEM_EXAMINED_ONCE",
        "newStateValue": "true",
        "preconditions": [
            {
                "requiredStateKey": "GEM_EXAMINED_ONCE",
                "requiredStateValues": ["false"]
            }
        ]
    },
    {
        "type": "ChangeState",
        "description": "As you continue to examine the gem, you realize that this is the famous Eye Of Bengal.",
        "changedStateKey": "GEM_EXAMINED_TWICE",
        "newStateValue": "true",
        "preconditions": [
            {
                "requiredStateKey": "GEM_EXAMINED_ONCE",
                "requiredStateValues": ["true"]
            },
            {
                "requiredStateKey": "GEM_EXAMINED_TWICE",
                "requiredStateValues": ["false"]
            }
        ]
    },
    {
        "type": "TransformIntoItem",
        "description": "This is an enormous treasure! It looks like your expedition has actually been worth it...",
        "affectedItemIds": [9],
        "transformsIntoItemIds": [10],
        "preconditions": [
            {
                "requiredStateKey": "GEM_EXAMINED_TWICE",
                "requiredStateValues": ["true"]
            },
            {
                "type": "PreconditionItemsLocation",
                "requiredItems": [10],
                "requiredRoomForItems": 0
            }
        ]
    }
]
```

**Key concepts:**

- Actions execute in order
- Each action has preconditions to control when it fires
- First examine: Sets `GEM_EXAMINED_ONCE = true`
- Second examine: Sets `GEM_EXAMINED_TWICE = true`
- Third examine: Transforms item 9 into item 10

**Player interaction:**
```
> examine gem
As you quickly examine the gem, it seems familiar. But you can't seem to remember where you could know it from...

> examine gem
As you continue to examine the gem, you realize that this is the famous Eye Of Bengal.

> examine gem
This is an enormous treasure! It looks like your expedition has actually been worth it...

> inventory
You are carrying: Eye Of Bengal
```

---

<a name="tutorial-onexamine-actions"></a>
### Tutorial: Hidden Items with onExamine

**Goal:** Create a mummified miner that reveals a hidden key when examined.

**Step 1: Create the hidden key**

```json
{
    "id": 8,
    "name": {
        "name": "rusty key",
        "aliases": ["key", "rusty key"]
    },
    "description": "A small rusty key, it looks like it could open a simple lock.",
    "carriable": true,
    "location": 0
}
```

**Step 2: Create the miner with onExamine action**

```json
{
    "id": 17,
    "name": {
        "name": "mummified miner",
        "aliases": ["miner", "corpse", "mummy", "body"],
        "genderKey": "male"
    },
    "description": "The mummified remains of a miner who seems to have been trapped here when the exit collapsed.",
    "carriable": false,
    "location": 11,
    "onExamine": [
        {
            "type": "SetItemRoom",
            "description": "As you examine the mummified miner, you find a rusty key hidden in his pocket.\nYou stash it in your pockets - who knows where it might fit!",
            "affectedItemIds": [8],
            "moveToRoomIdForItems": -1,
            "preconditions": [
                {
                    "type": "PreconditionItemsLocation",
                    "requiredItems": [8],
                    "requiredRoomForItems": 0
                }
            ]
        }
    ]
}
```

**Key fields explained:**

- `onExamine` – actions that trigger when player examines this item
- `type: "SetItemRoom"` – moves items to a specific location
- `affectedItemIds: [8]` – which items to move
- `moveToRoomIdForItems: -1` – moves to player's inventory
- `preconditions` – only works if key has `location: 0` (not yet found)

**Player interaction:**
```
> examine miner
The mummified remains of a miner who seems to have been trapped here when the exit collapsed.

> examine miner
As you examine the mummified miner, you find a rusty key hidden in his pocket.
You stash it in your pockets - who knows where it might fit!

> inventory
You are carrying: rusty key

> examine miner
The mummified remains of a miner who seems to have been trapped here when the exit collapsed.
(The key is already found, so the action doesn't trigger again)
```

---

<a name="tutorial-multi-use-items"></a>
### Tutorial: Multi-Use Items

**Goal:** Create a fuse pack that can be used twice before being consumed.

**Create the multi-use item:**

```json
{
    "id": 7,
    "name": {
        "name": "fuse pack",
        "aliases": ["fuse", "fuses", "pack"]
    },
    "description": "A small pack of fuses, useful for repairing electrical equipment.\nThere are <numberOfUses> fuses left.",
    "carriable": true,
    "numberOfUses": 2,
    "location": -2
}
```

**Key fields explained:**

- `numberOfUses: 2` – item can be used twice
- `<numberOfUses>` in description – placeholder that shows remaining uses

**Configure ItemUsage with consumption:**

```json
{
    "itemId": 7,
    "actions": [
        {
            "type": "ChangeState",
            "description": "You plug the fuse into the socket...",
            "changedStateKey": "ELEVATORPANEL_STATE",
            "newStateValue": "operational"
        }
    ],
    "becomesUsable": true,
    "consumeUsedItem": true
}
```

**Key fields explained:**

- `consumeUsedItem: true` – decrements `numberOfUses` by 1 after use
- `becomesUsable: true` – item becomes available again after exhausting uses
- When `numberOfUses` reaches 0, the item is removed from the game

**Player interaction:**
```
> examine fuse pack
A small pack of fuses, useful for repairing electrical equipment.
There are 2 fuses left.

> use fuse
You plug the fuse into the socket...

> examine fuse pack
[...] There are 1 fuses left.

> use fuse
You plug the fuse into the socket...

> inventory
(The fuse pack is gone)
```

---

# Part 2: Technical Reference

## Architecture & Key Files

| Layer | File(s) | Role |
|---|---|---|
| Entry point | `Main.kt` | Wires globals, CLI args, game loop |
| Game controller | `Game.kt` | Parses input, dispatches all player commands |
| Runtime state | `model/GameData.kt` | Central runtime model (rooms, items, states, containers) |
| Data model | `model/Room.kt`, `model/Item.kt`, `model/Exit.kt`, `model/Container.kt` | Core game entities |
| Actions | `model/Actions/Action.kt` | `ActionType` enum + all action `data class`es |
| Item interaction | `model/Actions/ItemUsage.kt` | Links item IDs to lists of `Action`s within a room |
| Preconditions | `model/Actions/Precondition.kt` | Polymorphic precondition hierarchy (`model.actions` package) |
| Serialization | `tools/serializers/` | Custom surrogate serializers for all polymorphic types |
| Persistence | `tools/SaveManager.kt` | Saves/loads `savegame.json` in the working directory |
| Validation | `tools/DataValidator.kt` | Validates game data integrity (rooms, items, actions, preconditions) on load |
| i18n | `lang/LanguageData.kt`, `lang/Keys.kt` | All user-facing text via keyed templates |
| Config | `tools/Config.kt`, `config.json` | Log level / language persisted in `config.json` in working directory |
| Game data | `src/main/resources/data.json` | Bundled example adventure |
| Language data | `src/main/resources/lang/en.json` | English strings, command aliases, pronoun groups |

### Package Note
`Action.kt` and `Precondition.kt` live in the `model/actions/` folder and correctly declare `package net.daddldiddl.jbsadventure.model.actions`.  
`ItemUsage.kt` lives in the same `model/actions/` folder but declares `package net.daddldiddl.jbsadventure.model` (misplaced but functional). On Windows the folder is case-insensitive (`Actions/` == `actions/`).

---

## Global Singletons

Declared in `Main.kt`:

```kotlin
LOG: SimpleFileLog   // structured logger; creates timestamped log files (e.g., JBsBigAdventure_240605123045.log)
CONSOLE: ConsoleOutput
LANG: LanguageData   // all i18n text; access via LANG.getTemplate(Keys.Message.*)
DATA: GameData       // runtime game state; DATA.currentRoom tracks player position
```

These `lateinit var` globals are accessible from any file in the package.

### Config Initialization Pattern

`Main.kt` loads persisted `config.json` via `Config.current`, then applies command-line argument overrides, then saves the effective configuration back:

```kotlin
val effectiveConfig = Config.current.copy()  // Load persisted config
if (args.contains("--consoleDebug")) {
    effectiveConfig.writeLogToConsole = true
    effectiveConfig.logLevel = LogLevel.DEBUG
}
// ... other overrides ...
Config.current = effectiveConfig
Config.save()  // Persist for next run
```

This pattern ensures user preferences persist across runs while allowing per-launch overrides.

---

## i18n Pattern – Always Use `LANG` for Player-Facing Text

Never hardcode strings shown to the player. Use:

```kotlin
LANG.getTemplate(Keys.Message.msgItemNotUsable)
    .replace(Keys.StandIn.definiteName, item.getDescriptiveName(definite = true))
```

All placeholder constants live in `Keys.StandIn` (e.g. `<name>`, `<direction>`, `<items>`).  
All message key constants live in `Keys.Message` and `Keys.Part`.

### Name and NamedEntity

The `Name` class holds localizable name metadata (`name`, `aliases`, `genderKey`, `isPlural`) for game entities.  
`NamedEntity` interface provides language-aware helpers for articles, pronouns, and formatted names:

- `getArticle(definite)` – returns definite/indefinite article based on gender
- `getIndefiniteName()` / `getDefiniteName()` – name with appropriate article
- `getPronounSubject()` / `getPronounObject()` – gender-aware pronouns
- `nameMatches(lookupName)` – case-insensitive lookup including aliases

Implemented by `Room`, `Item`, `Container`, and `Exit`.

---

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

---

## Save System

`SaveManager` persists runtime state to `savegame.json` in the working directory using `Json { prettyPrint = true }`. The `SaveState` data class contains:

- `currentRoomId` – player's current room
- `itemStates` – location, usability, number of uses, container open/locked state, contained items
- `stateValues` – current values of all game state keys
- `exitStates` – mutable open/locked/blocked/visible flags per room+direction
- `roomStates` – mutable name/description for rooms (when changed at runtime)

Only divergence from `data.json` defaults is saved, making save files compact and human-readable.

---

## Actions & ItemUsage

`ItemUsage` is attached to a `Room` (not an `Item`): a room declares what happens when a specific item is used there.  
Each `ItemUsage` contains:

- `itemId` – the item that can be used
- `actions` – list of `Action` objects executed in order when item is used
- `becomesUsable` (optional) – if true, item becomes reusable after exhausting uses
- `consumeUsedItem` (optional) – if true, item is removed from game after use

An action is skipped when its `preconditions` are not met.

### onExamine Actions

`Room`, `Item`, `Container`, and `Exit` all have an optional `onExamine` field (list of `Action`s) that executes when the player examines the entity. Use this for effects like "examining the painting reveals a hidden switch" or gaining knowledge that unlocks other actions.

### Available Action Types

| Type | Key JSON fields | Description |
|---|---|---|
| `MoveTo` | `moveToRoomId` | Teleports player to another room |
| `SetItemRoom` | `affectedItemIds`, `moveToRoomIdForItems` | Moves items to a specific location |
| `ChangeState` | `changedStateKey`, `newStateValue` | Modifies a game state variable |
| `TransformIntoItem` | `affectedItemIds`, `transformsIntoItemIds` | Transforms items into other items |
| `ModifyExit` | `roomId`, `direction`, `open?`, `locked?`, `blocked?`, `visible?`, `newName?` | Changes exit properties |
| `ModifyContainer` | `containerId`, `open?`, `locked?` | Changes container state |

All actions support optional `preconditions`, `delayInMillis`, `description` (shown to player), and `comment` (ignored at runtime).

---

## Precondition System

Actions carry a `preconditions` list. Each precondition has a `type` field that selects the subclass:

| `type` | Class | Key fields |
|---|---|---|
| `PreconditionState` | `PreconditionState` | `requiredStateKey`, `requiredStateValues` |
| `PreconditionItem` | `PreconditionItem` | `itemId`, `location?`, `usable?`, `carriable?`, `driveable?`, `numberOfUses?` |
| `PreconditionContainer` | `PreconditionContainer` | `itemId`, `location?`, `open?`, `locked?`, `containsItems?`, `excludesItems?` |
| `PreconditionExit` | `PreconditionExit` | `roomId`, `direction`, `open?`, `locked?`, `blocked?`, `visible?` |
| `PreconditionItemsLocation` | `PreconditionItemsLocation` | `requiredItems`, `requiredRoomForItems?`, `requiredContainerForItems?` |

Serialization is handled by `PreconditionSerializer` (surrogate pattern in `tools/serializers/`).  
The `@Serializable(PreconditionSerializer::class)` annotation belongs **only on the abstract base class** – do not repeat it on subclasses.

---

## Adding a New Action Type

1. Add the enum value to `ActionType` in `model/Actions/Action.kt`.
2. Create a `data class FooAction(...) : Action(type = ActionType.Foo, ...)` in the same file with `override fun execute(gameData: GameData): Boolean`.
3. Register both `deserialize` and `serialize` branches in `tools/serializers/ActionSerializer.kt`.

---

## Adding a New Precondition Type

1. Add the enum value to `PreconditionType` in `model/Actions/Precondition.kt`.
2. Create `class FooPrecondition(...) : Precondition()` (no `@Serializable` annotation on the subclass) with `override fun isSatisfied(gameData: GameData): Boolean` and `override fun validate(gameData: GameData): Boolean`.
3. Add the required fields to `PreconditionSurrogate` in `tools/serializers/PreconditionSerializer.kt`.
4. Register both `deserialize` and `serialize` branches in `PreconditionSerializer`.

---

## Serialization Notes

- All polymorphic model classes use **custom surrogate serializers** in `tools/serializers/`.
- `GameLoader` uses `Json { ignoreUnknownKeys = true }` – unknown JSON fields are silently ignored.
- `SaveManager` uses `Json { prettyPrint = true }` and writes `savegame.json` to the **working directory**.
- `SetItemRoom` accepts both `moveToRoomId` (old) and `moveToRoomIdForItems` (new) for backward compatibility.

---

## Data Validation

`DataValidator.validate(gameData)` is automatically called by `GameLoader` after deserializing game data. It checks:

- Room exits pointing to non-existent rooms
- Items referencing invalid locations or non-existent state keys
- Item usages with invalid target room IDs or item IDs
- Action preconditions referencing non-existent entities

Validation warnings are logged but **do not prevent the game from loading** – this allows for rapid iteration during development. Check the log output for potential data issues.

### Language File Lookup Order

`GameLoader.loadLanguageData` searches in this order:

1. `./lang/<code>.json` (filesystem, relative to working directory)
2. `./<code>.json` (filesystem)
3. `/lang/<code>.json` (classpath / bundled in JAR)

Custom language files placed next to the JAR override the bundled ones.

---

## Container Items

`Container` extends `Item` and implements `OpenLockEnabledNamedEntity`.  
**Supports both open/close and lock/unlock** – `supportsOpenClose = true` (always), `supportsLockUnlock` is configurable via the JSON field `"supportsLockUnlock"` (defaults to `false`).  
Items inside have `location = FixedLocation.CONTAINER (-2)` and are tracked in `container.containedItems` (list of IDs).  
Use `GameData.getOpenContainerItemsForRoom()` / `getAllAccessibleItemsForRoom()` to reach them.  
`GameData.Containers` (computed property) gives a `Map<Int, Container>` of all containers in the game.

---

## Open/Lock Entity Hierarchy

`Exit` and `Container` both implement `OpenLockEnabledNamedEntity` → `OpenLockEnabledEntity` + `NamedEntity`.  
`supportsOpenClose` / `supportsLockUnlock` guard all open/close/lock operations. `keyId` specifies the item ID required to lock/unlock (null if no key required). `getOpenLockState()` returns the localized state string for display in descriptions.

**Implementation notes:**

- `Exit` accepts both `supportsOpenClose` and `supportsLockUnlock` as direct constructor parameters from JSON
- `Container` has `supportsOpenClose = true` (always) and accepts `supportsLockUnlock` from JSON (mapped to internal `configuredSupportsLockUnlock`)

---

## Quick Reference: JSON Field Summary

### Room Fields

- `id` **(required)** – unique room identifier
- `name` **(required)** – `Name` object with name, aliases, gender
- `description` – text shown when entering/looking
- `exits` – array of `Exit` objects
- `itemUsages` – array of `ItemUsage` objects
- `onExamine` – array of `Action` objects triggered when examining

### Item Fields

- `id` **(required)** – unique item identifier
- `name` **(required)** – `Name` object with name, aliases, gender
- `description` – text shown when examining
- `location` **(required)** – where item is located (room ID, -1, -2, or 0)
- `carriable` – can player pick this up? (default: `false`)
- `driveable` – can player use this for transport? (default: `false`)
- `usable` – can this item be used? (default: `true`)
- `numberOfUses` – how many times can it be used? (optional, consumed with `consumeUsedItem`)
- `stateKey` – links to a game state for dynamic descriptions
- `onExamine` – array of `Action` objects triggered when examining

### Container-Specific Fields (extends Item)

- `isContainer` – must be `true` to mark as container
- `containedItems` – array of item IDs inside
- `supportsLockUnlock` – enable lock/unlock? (default: `false`)
- `open` – is container open? (default: `false`)
- `locked` – is container locked? (default: `false`)
- `keyId` – item ID required to unlock (optional)

### Exit Fields

- `direction` **(required)** – compass direction key
- `targetRoomId` **(required)** – destination room ID
- `name` – custom name (defaults to direction)
- `description` – shown when examining exit
- `supportsOpenClose` – enable open/close? (default: `false`)
- `supportsLockUnlock` – enable lock/unlock? (default: `false`)
- `open` – is exit open? (default: `true`)
- `locked` – is exit locked? (default: `false`)
- `keyId` – item ID required to unlock (optional)
- `visible` – can players see this exit? (default: `true`)
- `blocked` – is exit blocked? (default: `false`)
- `blockedDescription` – shown when exit is blocked
- `onExamine` – array of `Action` objects triggered when examining

### State Fields

- `stateKey` **(required)** – unique state identifier
- `description` – template for displaying state (use `<currentValue>` placeholder)
- `possibleValues` **(required)** – array of allowed string values
- `currentValue` **(required)** – initial value

### ItemUsage Fields

- `itemId` **(required)** – which item triggers these actions
- `actions` **(required)** – array of `Action` objects to execute
- `becomesUsable` – item usable again after exhausting uses? (default: `false`)
- `consumeUsedItem` – decrement `numberOfUses`? (default: `false`)

---

**End of Guide**

For questions or contributions, see the project README.

