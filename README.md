# JBsAdventureEngine

## Introduction
This Adventure Game Engine (which sounds way too impressive already) is a small, fun learning project I set up while learning Kotlin. It is intentionally simple, but you can still create full adventures by writing a JSON file and pointing the engine to it. **No coding required!**

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
This is my first text adventure engine project (except for reading *Write Adventures in BASIC* back in the 80s), so expect a few non-optimal solutions. I mostly come from a Java 8 background and started this to learn Kotlin while building something fun and usable.

### Regarding AI use
I used Copilot to set up the base project, plus AI-assisted completion and debugging during development and refactoring. Parts of the guides were AI-generated/translated. Any remaining mistakes are mine.

## Project Structure

- `pom.xml` (root): Maven parent with modules `model-lib` and `engine`
- `model-lib/`: Shared model, i18n, loader/validator, serializers
- `engine/`: Runtime, CLI loop, save/config/logging, bundled language resources

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

Requires **Java 21 or newer** (all dependencies are included in the JAR).

**Run with bundled example adventure:**
```bash
java -jar engine/target/jbs-adventure-engine-1.0-SNAPSHOT-jar-with-dependencies.jar
```

**Run with your own adventure data file:**
```bash
java -jar engine/target/jbs-adventure-engine-1.0-SNAPSHOT-jar-with-dependencies.jar --data ./path/to/my-adventure.json
```

**Enable console logging:**
```bash
java -jar engine/target/jbs-adventure-engine-1.0-SNAPSHOT-jar-with-dependencies.jar --consoleLog
```

**Enable file logging with debug level (creates timestamped log files):**
```bash
java -jar engine/target/jbs-adventure-engine-1.0-SNAPSHOT-jar-with-dependencies.jar --fileLog --debug
```

**Alternative log levels:**
```bash
java -jar engine/target/jbs-adventure-engine-1.0-SNAPSHOT-jar-with-dependencies.jar --info
java -jar engine/target/jbs-adventure-engine-1.0-SNAPSHOT-jar-with-dependencies.jar --warn
java -jar engine/target/jbs-adventure-engine-1.0-SNAPSHOT-jar-with-dependencies.jar --noLog
```

**Set language (default: en):**
```bash
java -jar engine/target/jbs-adventure-engine-1.0-SNAPSHOT-jar-with-dependencies.jar --lang en
```

**Show help:**
```bash
java -jar engine/target/jbs-adventure-engine-1.0-SNAPSHOT-jar-with-dependencies.jar --help
```

You can combine command-line parameters as needed. CLI options override persisted settings in `config.json`.

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
✅ **Message actions** – Display flavor text and feedback during action sequences  
✅ **Single-use keys** – Keys that break or get consumed when used  
✅ **Save/Load system** – Automatic save game persistence

## Planned Features

What may be coming in the future:

- 🔄 Item-on-item usage (currently you can only use an item in a room)
- 🔄 State changes triggering actions automatically
- 🔄 Item states reflected in names (currently only visible in descriptions)
- 💭 NPCs as additional object category with dialog options
- 💭 Stats and combat system for roleplay-like experiences

These are just ideas. This is not a continuous major effort, but I plan to expand it bit by bit whenever I have time, mainly to support more elaborate puzzles and stories.

