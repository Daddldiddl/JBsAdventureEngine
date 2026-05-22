# Build Your Own Adventure with JB's Adventure Engine

So you want to create your own text adventure game? You're in the right place!  
JB's Adventure Engine lets you build an entire game just by editing a single JSON file — no programming required. You describe the world, the items, and what happens when players do things, and the engine handles the rest.

This guide walks you through everything you need to know.

---

## How the Game Works (The Big Picture)

The game is a classic **text adventure**. The player types commands like `go north`, `examine ladder`, or `use fuse pack` and the engine responds with text describing what happens. The whole game world lives in one file: `data.json`.

The player can:

| Command | What it does |
|---|---|
| `go north` / `n` / `north` | Move in a direction |
| `look` / `l` | Describe the current room |
| `examine ladder` / `x ladder` | Read an item's description |
| `use fuse pack` | Use an item in the current room |
| `take fuse pack` / `pickup` | Pick up a carriable item |
| `drop fuse pack` | Drop a carried item in the current room |
| `inventory` / `i` | List carried items |
| `save` / `load` | Save or restore the game |
| `help` | Show all commands |
| `quit` | Exit the game |

There are four building blocks you combine to create your adventure:

1. **Rooms** — the places the player can visit
2. **Items** — objects that exist in the world
3. **States** — flags that track the condition of things
4. **ItemUsages** — what happens when an item is used in a specific room

---

## The File Structure

`data.json` always looks like this:

```json
{
    "title": "My Cool Adventure",
    "introductionMessage": "Welcome, brave explorer!",
    "exitMessage": "Thanks for playing!",
    "Rooms": [ ...list of rooms... ],
    "Items": [ ...list of items... ],
    "States": [ ...list of states... ]
}
```

| Field | Required? | Description |
|---|---|---|
| `title` | Yes | The name of your game, shown at startup |
| `introductionMessage` | Yes | A welcome message shown before the game starts. Use `\n` for a new line. |
| `exitMessage` | Yes | A goodbye message shown when the player quits |
| `Rooms` | Yes | The list of all rooms in the game |
| `Items` | Yes | The list of all items in the game |
| `States` | No | Optional list of state flags (you only need these for puzzles) |

---

## Rooms

A room is any place the player can be — a dungeon cell, a forest clearing, a spaceship corridor. The player always starts in the room with **id 1**.

```json
{
    "id": 1,
    "name": "castle entrance",
    "description": "You stand before a massive iron gate.\nA cold wind howls through the cracks.",
    "exits": {
        "north": 2,
        "east": 3
    }
}
```

| Field | Required? | Description |
|---|---|---|
| `id` | Yes | A unique number for this room. **Room 1 is always the starting room.** |
| `name` | Yes | Short name shown to the player (e.g. "castle entrance") |
| `description` | Yes | What the player sees when they enter or type `look`. Use `\n` for line breaks. |
| `exits` | Yes | A list of directions and the room IDs they lead to. Can be empty `{}` for dead ends. |
| `ItemUsages` | No | What happens when specific items are used here (see the ItemUsages section below) |
| `comment` | No | A note for yourself — the game ignores it completely |

### Exits

Exits are just a mapping of direction name → room ID. Supported directions:

`north`, `south`, `east`, `west`, `up`, `down`

```json
"exits": {
    "north": 2,
    "west": 5,
    "up": 10
}
```

If a direction isn't listed, the player can't go that way. That's it!

> **Tip:** Draw your map on paper first. Give each location a number, sketch arrows for the connections, then translate it into exits.

---

## Items

An item is any object in the game world — a key, a torch, a magical sword, a broken machine. Items sit in rooms until the player interacts with them.

```json
{
    "id": 7,
    "name": "fuse pack",
    "alternateNames": ["fuse", "fuses", "pack"],
    "description": "A small pack of fuses. There are <numberOfUses> fuses left.",
    "carriable": true,
    "numberOfUses": 2,
    "location": 3
}
```

| Field | Required? | Description |
|---|---|---|
| `id` | Yes | A unique number for this item |
| `name` | Yes | The primary name used in the game |
| `alternateNames` | Yes | Other names the player might type for this item (must include the `name` value too if you want it to work as the main name as well) |
| `description` | Yes | Shown when the player types `examine`. |
| `location` | Yes | The room ID where this item starts. Use `-1` to put it in the player's inventory from the start. |
| `carriable` | No | `true` if the player can pick it up and carry it. Default: `false` |
| `driveable` | No | `true` if using this item can transport the player (e.g. a boat or elevator). Default: `false` |
| `usable` | No | `false` to disable using the item until something enables it. Default: `true` |
| `numberOfUses` | No | How many times the item can be used before it is consumed. Leave it out for unlimited uses. |
| `stateKey` | No | Links this item to a State — the state's description will be appended when examined |
| `comment` | No | A private note for yourself, ignored by the game |

### The `<numberOfUses>` placeholder

If you include `numberOfUses` in an item, you can write `<numberOfUses>` anywhere in the item's `description` and the engine will replace it with the actual remaining count:

```json
"description": "A torch. It will last for <numberOfUses> more minutes."
```

### Items that start in the player's inventory

Set `"location": -1` and the item will be in the player's inventory from the very beginning of the game.

---

## States

States are like switches or labels that track the condition of something in the world. For example, a door can be `locked` or `unlocked`; a machine can be `broken` or `operational`.

```json
{
    "stateKey": "DOOR_STATE",
    "description": "The door looks <currentValue>.",
    "possibleValues": ["locked", "unlocked"],
    "currentValue": "locked"
}
```

| Field | Required? | Description |
|---|---|---|
| `stateKey` | Yes | A unique name you make up (no spaces). Used to reference this state from items and usages. Convention: use ALLCAPS like `DOOR_STATE`. |
| `description` | Yes | Text appended to an item's examine output. Use `<currentValue>` and it will be replaced with the current state value. |
| `possibleValues` | Yes | The list of values this state can take |
| `currentValue` | Yes | The starting value (must be one of `possibleValues`) |
| `comment` | No | A private note, ignored by the game |

### Linking a state to an item

Add the `stateKey` field to an item. When the player examines that item, the state's description is automatically shown after the item's own description:

```json
{
    "id": 3,
    "name": "control panel",
    "alternateNames": ["panel", "controls", "button"],
    "description": "A sturdy panel with a call button. There is a fuse socket on the side.",
    "location": 5,
    "stateKey": "ELEVATORPANEL_STATE",
    "usable": false
}
```
With the state set to `"currentValue": "broken"`, examining the panel shows:  
*"A sturdy panel with a call button. There is a fuse socket on the side.  
The elevator panel looks broken."*

---

## ItemUsages — Making Things Happen

This is where the magic is. An `ItemUsage` says: *"When the player uses item X while standing in room Y, do Z."*

`ItemUsages` are defined **inside the room** where they apply:

```json
{
    "id": 5,
    "name": "mine machinery room",
    ...
    "ItemUsages": [
        {
            "itemId": 2,
            "description": "You step into the elevator and pull the lever.\nThe elevator descends to the lower level.",
            "action": "MoveTo",
            "moveToRoomId": 6
        }
    ]
}
```

Every ItemUsage has a base set of fields, plus extra fields depending on the `action` type.

### Base fields (always present)

| Field | Required? | Description |
|---|---|---|
| `itemId` | Yes | The ID of the item being used |
| `description` | Yes | The text shown to the player when they use the item here |
| `action` | Yes | What actually happens: `MoveTo`, `SetItemRoom`, or `ChangeState` |
| `consumeUsedItem` | No | `true` to remove one use (or destroy the item if `numberOfUses` reaches zero). Default: `false` |

---

### Action: `MoveTo` — Teleport the player

Moves the player to a different room. Use this for vehicles, ladders, trapdoors, teleporters, or anything that transports the player.

```json
{
    "itemId": 1,
    "description": "You climb into the boat and row across the underground lake.",
    "action": "MoveTo",
    "moveToRoomId": 9
}
```

| Extra field | Required? | Description |
|---|---|---|
| `moveToRoomId` | Yes | The room ID the player is moved to |

> **Example use cases:** a boat, an elevator, a rope ladder, a magic portal.

---

### Action: `SetItemRoom` — Move an item to a room

Moves another item to a specific room. Use this when using something causes an object to appear or relocate — like pressing a button to call an elevator to your floor.

```json
{
    "itemId": 3,
    "description": "You press the button. The elevator hums and arrives at this level.",
    "action": "SetItemRoom",
    "affectedItemId": 2,
    "moveToRoomId": 5
}
```

| Extra field | Required? | Description |
|---|---|---|
| `affectedItemId` | Yes | The ID of the item that gets moved |
| `moveToRoomId` | Yes | The room where the affected item is moved to |

> **Example use cases:** calling an elevator, a pulley that lowers a bridge (moves the bridge item into the player's current room), a button that spawns a key.

---

### Action: `ChangeState` — Flip a state switch

Changes a state value AND can also change an item's `usable` flag. Use this for puzzles where repairing or activating something unlocks new actions.

```json
{
    "itemId": 7,
    "description": "You plug the fuse into the socket. The panel springs to life!",
    "action": "ChangeState",
    "stateKey": "ELEVATORPANEL_STATE",
    "oldStateValue": "broken",
    "newStateValue": "operational",
    "affectedItemId": 3,
    "becomesUsable": true,
    "consumeUsedItem": true
}
```

| Extra field | Required? | Description |
|---|---|---|
| `stateKey` | Yes | The state to change |
| `oldStateValue` | Yes | The value the state must currently have for this usage to work (a guard condition) |
| `newStateValue` | Yes | The value the state is changed to |
| `affectedItemId` | No | An item whose `usable` flag is toggled by this action |
| `becomesUsable` | No | `true` to enable the affected item, `false` to disable it |
| `consumeUsedItem` | No | `true` to consume one use of the item being used (usually `true` for single-use items like fuses) |

> **Example use cases:** repairing a broken machine with a spare part, unlocking a door with a key, activating a magic artifact.

---

## How Rooms, Items, Usages and States Work Together

Let's walk through a complete mini-puzzle to see how everything connects.

### The Puzzle: Fix the elevator

The player needs to travel down to an underground lake, but the elevator won't work because its control panel is broken. To fix it, they need to find a fuse pack, then use it on the panel.

**Step 1 — Define the state**

```json
{
    "stateKey": "PANEL_STATE",
    "description": "The panel looks <currentValue>.",
    "possibleValues": ["broken", "operational"],
    "currentValue": "broken"
}
```

**Step 2 — Define the items**

The fuse pack sits in a distant room. The control panel is `usable: false` by default (so the player can't use it until it's repaired).

```json
{ "id": 1, "name": "fuse pack", "alternateNames": ["fuse", "pack"],
  "description": "A pack of spare fuses.", "carriable": true,
  "numberOfUses": 1, "location": 8 },

{ "id": 2, "name": "control panel", "alternateNames": ["panel", "button"],
  "description": "A control panel with a socket for a fuse.",
  "location": 5, "stateKey": "PANEL_STATE", "usable": false },

{ "id": 3, "name": "elevator", "alternateNames": ["elevator", "lift"],
  "description": "A sturdy elevator cage.", "driveable": true, "location": 5 }
```

**Step 3 — Define what happens when the fuse is used (in the panel's room)**

```json
{
    "id": 5,
    "name": "machinery room",
    "description": "An old machinery room. There is a control panel on the wall.",
    "exits": { "east": 4 },
    "ItemUsages": [
        {
            "itemId": 1,
            "description": "You plug the fuse in. The panel hums back to life!",
            "action": "ChangeState",
            "stateKey": "PANEL_STATE",
            "oldStateValue": "broken",
            "newStateValue": "operational",
            "affectedItemId": 2,
            "becomesUsable": true,
            "consumeUsedItem": true
        },
        {
            "itemId": 2,
            "description": "You press the button. The elevator descends to the lower level.",
            "action": "MoveTo",
            "moveToRoomId": 6
        }
    ]
}
```

**What the player experiences:**

1. Player finds the fuse pack in room 8 and types `take fuse pack`.
2. Player navigates to the machinery room (room 5).
3. Player types `examine control panel` → sees *"A control panel with a socket for a fuse. The panel looks broken."*
4. Player types `use fuse pack` → fuse is consumed, state changes to `operational`, panel becomes usable.
5. Player types `use control panel` → elevator takes them to the lower level.

That's a complete puzzle, built from just a few lines of JSON!

---

## Quick Tips and Common Mistakes

**Every room needs a unique ID, and so does every item.**  
If two rooms share the same ID, one of them simply won't work correctly. Keep a list on paper or a spreadsheet.

**Room 1 is always the starting room.**  
The player always begins there, so make it a place that makes sense as a starting point.

**`alternateNames` must include everything the player might type.**  
If the name is `"fuse pack"` but you don't put `"fuse"` in `alternateNames`, typing `use fuse` won't work.

**Use `\n` for line breaks inside any text field.**  
This works in `description`, `introductionMessage`, `exitMessage`, and `ItemUsage` descriptions.

**`ChangeState` only fires if the state currently has `oldStateValue`.**  
This means using a fuse on an already-fixed panel does nothing (and shows nothing). If you want a different message, you can add a second ItemUsage for the same item — but the engine only acts on the first one whose guard condition matches.

**Use `comment` freely.**  
The game engine completely ignores `comment` fields. Use them to leave notes for yourself: *"this is the elevator between rooms 5 and 6"*.

**Test as you build.**  
Add one room at a time, test it, then add the next. It's much easier to find problems early than to debug a 30-room adventure all at once.

---

## Full Reference Summary

### Top Level

```
title               string   — Game title
introductionMessage string   — Shown at startup (\n = new line)
exitMessage         string   — Shown on quit
Rooms               array    — List of Room objects
Items               array    — List of Item objects
States              array    — List of State objects (optional)
```

### Room

```
id          integer  — Unique room ID (1 = start)
name        string   — Short location name
description string   — Room description (\n = new line)
exits       object   — { "direction": roomId, ... }
ItemUsages  array    — List of ItemUsage objects (optional)
comment     string   — Your private note (ignored)
```

### Item

```
id             integer  — Unique item ID
name           string   — Primary item name
alternateNames array    — Other names the player can type
description    string   — Shown on examine (\n = new line, <numberOfUses> = count)
location       integer  — Starting room ID (-1 = player inventory)
carriable      boolean  — Can the player pick this up? (default: false)
driveable      boolean  — Does using this move the player too? (default: false)
usable         boolean  — Can the player currently use this? (default: true)
numberOfUses   integer  — Uses before consumed (omit for unlimited)
stateKey       string   — Links this item to a State
comment        string   — Your private note (ignored)
```

### State

```
stateKey       string   — Unique state name (e.g. "DOOR_STATE")
description    string   — Appended on examine (<currentValue> = substituted)
possibleValues array    — All valid values
currentValue   string   — Starting value (must be in possibleValues)
comment        string   — Your private note (ignored)
```

### ItemUsage

```
itemId          integer  — Item that triggers this usage
description     string   — Text shown to the player
action          string   — "MoveTo" | "SetItemRoom" | "ChangeState"
consumeUsedItem boolean  — Consume a use of itemId? (default: false)

--- For MoveTo ---
moveToRoomId    integer  — Room the player is moved to

--- For SetItemRoom ---
affectedItemId  integer  — Item to relocate
moveToRoomId    integer  — Room the item is moved to

--- For ChangeState ---
stateKey        string   — State to change
oldStateValue   string   — State must equal this for the action to fire
newStateValue   string   — State is changed to this
affectedItemId  integer  — Item whose usable flag is toggled (optional)
becomesUsable   boolean  — New usable value for affectedItem (optional)
```

---

Now go build something amazing! Start small — a three-room dungeon with one locked door is a great first project. Then add more rooms, more puzzles, and more surprises. The only limit is your imagination.
