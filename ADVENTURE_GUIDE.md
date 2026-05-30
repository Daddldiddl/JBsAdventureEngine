# Build Your Own Adventure with JB's Adventure Engine

Want to build your own text adventure? Nice. You absolutely can.

No coding marathon, no scary setup - just your ideas plus one JSON file.
This guide uses the **current data model** and keeps things simple.

## Quick model overview (the short version)

- Top-level sections: `Rooms`, `Items`, `States`
- A `Room` can define `itemUsages`
- Each `ItemUsage` contains one or more `actions`
- Containers are regular entries in `Items` (`isContainer: true`)
- Exits are explicit objects (not direction->id maps)

## Commands players can use

`go`, `look`, `examine`, `use`, `take`, `drop`, `open`, `close`, `inventory`, `save`, `load`, `help`, `quit`

---

## Top-level JSON structure

```json
{
  "title": "My Adventure",
  "introductionMessage": "Welcome!",
  "exitMessage": "Bye!",
  "Rooms": [ ... ],
  "Items": [ ... ],
  "States": [ ... ]
}
```

Quick notes:
- Use `introductionMessage` for your start text.
- There is no separate `Containers` section anymore.

---

## Names are objects now

`name` is no longer a plain string. It is a `Name` object:

```json
"name": {
  "name": "control panel",
  "aliases": ["panel", "controls", "button"]
}
```

There are optional advanced fields (`genderKey`, `isPlural`), but for most adventures you only need `name` + `aliases`.

---

## Rooms

Think of a room as one place on your map.
Every room gets an ID, a name, a description, exits, and optional item interactions.
If your map is clear, your game instantly feels better for players.

```json
{
  "id": 5,
  "name": { "name": "machinery room", "aliases": [] },
  "description": "Old machines are humming.",
  "exits": [
    { "direction": "east", "targetRoomId": 4 },
    { "direction": "north", "targetRoomId": 10 }
  ],
  "itemUsages": [ ... ]
}
```

`itemUsages` uses lowercase `i` and `exits` is a **list of Exit objects**.
That sounds technical, but it gives you more control over each exit.

### Exit object fields

```json
{
  "direction": "north",
  "name": { "name": "iron gate", "aliases": ["gate", "door"] },
  "targetRoomId": 2,
  "supportsOpenClose": false,
  "supportsLockUnlock": false,
  "open": true,
  "locked": false,
  "visible": true,
  "description": "A heavy iron gate."
}
```

Defaults (if you don't set them):
- Exits: `supportsOpenClose = false`, `supportsLockUnlock = false`

Tip:
- Exits can auto-use the direction name, but giving them a real `name` is usually better.
- Why? Players can type things like `open gate` instead of only `open north`.

---

## Items

Items are all the stuff players can find, carry, examine, and use.

```json
{
  "id": 7,
  "name": { "name": "fuse pack", "aliases": ["fuse", "pack"] },
  "description": "A pack of fuses.",
  "carriable": true,
  "numberOfUses": 2,
  "location": -2
}
```

Useful location values:
- `-1` = inventory
- `-2` = inside a container
- `0` = not assigned / hidden

### Containers are Items

```json
{
  "id": 12,
  "name": { "name": "treasure chest", "aliases": ["chest"] },
  "description": "A sturdy old chest.",
  "location": 9,
  "isContainer": true,
  "containedItems": [9],
  "open": false,
  "locked": true
}
```

Container defaults in the engine:
- `supportsOpenClose = true`
- `supportsLockUnlock = false` (for now; lock/unlock behavior can be driven later by actions)

---

## States

States are simple world variables (like tiny switches):
"broken/operational", "off/on", "locked/unlocked".

```json
{
  "stateKey": "ELEVATORPANEL_STATE",
  "description": "The panel looks <currentValue>.",
  "possibleValues": ["operational", "broken"],
  "currentValue": "broken"
}
```

`stateKey` values are referenced by actions.

---

## Item usages and actions (new model)

This is where your puzzle logic lives.
When a player uses item X in room Y, one or more actions run.
If you ever get stuck, check this section first - most game behavior is here.

`itemUsages` live in rooms and now use **`actions: []`**.

```json
{
  "itemId": 7,
  "consumeUsedItem": true,
  "becomesUsable": true,
  "actions": [
    {
      "type": "ChangeState",
      "description": "You install the fuse.",
      "changedStateKey": "ELEVATORPANEL_STATE",
      "newStateValue": "operational",
      "preconditions": [
        {
          "requiredStateKey": "ELEVATORPANEL_STATE",
          "requiredStateValues": ["broken"]
        }
      ]
    }
  ]
}
```

### Supported action types

#### `MoveTo`

```json
{
  "type": "MoveTo",
  "description": "You ride the elevator.",
  "moveToRoomId": 6
}
```

#### `SetItemRoom`

```json
{
  "type": "SetItemRoom",
  "description": "You call the elevator.",
  "affectedItemIds": [2],
  "moveToRoomIdForItems": 5
}
```

#### `ChangeState`

```json
{
  "type": "ChangeState",
  "description": "The panel powers up.",
  "changedStateKey": "ELEVATORPANEL_STATE",
  "newStateValue": "operational"
}
```

#### `TransformIntoItem`

```json
{
  "type": "TransformIntoItem",
  "description": "The old item changes form.",
  "affectedItemIds": [9],
  "transformsIntoItemIds": [10]
}
```

#### `ModifyExit`

```json
{
  "type": "ModifyExit",
  "description": "A gate mechanism clicks.",
  "roomId": 5,
  "direction": "north",
  "open": true,
  "locked": false,
  "blocked": false,
  "visible": true
}
```

---

## Practical advice

- Keep IDs unique across `Items` (including containers), or weird things happen.
- Build in small steps: one room, one puzzle, quick test, repeat.
- Use `comment` fields for your own notes - the engine ignores them.
- Put player feedback in action `description` so `use` feels clear and satisfying.

## Your first mini adventure in 10 minutes

If you want to start *right now*, copy this tiny example into your `data.json`.
It gives you 2 rooms and 1 item to examine.

```json
{
  "title": "My First Adventure",
  "introductionMessage": "Welcome to your very first game!",
  "exitMessage": "Nice! You built and played your first adventure.",
  "Rooms": [
    {
      "id": 1,
      "name": { "name": "start room", "aliases": [] },
      "description": "You are in a small room with a single door.",
      "exits": [
        {
          "direction": "north",
          "name": { "name": "wooden door", "aliases": ["door"] },
          "targetRoomId": 2,
          "supportsOpenClose": true
        }
      ]
    },
    {
      "id": 2,
      "name": { "name": "treasure room", "aliases": [] },
      "description": "You found the treasure room. Nice!",
      "exits": [
        {
          "direction": "south",
          "name": { "name": "wooden door", "aliases": ["door"] },
          "targetRoomId": 1,
          "supportsOpenClose": true
        }
      ]
    }
  ],
  "Items": [
    {
      "id": 1,
      "name": { "name": "note", "aliases": ["paper"] },
      "description": "It says: Keep going, game designer!",
      "carriable": true,
      "location": 1
    }
  ],
  "States": []
}
```

Then try this:
- `look`
- `examine note`
- `take note`
- `go north`
- `go south`

Boom - your first playable adventure.
Now add one more room, one more item, and one tiny puzzle.

Have fun building. Start small, then make it awesome.
