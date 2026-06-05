# JBsAdventureEngine

## Introduction
This Adventure Game Engine (which sounds way too impressive already) is just a small fun project I setup for learning to program Kotlin - its simplistic, but you can actually create adventures by creating a JSON file and pointing the game engine towards it. **No coding required!**

**📖 Want to build your own adventure?** Check out **[TUTORIAL.md](TUTORIAL.md)** for a comprehensive tutorial guide with step-by-step examples!

The guide includes tutorials for:
- Setting up locked treasure chests
- Creating blocked passages that can be cleared with items
- Making driveable items (boats, elevators)
- State-dependent puzzles
- Item transformations
- Hidden items revealed through examination
- And much more!

Have fun expanding this yourself or building adventures for it!

### Some notes to limit expectations:
It's a learning project for me and my first work on an text adventure engine (except for that 'Write Adventures in BASIC' book I read once in the 80s), so expect there to be non-optimal solutions as I now come from a mainly Java 8 background with little Kotlin knowledge. But then I'm not aiming to write the best adventure engine ever - I'm trying to learn the language and have fun doing so, and if everything works out I'll end up with a usable engine.

### Regarding AI use
I used Copilot to setup the base project (I can't be arsed to write a pom.xml, sorry!), as well as using AI-based code-completion and debugging throughout the development and for refactoring. Also the guides were AI generated/translated. The rest is my own stupidity, of which there is plenty to go around.

## Running Adventures

### Build Commands

**Standard build (no Dokka/docs generation):**
```bash
mvn -DskipTests package
```

**Build including API docs via Dokka:**
```bash
mvn -DskipTests -Pdocs package
```

**Note:** If `mvn clean` fails, close any running game instances first (they lock the JAR file on Windows).

### Running the Game

Requires **Java 21 or newer** to run (all dependencies are included in the JAR).

**Run with bundled example adventure:**
```bash
java -jar target/jbs-adventure-engine-1.0-SNAPSHOT.jar
```

**Run with your own adventure data file:**
```bash
java -jar target/jbs-adventure-engine-1.0-SNAPSHOT.jar --data ./path/to/my-adventure.json
```

**Enable debug output to console:**
```bash
java -jar target/jbs-adventure-engine-1.0-SNAPSHOT.jar --consoleDebug
```

**Enable file logging (creates timestamped log files):**
```bash
java -jar target/jbs-adventure-engine-1.0-SNAPSHOT.jar --log --logDebug
```

**Set language (default: en):**
```bash
java -jar target/jbs-adventure-engine-1.0-SNAPSHOT.jar --lang en
```

**Show help:**
```bash
java -jar target/jbs-adventure-engine-1.0-SNAPSHOT.jar --help
```

You can combine command line parameters as needed. Command-line options override persisted settings in `config.json`.

## Implemented Features

The engine now supports:

✅ **Full i18n support** – Commands, directions, help pages, and all responses are translatable  
✅ **Multiple actions per item usage** – Chain actions together (e.g., change state and move player)  
✅ **Exits as separate entities** – Exits can be opened, closed, locked, unlocked, blocked, hidden  
✅ **Item transformations** – Replace items with others (e.g., sword → magic sword)  
✅ **Dynamic exit modification** – Add/remove/modify exits at runtime  
✅ **Container items** – Treasure chests, boxes, crates that can contain other items  
✅ **Open/Close & Lock/Unlock** – Both containers and exits support these operations  
✅ **State-dependent items** – Item descriptions and behavior change based on game state  
✅ **Precondition system** – Actions only execute when conditions are met  
✅ **onExamine actions** – Examining items/rooms/exits can trigger effects  
✅ **Multi-use items** – Items that can be used multiple times before being consumed  
✅ **Driveable items** – Boats, elevators, or other transport mechanisms  
✅ **Save/Load system** – Automatic save game persistence

## Planned Features

What may be coming in the future:

- 🔄 Item-on-item usage (currently you can only use an item in a room)
- 🔄 State changes triggering actions automatically
- 🔄 Item states reflected in names (currently only visible in descriptions)
- 💭 NPCs as additional object category with dialog options
- 💭 Stats and combat system for roleplay-like experiences

These are basic ideas – this is not going to be a continuous major effort, but I'd like to expand it bit by bit whenever I have the time. The idea is to provide more features for elaborate puzzles and stories.

**Note:** The included adventure is a testbed/example for implemented features, not a full game. The texts are mostly AI-generated placeholders to demonstrate the engine's capabilities.

Enjoy!
