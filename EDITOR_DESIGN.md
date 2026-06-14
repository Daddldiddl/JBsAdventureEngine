# JB's Adventure Editor – Design Document

**Version:** 1.0  
**Date:** 2026-06-08  
**Target Platform:** Desktop (Windows, Linux, macOS)  
**Technology:** Jetpack Compose for Desktop, Kotlin

---

## 1. Executive Summary

The **JB's Adventure Editor** is a visual tool for creating and editing adventure game data files (`data.json`) for the JB's Adventure Engine without requiring manual JSON editing. The editor will provide an intuitive, form-based interface for managing all game elements (rooms, items, exits, containers, states, actions, preconditions) and includes integrated testing capabilities.

### Key Goals

- **Zero JSON Knowledge Required**: Users should never need to edit raw JSON
- **Visual Workflow**: Drag-and-drop where appropriate, visual connections between entities
- **Comprehensive Coverage**: Support all engine features documented in AGENTS.md
- **Immediate Testing**: Launch game directly from editor for rapid iteration (F5)
- **Smart Validation**: Three-level system with Quick Fix buttons
- **Cross-Platform**: JAR distribution for Windows, Linux, and macOS

### Technology Highlights

- **UI**: Jetpack Compose for Desktop (modern, declarative)
- **Build**: Maven (engine + model) + Gradle (editor) hybrid approach
- **Architecture**: Three-module structure with shared model library
- **Languages**: Kotlin 2.3.21, Java 21
- **Localization**: German + English editor UI

### Critical Features

✅ **Auto-Save**: Automatic backup every 3 minutes with crash recovery  
✅ **Three-Level Validation**: Errors block save, Warnings allow save with Quick Fixes, Info suggests improvements  
✅ **Version Tolerance**: Load adventures with format warnings, migration framework for future updates  
✅ **Direct Engine Integration**: Uses engine classes directly via shared model-lib  
✅ **Smart ID Management**: Auto-increment with manual override, duplicate prevention  
✅ **Visual Graph**: Room connections with auto-layout + manual positioning  

### Target Timeline

**Phase 1 (Core MVP)**: 4-6 weeks  
**Phase 2 (Actions)**: 3-4 weeks  
**Phase 3 (Advanced)**: 2-3 weeks  
**Phase 4 (Visual)**: 2-3 weeks  
**Phase 5 (Polish)**: 2-3 weeks  
**Total to v1.0**: ~15-20 weeks

### Quick Reference Card

```
┌─────────────────────────────────────────────────────────┐
│ JB's Adventure Editor - Quick Reference                 │
├─────────────────────────────────────────────────────────┤
│ Build Commands:                                         │
│   mvn clean install              # Build model-lib      │
│   mvn -pl engine package         # Build engine         │
│   gradle build                   # Build editor (in /editor) │
│                                                         │
│ Run Commands:                                           │
│   java -jar editor.jar           # Launch editor        │
│   gradle run                     # Run from source      │
│                                                         │
│ Project Structure:                                      │
│   model-lib/  → Shared models (Maven)                   │
│   engine/     → Game engine (Maven)                     │
│   editor/     → Visual editor (Gradle)                  │
│                                                         │
│ Key Shortcuts:                                          │
│   Ctrl+N      New Adventure                             │
│   Ctrl+O      Open                                      │
│   Ctrl+S      Save                                      │
│   F5          Test in Engine                            │
│   Ctrl+Z/Y    Undo/Redo                                 │
│                                                         │
│ Validation Levels:                                      │
│   🛑 ERROR    → Blocks save                             │
│   ⚠️ WARNING  → Allows save + Quick Fixes               │
│   ℹ️ INFO     → Suggestions only                        │
│                                                         │
│ Auto-Save:                                              │
│   Interval: 3 minutes                                   │
│   Location: .autosave/ folder                           │
│   Retention: Last 10 saves                              │
└─────────────────────────────────────────────────────────┘
```

---

## 2. Technology Stack

### Core Technologies

| Component | Technology | Justification |
|---|---|---|
| UI Framework | Jetpack Compose for Desktop 1.6+ | Modern declarative UI, excellent Kotlin integration |
| Language | Kotlin 2.3.21 | Consistency with engine, native support for Compose |
| Build Tool (Engine) | Maven 3.9+ | Existing engine build system, familiar to maintainer |
| Build Tool (Editor) | Gradle 8.5+ | Better Compose Desktop support, easier packaging |
| Build Tool (Model) | Maven 3.9+ | Shared with engine for consistency |
| JSON Serialization | kotlinx-serialization-json 1.11.0 | Same as engine for compatibility |
| Data Validation | Shared code from model-lib | Reuse `DataValidator.kt` |
| Process Management | ProcessBuilder | Launch game for testing |
| Graph Layout (MVP) | Simple hierarchical | Force-directed layout in later phase |

**Build System Rationale:**
- **Maven for Engine & Model-Lib**: Maintains existing workflow, no migration needed
- **Gradle for Editor**: Leverages superior Compose Desktop tooling
- **Hybrid Benefit**: Select builds via `mvn -pl model-lib,engine` or `gradle :editor:build`

### Project Structure (Maven Multi-Module + Gradle)

```
JBsAdventureEngine/
├── pom.xml                                # Parent Maven POM (aggregates model-lib + engine)
├── model-lib/                             # NEW: Shared data models
│   ├── pom.xml                            # Maven module
│   └── src/main/kotlin/
│       └── net/daddldiddl/jbsadventure/
│           ├── model/                     # All data classes (moved from engine)
│           │   ├── GameData.kt
│           │   ├── Room.kt
│           │   ├── Item.kt
│           │   ├── Container.kt
│           │   ├── Exit.kt
│           │   ├── State.kt
│           │   ├── Name.kt
│           │   ├── OpenLockEnabledEntity.kt
│           │   ├── FixedLocations.kt
│           │   ├── SaveState.kt
│           │   └── actions/
│           │       ├── Action.kt
│           │       ├── Precondition.kt
│           │       └── ItemUsage.kt
│           ├── lang/                      # Language support (moved from engine)
│           │   ├── LanguageData.kt
│           │   └── Keys.kt
│           └── tools/
│               ├── serializers/           # All serializers
│               │   ├── ActionSerializer.kt
│               │   ├── PreconditionSerializer.kt
│               │   ├── GameDataSerializer.kt
│               │   ├── ItemSerializer.kt
│               │   ├── ExitSerializer.kt
│               │   ├── RoomSerializer.kt
│               │   └── LanguageDataSerializer.kt
│               ├── DataValidator.kt       # Validation logic
│               └── GameLoader.kt          # Load/parse JSON
├── engine/                                # Existing engine (refactored)
│   ├── pom.xml                            # Depends on model-lib
│   └── src/main/kotlin/
│       └── net/daddldiddl/jbsadventure/
│           ├── Main.kt                    # Engine entry point
│           ├── Game.kt                    # Game controller
│           └── tools/
│               ├── ConsoleOutput.kt       # Engine-specific
│               ├── SimpleFileLog.kt       # Engine-specific
│               ├── SaveManager.kt         # Uses model-lib classes
│               └── Config.kt              # Engine-specific
└── editor/                                # NEW: Editor application
    ├── build.gradle.kts                   # Gradle build (Compose Desktop)
    ├── settings.gradle.kts
    └── src/main/kotlin/
        └── net/daddldiddl/jbsadventure/editor/
            ├── Main.kt                    # Editor entry point
            ├── ui/
            │   ├── MainWindow.kt          # Main application window
            │   ├── theme/                 # Material theme configuration
            │   ├── components/            # Reusable UI components
            │   │   ├── NavigationTree.kt
            │   │   ├── ValidationPanel.kt
            │   │   └── ...
            │   ├── screens/               # Editor screens
            │   │   ├── RoomEditor.kt
            │   │   ├── ItemEditor.kt
            │   │   ├── StateEditor.kt
            │   │   └── ...
            │   ├── dialogs/               # Modal dialogs
            │   │   ├── ActionBuilder.kt
            │   │   ├── PreconditionBuilder.kt
            │   │   └── ...
            │   └── views/                 # Visual views
            │       ├── RoomGraphView.kt
            │       └── ItemLocationsView.kt
            ├── viewmodel/                 # State management
            │   ├── AdventureViewModel.kt
            │   ├── RoomEditorViewModel.kt
            │   └── ...
            ├── model/                     # Editor-specific models
            │   ├── EditorState.kt
            │   ├── ValidationResult.kt
            │   └── UndoCommand.kt
            ├── util/                      # Utilities
            │   ├── AutoSaveManager.kt
            │   ├── IdGenerator.kt
            │   └── GraphLayout.kt
            └── resources/
                ├── icons/                 # Application icons
                └── i18n/                  # Editor UI translations
                    ├── messages_en.properties
                    └── messages_de.properties
```

**Dependency Flow:**
```
editor (Gradle) ──► model-lib (Maven JAR)
                    ▲
engine (Maven)  ────┘
```

---

## 3. Core Features

### 3.1 Project Management
- Create new adventure projects
- Open existing `data.json` files
- Save/Save As functionality
- **Auto-Save**: Automatic backup every 3 minutes (configurable)
- **Auto-Recovery**: Restore from auto-save on crash recovery
- Recent files list (last 10)
- Import/Export partial data (e.g., just rooms or items)
- Version detection with compatibility warnings

#### Auto-Save Details

**Strategy:**
```
Working Directory:
  adventure.json              ← Main file (user explicitly saves)
  .autosave/
    adventure_20260608_143022.json    ← Auto-save every 3 minutes
    adventure_20260608_143322.json
    adventure_20260608_143622.json
    ... (keeps last 10 auto-saves)
```

**Behavior:**
- Auto-save triggers 3 minutes after last change (debounced)
- Only saves if changes exist since last auto-save
- Non-intrusive (background thread, no UI blocking)
- Status bar shows "Auto-saved at 14:30" confirmation

**Recovery:**
```
╔═══════════════════════════════════════════════════╗
║ Auto-Save Recovery                                 ║
╠═══════════════════════════════════════════════════╣
║                                                   ║
║ An auto-save file is newer than your last save:  ║
║                                                   ║
║ Last Saved:    2026-06-08 14:25:00               ║
║ Auto-Saved:    2026-06-08 14:36:00 (11 min ago)  ║
║                                                   ║
║ The auto-save may contain unsaved changes.       ║
║                                                   ║
║ [Restore Auto-Save] [Use Last Save] [Compare]    ║
║                                                   ║
╚═══════════════════════════════════════════════════╝
```

**Manual Recovery:**
- File → Restore from Auto-Save...
- Shows list of available auto-saves with timestamps
- Preview differences before restoring

### 3.2 Adventure Metadata
- Title, introduction message, exit message
- Language selection
- Project-level settings

### 3.3 Entity Management
- **Rooms**: Create, edit, delete, link via exits
- **Items**: Full item properties, including containers
- **Exits**: Visual connection between rooms
- **States**: Global game variables
- **Actions**: Build action chains with visual flow
- **Preconditions**: Visual condition builder
- **ItemUsages**: Link items to rooms with actions

### 3.4 Validation & Testing
- Real-time validation with inline error display
- Integrated game launcher
- Separate console window for testing
- Quick reload after changes

### 3.5 Visual Aids
- Room graph/map view
- Item location overview
- Action flow diagrams
- State dependencies visualization

---

## 4. User Interface Design

### 4.1 Application Layout

```
┌─────────────────────────────────────────────────────────────────┐
│ File  Edit  View  Tools  Help                    [🔍] [▶ Test]  │ Menu Bar
├─────────────────────────────────────────────────────────────────┤
│ ⊞ Adventure Info                                                │
│ ⊞ States (3)                                                    │ Nav Tree
│ ⛶ Rooms (12)                                                    │ (Collapsible)
│   ├─ 🏠 Starting Room (1)                                       │
│   ├─ 🌲 Forest Path (2)                                         │
│   └─ 🏰 Castle Gate (3)                                         │
│ ⛶ Items (25)                                                    │
│   ├─ 🗝️ Rusty Key (101)                                        │
│   ├─ 📦 Wooden Chest (102) [Container]                         │
│   └─ ⚔️ Iron Sword (103)                                        │
├─────────────┬───────────────────────────────────────────────────┤
│             │ ╔═══════════════════════════════════════════════╗ │
│             │ ║ Room Editor: Starting Room (ID: 1)           ║ │ Editor Panel
│             │ ╠═══════════════════════════════════════════════╣ │ (Changes based
│             │ ║ [General] [Exits] [Item Usages] [Actions]    ║ │  on selection)
│             │ ╟───────────────────────────────────────────────╢ │
│             │ ║ Name: [Starting Room________________]  ⚙️     ║ │
│             │ ║ Aliases: [lobby, entrance, hall______]        ║ │
│             │ ║ Gender: [Neutral ▼] ☐ Plural                  ║ │
│             │ ║                                               ║ │
│             │ ║ Description:                                  ║ │
│             │ ║ ┌───────────────────────────────────────────┐ ║ │
│             │ ║ │You stand in a dimly lit hall with stone  │ ║ │
│             │ ║ │walls. Torches flicker on the walls...    │ ║ │
│             │ ║ └───────────────────────────────────────────┘ ║ │
│             │ ║                                               ║ │
│             │ ║ On Examine Actions: [+ Add Action]            ║ │
│             │ ║ ☐ Display flavor text when examined           ║ │
│             │ ╚═══════════════════════════════════════════════╝ │
├─────────────┴───────────────────────────────────────────────────┤
│ ✓ Ready  │ Adventure: "The Lost Crown"  │ 12 Rooms, 25 Items  │ Status Bar
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 Main Window Components

#### Top Menu Bar

**File Menu:**
- New Adventure (Ctrl+N)
- Open... (Ctrl+O)
- Save (Ctrl+S)
- Save As... (Ctrl+Shift+S)
- Recent Files ▶
- Import... (Partial import)
- Export... (Partial export)
- Exit

**Edit Menu:**
- Undo (Ctrl+Z)
- Redo (Ctrl+Y)
- Cut/Copy/Paste (for entities)
- Find... (Ctrl+F)
- Preferences

**View Menu:**
- Show/Hide Navigation Tree
- Show/Hide Properties Panel
- Room Graph View (Ctrl+G)
- Item Locations View
- State Dependencies View
- Zoom In/Out (for graph views)

**Tools Menu:**
- Validate Adventure (Ctrl+Shift+V)
- Test in Engine (F5)
- Configure Test Settings...
- Export Language Template

**Help Menu:**
- Documentation
- Tutorial
- About

#### Navigation Tree (Left Panel)

Hierarchical tree structure with collapsible sections:

```
⊞ Adventure Information
  ├─ Title
  ├─ Introduction Message  
  └─ Exit Message

⊞ States (3)
  ├─ gate_state [open, closed, locked]
  ├─ quest_progress [not_started, in_progress, ...]
  └─ time_of_day [morning, noon, night]

⊞ Rooms (12)
  ├─ 🏠 Starting Room (1)
  ├─ 🌲 Forest Path (2) ⚠️
  └─ 🏰 Castle Gate (3)

⊞ Items (25)
  ├─ 🗝️ Keys (3)
  │   ├─ Rusty Key (101)
  │   └─ Golden Key (102)
  ├─ 📦 Containers (5)
  │   ├─ Wooden Chest (201) [locked]
  │   └─ Leather Bag (202)
  └─ ⚔️ Weapons (4)
      └─ Iron Sword (301)
```

**Features:**
- Icons indicate entity types
- Warning icons (⚠️) for validation issues
- Search/filter box at top
- Right-click context menu (Edit, Duplicate, Delete)
- Drag-to-reorder within categories
- Color coding for item states (carriable, driveable, usable)

---

### 4.3 Editor Panels (Right Side)

#### 4.3.1 Room Editor

**General Tab:**
```
┌────────────────────────────────────────────────────┐
│ ID: [1]  (Auto-assigned, read-only)               │
│                                                    │
│ Name:                                              │
│   Name:     [Starting Room________________] ⚙️     │
│   Aliases:  [lobby, entrance, hall______]         │
│   Gender:   [Neutral ▼] ☐ Plural                  │
│                                                    │
│ Description:                                       │
│ ┌────────────────────────────────────────────────┐ │
│ │You stand in a dimly lit hall with stone walls.│ │
│ │Torches flicker, casting dancing shadows.      │ │
│ └────────────────────────────────────────────────┘ │
│                                                    │
│ On Examine Actions:                                │
│ ┌────────────────────────────────────────────────┐ │
│ │ 1. Message: "You notice strange symbols..."   │ │
│ │    [Edit] [Delete] [⬆] [⬇]                    │ │
│ └────────────────────────────────────────────────┘ │
│ [+ Add Action]                                     │
│                                                    │
│ Comment (internal):                                │
│ [This is the tutorial starting area________]      │
└────────────────────────────────────────────────────┘
```

**Exits Tab:**
```
┌────────────────────────────────────────────────────┐
│ Current Exits:                           [+ Add]   │
│                                                    │
│ North → Forest Path (2)                            │
│   Name: [rusty iron gate_________] ⚙️              │
│   ☑ Visible  ☑ Open  ☐ Locked  ☐ Blocked          │
│   ☑ Supports Open/Close  ☑ Supports Lock/Unlock   │
│   Key Required: [Golden Key (102) ▼] (optional)   │
│   ☐ Consume key on lock  ☐ Consume key on unlock  │
│   Description: [An old gate covered in rust_____] │
│   Blocked Desc: [The gate is jammed____________]  │
│                                                    │
│   On Open Actions:   [+ Add Action]                │
│   On Close Actions:  [+ Add Action]                │
│   On Lock Actions:   [+ Add Action]                │
│   On Unlock Actions: [+ Add Action]                │
│                                                    │
│   Item Usages at Exit:                             │
│   ┌──────────────────────────────────────────────┐ │
│   │ Crowbar (401): [Edit] [Delete]               │ │
│   │   → Break open the gate                      │ │
│   └──────────────────────────────────────────────┘ │
│   [+ Add Item Usage]                               │
│                                                    │
│   [Edit Exit] [Delete Exit] [Map View]             │
│                                                    │
│ ─────────────────────────────────────────────────  │
│                                                    │
│ South → Castle Courtyard (4)                       │
│   [Basic exit, no special properties]             │
│   [Edit Exit] [Delete Exit]                        │
│                                                    │
└────────────────────────────────────────────────────┘
```

**Item Usages Tab:**
```
┌────────────────────────────────────────────────────┐
│ Items that can be used in this room:               │
│                                                    │
│ ┌──────────────────────────────────────────────┐  │
│ │ 🗝️ Golden Key (102)                [Edit]    │  │
│ │   Actions: 3 configured                      │  │
│ │   1. Message: "The key glows..."             │  │
│ │   2. ModifyExit: Unlock north gate           │  │
│ │   3. ChangeState: quest_progress → started   │  │
│ │   Uses: 1  ☐ Becomes usable  ☑ Consume item │  │
│ │   [▲] [▼] [Delete]                           │  │
│ └──────────────────────────────────────────────┘  │
│                                                    │
│ ┌──────────────────────────────────────────────┐  │
│ │ ⚔️ Iron Sword (301)               [Edit]     │  │
│ │   Actions: 1 configured                      │  │
│ │   No special effects in this room            │  │
│ │   [▲] [▼] [Delete]                           │  │
│ └──────────────────────────────────────────────┘  │
│                                                    │
│ [+ Add Item Usage]                                 │
│                                                    │
│ Available Items: [Select item... ▼]               │
└────────────────────────────────────────────────────┘
```

#### 4.3.2 Item Editor

**General Tab:**
```
┌────────────────────────────────────────────────────┐
│ ID: [101]  (Auto-assigned, read-only)             │
│                                                    │
│ Name:                                              │
│   Name:     [Rusty Key________________] ⚙️         │
│   Aliases:  [key, old key, iron key___]           │
│   Gender:   [Neutral ▼] ☐ Plural                  │
│                                                    │
│ Description:                                       │
│ ┌────────────────────────────────────────────────┐ │
│ │An old iron key, covered in rust but still     │ │
│ │functional. It looks like it might open        │ │
│ │something important.                            │ │
│ └────────────────────────────────────────────────┘ │
│                                                    │
│ Properties:                                        │
│   Location:     [Starting Room (1) ▼]             │
│   ☑ Carriable   ☐ Driveable   ☑ Usable            │
│   Number of Uses: [1___] (empty = unlimited)      │
│                                                    │
│ State-Dependent Description:                       │
│   Linked State: [gate_state ▼] (optional)         │
│   When state = "open": [The gate stands open___]  │
│                                                    │
│ On Examine Actions:                                │
│   [+ Add Action]                                   │
│                                                    │
│ On Use Actions (location-independent):             │
│   ┌──────────────────────────────────────────────┐ │
│   │ 1. Message: "You turn the key..."           │ │
│   │    [Edit] [Delete] [⬆] [⬇]                  │ │
│   └──────────────────────────────────────────────┘ │
│   [+ Add Action]                                   │
│   Note: These run after room-specific usages.     │
│                                                    │
│ Comment (internal):                                │
│ [Tutorial key for first puzzle__________]         │
└────────────────────────────────────────────────────┘
```

**Container Properties Tab** (only for containers):
```
┌────────────────────────────────────────────────────┐
│ Container Type: [✓ This is a container]           │
│                                                    │
│ State:                                             │
│   ☑ Open   ☐ Locked                               │
│   ☑ Supports Open/Close                           │
│   ☑ Supports Lock/Unlock                          │
│                                                    │
│ Key Settings:                                      │
│   Key Required: [Brass Key (103) ▼] (optional)    │
│   ☐ Consume key on lock                           │
│   ☐ Consume key on unlock                         │
│                                                    │
│ Contained Items:                     [+ Add Item]  │
│ ┌────────────────────────────────────────────────┐ │
│ │ 🗝️ Golden Key (102)           [Remove]         │ │
│ │ 💎 Ruby (501)                  [Remove]         │ │
│ │ 📜 Ancient Scroll (502)        [Remove]         │ │
│ └────────────────────────────────────────────────┘ │
│                                                    │
│ On Open Actions:   [+ Add Action]                  │
│ On Close Actions:  [+ Add Action]                  │
│ On Lock Actions:   [+ Add Action]                  │
│ On Unlock Actions: [+ Add Action]                  │
│                                                    │
└────────────────────────────────────────────────────┘
```

#### 4.3.3 State Editor

```
┌────────────────────────────────────────────────────┐
│ State Key: [gate_state_______________]             │
│                                                    │
│ Current Value: [closed ▼]                          │
│                                                    │
│ Possible Values:              [+ Add Value]        │
│ ┌────────────────────────────────────────────────┐ │
│ │ • open       [✓] [Edit] [Delete] [⬆] [⬇]      │ │
│ │ • closed     [✓] [Edit] [Delete] [⬆] [⬇]      │ │
│ │ • locked     [✓] [Edit] [Delete] [⬆] [⬇]      │ │
│ │ • broken     [✓] [Edit] [Delete] [⬆] [⬇]      │ │
│ └────────────────────────────────────────────────┘ │
│                                                    │
│ Description (for designers):                       │
│ ┌────────────────────────────────────────────────┐ │
│ │Tracks the state of the main castle gate       │ │
│ └────────────────────────────────────────────────┘ │
│                                                    │
│ Comment:                                           │
│ [Used in chapter 1 puzzle sequence_____]          │
│                                                    │
│ Dependencies:                                      │
│   Used by Items: [2 items ▼]                      │
│     - Gate Key (102)                              │
│     - Wooden Lever (201)                          │
│   Referenced in Actions: [5 actions ▼]            │
│   Referenced in Preconditions: [3 preconditions ▼]│
│                                                    │
└────────────────────────────────────────────────────┘
```

#### 4.3.4 Action Builder Dialog

When clicking "Add Action" or "Edit Action":

```
╔═══════════════════════════════════════════════════╗
║ Action Builder                      [Save] [Cancel]║
╠═══════════════════════════════════════════════════╣
║                                                   ║
║ Action Type: [ChangeState ▼]                      ║
║                                                   ║
║ ┌───────────────────────────────────────────────┐ ║
║ │ Change State Configuration                    │ ║
║ │                                               │ ║
║ │ State Key:     [gate_state ▼]                 │ ║
║ │ New Value:     [open ▼]                       │ ║
║ │                                               │ ║
║ └───────────────────────────────────────────────┘ ║
║                                                   ║
║ Description (shown to player):                    ║
║ ┌───────────────────────────────────────────────┐ ║
║ │The gate creaks open slowly, revealing a dark  │ ║
║ │corridor beyond.                                │ ║
║ └───────────────────────────────────────────────┘ ║
║                                                   ║
║ Delay: [___] ms (optional)                        ║
║                                                   ║
║ Comment (internal): [_________________________]   ║
║                                                   ║
║ ─────────────────────────────────────────────────  ║
║                                                   ║
║ Preconditions:                   [+ Add Condition] ║
║ ┌───────────────────────────────────────────────┐ ║
║ │ ✓ Player has item: Golden Key (102)          │ ║
║ │   [Edit] [Delete]                             │ ║
║ │                                               │ ║
║ │ ✓ State 'quest_progress' = 'started'         │ ║
║ │   [Edit] [Delete]                             │ ║
║ └───────────────────────────────────────────────┘ ║
║                                                   ║
║ Preview: When this action executes, the game      ║
║          will change 'gate_state' to 'open' and   ║
║          display the message to the player.       ║
║                                                   ║
║                               [Test] [Save] [Cancel]║
╚═══════════════════════════════════════════════════╝
```

**Action Type Dropdown shows:**
- MoveTo (Teleport player)
- SetItemRoom (Move items)
- ChangeState (Modify game state)
- TransformIntoItem (Replace items)
- ModifyExit (Change exit properties)
- ModifyContainer (Change container properties)
- Message (Display text only)

Each type shows appropriate configuration fields.

#### 4.3.5 Precondition Builder Dialog

```
╔═══════════════════════════════════════════════════╗
║ Precondition Builder                [Save] [Cancel]║
╠═══════════════════════════════════════════════════╣
║                                                   ║
║ Condition Type: [PreconditionPlayer ▼]            ║
║                                                   ║
║ ┌───────────────────────────────────────────────┐ ║
║ │ Player Condition Configuration                │ ║
║ │                                               │ ║
║ │ Player Location:                              │ ║
║ │   ☐ Must be in room: [Starting Room (1) ▼]   │ ║
║ │                                               │ ║
║ │ Required Items in Inventory:                  │ ║
║ │   ☑ Golden Key (102)            [Remove]      │ ║
║ │   ☑ Magic Amulet (305)          [Remove]      │ ║
║ │   [+ Add Required Item]                       │ ║
║ │                                               │ ║
║ │ Must NOT Have in Inventory:                   │ ║
║ │   ☑ Cursed Ring (306)           [Remove]      │ ║
║ │   [+ Add Forbidden Item]                      │ ║
║ └───────────────────────────────────────────────┘ ║
║                                                   ║
║ Preview: This action will only execute if:        ║
║  • Player has Golden Key (102) in inventory       ║
║  • Player has Magic Amulet (305) in inventory     ║
║  • Player does NOT have Cursed Ring (306)         ║
║                                                   ║
║                               [Test] [Save] [Cancel]║
╚═══════════════════════════════════════════════════╝
```

**Precondition Types:**
- PreconditionState (Game state must match)
- PreconditionItem (Item must have specific properties)
- PreconditionContainer (Container must have specific state)
- PreconditionExit (Exit must have specific state)
- PreconditionItemsLocation (Items must be in specific location)
- PreconditionPlayer (Player state conditions)

---

### 4.4 Visual Graph Views

#### 4.4.1 Room Graph View

```
┌─────────────────────────────────────────────────────────┐
│ Room Graph                    [Zoom +] [Zoom -] [Reset] │
├─────────────────────────────────────────────────────────┤
│                                                         │
│      ┌──────────────┐                                   │
│      │ Forest Path  │◄─────┐                            │
│      │   (Room 2)   │      │                            │
│      └──────────────┘      │ north                      │
│             │              │                            │
│          south            ┌┴──────────────┐             │
│             │             │ Starting Room │             │
│             ▼             │   (Room 1)    │             │
│      ┌──────────────┐    └───────────────┘             │
│      │ Castle Gate  │          │                        │
│      │   (Room 3)   │◄─────────┘                        │
│      │              │      east                         │
│      └──────────────┘                                   │
│             │                                           │
│          north                                          │
│             │                                           │
│             ▼                                           │
│      ┌──────────────┐                                   │
│      │  Courtyard   │                                   │
│      │   (Room 4)   │                                   │
│      └──────────────┘                                   │
│                                                         │
│  Legend:                                                │
│  ───►  Normal exit                                      │
│  ═══►  Locked exit                                      │
│  ┈┈┈►  Hidden exit                                      │
│  ▓▓▓►  Blocked exit                                     │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**Features:**
- Click on room to select/edit
- Drag rooms to reposition
- Double-click to open editor
- Right-click for context menu
- Color coding for room states
- Hover shows room details

#### 4.4.2 Item Locations View

```
┌─────────────────────────────────────────────────────────┐
│ Item Locations                     [Filter ▼] [Refresh] │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ Starting Room (1)                                       │
│   🗝️ Rusty Key (101)               [Carriable]         │
│   📦 Wooden Chest (102)             [Container]         │
│      └─ 💎 Ruby (501)                                   │
│      └─ 📜 Scroll (502)                                 │
│   🏺 Clay Vase (103)                                    │
│                                                         │
│ Forest Path (2)                                         │
│   ⚔️ Iron Sword (301)               [Carriable]         │
│   🔦 Lantern (302)                  [Carriable]         │
│                                                         │
│ 🎒 Player Inventory                                     │
│   🗝️ Golden Key (201)                                   │
│   🍞 Bread (401)                                        │
│                                                         │
│ 🗑️ Not Assigned (Hidden from game)                     │
│   ⚔️ Broken Sword (999)                                 │
│                                                         │
│ ⚠️ Issues:                                              │
│   • Item 999: No room assigned                          │
│   • Item 201: Referenced but missing                    │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 5. Workflows

### 5.1 Creating a New Adventure

```
User Action                    → System Response
─────────────────────────────────────────────────────────
1. File → New Adventure        → Show "New Adventure Wizard"
2. Enter title, intro text     → Create basic project structure
3. Click "Create"              → Open editor with default room
4. Add rooms via tree          → Show Room Editor panel
5. Connect rooms with exits    → Update room graph
6. Add items to rooms          → Update navigation tree
7. Define item usages          → Link items to rooms
8. Create actions & states     → Build game logic
9. Test (F5)                   → Launch game in console
10. Save (Ctrl+S)              → Write data.json
```

### 5.2 Opening Existing Adventure

```
1. File → Open                 → File browser dialog
2. Select data.json            → Parse and validate
3. Load complete               → Populate all editors
4. Show validation warnings    → Display in status bar
5. Edit entities               → Real-time validation
6. Save changes                → Update data.json
```

### 5.3 Creating a Room with Exit

```
1. Right-click "Rooms"         → Context menu
2. Select "Add Room"           → Show Room Editor
3. Enter name, description     → Update navigation tree
4. Switch to "Exits" tab       → Show exit list
5. Click "+ Add Exit"          → Show exit configuration
6. Select direction            → North, South, East, etc.
7. Select target room          → Dropdown of all rooms
8. Configure exit properties   → Open/locked/visible states
9. Add exit actions (optional) → Action builder opens
10. Save                       → Update room data
11. View in graph              → Visual representation
```

### 5.4 Creating an Item with Usage

```
1. Right-click "Items"         → Context menu
2. Select "Add Item"           → Show Item Editor
3. Configure basic properties  → Name, description, location
4. Mark as "Usable"            → Enable usage configuration
5. Go to room where used       → Select room in tree
6. Switch to "Item Usages"     → Show item usage list
7. Click "+ Add Item Usage"    → Select item from dropdown
8. Configure usage             → Add actions, preconditions
9. Set number of uses          → 1 for single-use
10. Save                       → Update room and item
```

### 5.5 Building Complex Action Chains

```
1. Open Item Usage editor      → Select usage entry
2. Click "+ Add Action"        → Action Builder dialog
3. Select action type          → ChangeState, MoveTo, etc.
4. Configure action            → Fill in parameters
5. Add description             → Text shown to player
6. Add preconditions           → Click "+ Add Condition"
7. Configure precondition      → Choose type and values
8. Save action                 → Add to action list
9. Repeat for chain            → Multiple actions
10. Reorder with ⬆⬇            → Set execution order
11. Test action chain          → F5 to launch game
```

---

## 6. Validation & Error Handling

### 6.1 Three-Level Validation System

**Validation Levels:**

| Level | Color | Icon | Blocks Save? | Description |
|---|---|---|---|---|
| **ERROR** | Red | 🛑 | **YES** | Critical issues that prevent valid JSON or game launch |
| **WARNING** | Yellow | ⚠️ | **NO** | Adventure can save but won't play correctly |
| **INFO** | Blue | ℹ️ | **NO** | Optimization hints and best practices |

#### ERROR Level (Blocking)

Cannot save until resolved. Prevents fundamental corruption.

**Examples:**
- Duplicate entity IDs (Room 1 exists twice)
- Empty required fields (Room without name/ID)
- Invalid JSON structure (internal error)
- Malformed data types (string where number expected)

**Display:**
```
🛑 ERROR: Cannot save adventure
   • Room ID 5 is duplicated
     [Go to Duplicate] [Auto-Fix: Renumber]
   
   • Item 102: Name is required
     [Go to Item 102]
```

#### WARNING Level (Non-Blocking)

Can save, but adventure won't work correctly. Offers "Quick Fix" actions.

**Examples & Quick Fixes:**

```
⚠️ WARNING: Exit 'north' in Room 1 points to non-existent Room 99
   [Create Room 99] [Change Target to...] [Remove Exit]

⚠️ WARNING: Item 102 references state 'door_open' which doesn't exist
   [Create State] [Remove Reference] [Choose Existing State ▼]

⚠️ WARNING: Action moves player to Room 10, but Room 10 has no exits
   [Add Exit to Room 10] [Go to Room 10] [Ignore]

⚠️ WARNING: Container 201 contains Item 301, but Item 301 location is Room 5
   [Fix: Set Item 301 location to CONTAINER] [Remove from Container]

⚠️ WARNING: State 'gate_status' has 3 possible values but only 'open' is used
   [Review Usages] [Add Missing Logic] [Ignore]
```

#### INFO Level (Informational)

Everything works, just suggestions for improvement.

**Examples:**
```
ℹ️ INFO: Item 103 is in inventory but never used anywhere
   [Find Usage] [Add Item Usage] [Move to Room]

ℹ️ INFO: Room 7 has description over 500 characters (may be too long)
   [Review] [Split Description] [Ignore]

ℹ️ INFO: Consider adding aliases for 'rusty key' (e.g., 'key', 'old key')
   [Add Aliases] [Ignore]

ℹ️ INFO: 5 rooms have no comment field (good for team documentation)
   [Add Comments] [Ignore All]
```

### 6.2 Real-Time Validation

**When Validation Runs:**

| Trigger | Scope | Display |
|---|---|---|
| On field blur | Single field | Inline error border |
| On entity save | Full entity | Entity validation panel |
| On file save | Full adventure | Full validation dialog |
| Manual (Ctrl+Shift+V) | Full adventure | Detailed report |
| Background (5s after change) | Changed entities | Status bar count |

**Validation Display Locations:**

**1. Inline (Field Level):**
```
Name: [____________]  ← Red border if empty
      ⚠️ Name is required
```

**2. Navigation Tree:**
```
⊞ Rooms (12)
  ├─ 🏠 Starting Room (1)
  ├─ 🌲 Forest Path (2) ⚠️  ← Warning indicator
  └─ 🏰 Castle Gate (3)
```

**3. Validation Panel (Right Side):**
```
┌────────────────────────────────────────────────────┐
│ 🛑 Errors (1)  ⚠️ Warnings (4)  ℹ️ Info (7)        │ ← Tab bar
├────────────────────────────────────────────────────┤
│ 🛑 ERROR: Duplicate Room ID                        │
│    Room 'Forest Path' uses ID 2, but 'Dark Woods'  │
│    also uses ID 2                                  │
│    [Go to 'Forest Path'] [Go to 'Dark Woods']      │
│    [Auto-Fix: Renumber 'Dark Woods' to ID 13]      │
│                                                    │
│ ⚠️ WARNING: Exit to missing room                   │
│    Room 'Starting Room' exit 'north' → Room 99     │
│    (Room 99 doesn't exist)                         │
│    [Create Room 99] [Change Target] [Remove Exit]  │
│                                                    │
│ ⚠️ WARNING: Unused state value                     │
│    State 'door_status' value 'broken' is never set │
│    [Find Usages] [Remove Value] [Ignore]           │
│                                                    │
│ [Filter: All ▼] [Group By: Type ▼] [Export Report]│
└────────────────────────────────────────────────────┘
```

**4. Save Dialog (if warnings exist):**
```
╔═══════════════════════════════════════════════════╗
║ Save with Warnings?                                ║
╠═══════════════════════════════════════════════════╣
║                                                   ║
║ Your adventure has 4 warnings:                    ║
║                                                   ║
║ ⚠️ 1 exit points to non-existent room             ║
║ ⚠️ 2 items reference missing states               ║
║ ⚠️ 1 container has invalid item references        ║
║                                                   ║
║ The file will save, but the adventure may not     ║
║ play correctly until these are fixed.             ║
║                                                   ║
║ ☐ Don't show this again for warnings              ║
║                                                   ║
║         [Fix Issues] [Save Anyway] [Cancel]       ║
╚═══════════════════════════════════════════════════╝
```

### 6.3 Error Prevention Strategies

**1. Dropdowns for References**
```kotlin
// Instead of free text:
Exit Target: [99___________]  ❌

// Use dropdown:
Exit Target: [Starting Room (1) ▼]  ✓
             [Forest Path (2)      ]
             [Castle Gate (3)      ]
```

**2. Smart ID Suggestions**
```
New Room ID: [_____]
             Next free: 13  [Use 13]
```

**3. Dependency Checking**
```
Delete Room 5?
⚠️ Warning: 3 entities reference this room:
   • Exit 'north' in Room 1
   • ItemUsage for Key (102) in Room 3  
   • Action 'MoveTo' in Room 4

[Cancel] [Delete & Remove References] [Delete Anyway]
```

**4. Template Validation**
- Pre-validated templates for common patterns
- "Locked Door Wizard" creates consistent, valid structure
- Templates maintain validation invariants

**5. Undo Safety**
- All operations undoable (Ctrl+Z)
- Undo/Redo preserves validation state
- Mark clean/dirty state in title bar

---

## 7. Testing Integration

### 7.1 Test Configuration

```
╔═══════════════════════════════════════════════════╗
║ Test Settings                       [Save] [Test] ║
╠═══════════════════════════════════════════════════╣
║                                                   ║
║ Engine JAR Path:                                  ║
║ [C:\...\jbs-adventure-engine-1.0-SNAPSHOT.jar]    ║
║ [Browse...]                                       ║
║                                                   ║
║ Test Options:                                     ║
║ ☑ Console debug output                           ║
║ ☑ Enable file logging                            ║
║ ☐ Ignore action delays (faster testing)          ║
║                                                   ║
║ Language:                                         ║
║ [English (en) ▼]                                  ║
║                                                   ║
║ Auto-save before test: [Yes ▼]                    ║
║                                                   ║
║ Additional JVM args:                              ║
║ [_____________________________________________]   ║
║                                                   ║
║                              [Cancel] [Save] [Test]║
╚═══════════════════════════════════════════════════╝
```

### 7.2 Test Execution

**When user clicks "Test" (F5):**

1. Validate current adventure
2. Show validation results if errors exist
3. Auto-save (if configured)
4. Write temporary data.json if needed
5. Launch engine in separate process
6. Open console window (native terminal)
7. Monitor process status
8. Allow editing while testing
9. Show "Stop Test" button
10. Clean up on exit

**Console Integration:**

```powershell
# Windows
Start-Process powershell -ArgumentList "-NoExit", "-Command", `
  "java -jar engine.jar --data temp.json --consoleLog --debug"
```

```bash
# Linux/macOS
gnome-terminal -- java -jar engine.jar --data temp.json --consoleLog --debug
```

---

## 8. Advanced Features

### 8.1 Templates & Wizards

**Common Templates:**
- Locked door with key
- Container with treasure
- Quest item sequence
- NPC interaction point (for future)
- Puzzle room setup

**Wizard Example: "Add Locked Door"**
```
Step 1: Select rooms to connect
Step 2: Choose door properties (locked/open)
Step 3: Select or create key item
Step 4: Configure key consumption
Step 5: Add opening actions (optional)
→ Creates: Exit, Key item, ItemUsage, Actions
```

### 8.2 Import/Export

**Partial Export:**
- Export selected rooms with all dependencies
- Export item collection (e.g., all keys)
- Export state definitions
- Export language strings template

**Use Cases:**
- Share puzzle designs between projects
- Create item libraries
- Backup specific game sections
- Collaborate on different areas

### 8.3 Search & Replace

```
╔═══════════════════════════════════════════════════╗
║ Find & Replace                          [Close] ║║
╠═══════════════════════════════════════════════════╣
║                                                   ║
║ Find:     [golden_____________________]           ║
║ Replace:  [silver_____________________]           ║
║                                                   ║
║ Search in:                                        ║
║ ☑ Room names/descriptions                        ║
║ ☑ Item names/descriptions                        ║
║ ☑ State keys/values                              ║
║ ☑ Action descriptions                            ║
║ ☐ Comments                                       ║
║                                                   ║
║ Options:                                          ║
║ ☐ Match case                                     ║
║ ☑ Whole words only                               ║
║                                                   ║
║ Results: 5 matches found                          ║
║ ┌───────────────────────────────────────────────┐ ║
║ │ Room 1: "golden door"            [Replace]    │ ║
║ │ Item 102: "golden key" (name)    [Replace]    │ ║
║ │ Item 102: "a golden key..." (desc)[Replace]   │ ║
║ │ State 'key_color': value "golden"[Replace]    │ ║
║ │ Action: "...find the golden..."  [Replace]    │ ║
║ └───────────────────────────────────────────────┘ ║
║                                                   ║
║           [Replace All] [Replace Selected] [Close]║
╚═══════════════════════════════════════════════════╝
```

---

## 9. Technical Considerations

### 9.1 Data Model Synchronization

**Editor uses engine's shared model layer:**
- `shared/` module contains all data classes
- Both `engine/` and `editor/` depend on `shared/`
- Same serializers for consistency
- Same validators for compatibility

### 9.2 Undo/Redo System

**Command Pattern:**
```kotlin
interface EditorCommand {
    fun execute()
    fun undo()
    fun getDescription(): String
}

class AddRoomCommand(val room: Room) : EditorCommand {
    override fun execute() { /* add room */ }
    override fun undo() { /* remove room */ }
    override fun getDescription() = "Add room '${room.name}'"
}

class CommandStack {
    private val undoStack = mutableListOf<EditorCommand>()
    private val redoStack = mutableListOf<EditorCommand>()
    
    fun execute(command: EditorCommand) { /* ... */ }
    fun undo() { /* ... */ }
    fun redo() { /* ... */ }
}
```

### 9.3 State Management

**Architecture Pattern:**
```kotlin
// ViewModel per major component
class AdventureViewModel {
    val adventureDataState = mutableStateOf<GameData?>(null)
    val validationErrors = mutableStateOf<List<ValidationError>>(emptyList())
    val selectedEntity = mutableStateOf<Any?>(null)
    
    fun loadAdventure(path: String) { /* ... */ }
    fun saveAdventure() { /* ... */ }
    fun validateAdventure() { /* ... */ }
}

class RoomEditorViewModel(val room: Room) {
    val name = mutableStateOf(room.name)
    val description = mutableStateOf(room.description)
    val exits = mutableStateOf(room.exits)
    
    fun saveChanges() { /* ... */ }
}
```

### 9.4 Performance Optimization

**Lazy Loading:**
- Load tree nodes on expand
- Defer validation until requested
- Cache rendered components

**Efficient Updates:**
- Use Compose's smart recomposition
- Minimize state changes
- Batch validation runs

**Large Adventures:**
- Virtual scrolling for long lists
- Pagination for large entity counts
- Search indexing for quick lookup

---

## 10. UI Mockups

### 10.1 Main Window (Light Theme)

```
┌─────────────────────────────────────────────────────────────┐
│ ☰ JB's Adventure Editor  │ File Edit View Tools Help  [🔍][▶]│
├───────────────┬─────────────────────────────────────────────┤
│ Adventure     │ ╔════════════════════════════════════════╗  │
│ □ Info        │ ║ Room Editor: Forest Path (2)          ║  │
│               │ ╠════════════════════════════════════════╣  │
│ □ States (3)  │ ║ [General] [Exits] [Item Usages] [...]  ║  │
│               │ ╟────────────────────────────────────────╢  │
│ ▼ Rooms (12)  │ ║ Name: [Forest Path_________]  ⚙️       ║  │
│  📍 Start (1) │ ║                                        ║  │
│  📍 Forest(2) │ ║ Description:                           ║  │
│  📍 Gate (3)  │ ║ ┌────────────────────────────────────┐ ║  │
│  ...          │ ║ │A narrow path winds through dense   │ ║  │
│               │ ║ │forest. Sunlight barely penetrates │ ║  │
│ ▼ Items (25)  │ ║ │the thick canopy above...           │ ║  │
│  🗝️ Keys (3)  │ ║ └────────────────────────────────────┘ ║  │
│   - Rusty(101)│ ║                                        ║  │
│   - Gold (102)│ ║ [Save Changes] [Cancel] [Delete Room]  ║  │
│  📦 Chests(5) │ ╚════════════════════════════════════════╝  │
│  ...          │                                             │
├───────────────┴─────────────────────────────────────────────┤
│ ✓ Ready  │ "The Lost Crown"  │ 12 Rooms, 25 Items │ [⚠️ 2]│
└─────────────────────────────────────────────────────────────┘
```

### 10.2 Action Builder (Detailed)

```
╔═══════════════════════════════════════════════════════════╗
║ Configure Action: Change State          [✓ Save] [✗ Cancel]║
╠═══════════════════════════════════════════════════════════╣
║                                                           ║
║ Action Type: [ChangeState ▼] [?]                          ║
║ ┌─────────────────────────────────────────────────────┐   ║
║ │ This action modifies a global game state variable. │   ║
║ └─────────────────────────────────────────────────────┘   ║
║                                                           ║
║ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓   ║
║ ┃ State Configuration                                 ┃   ║
║ ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫   ║
║ ┃                                                     ┃   ║
║ ┃ State to Change: [gate_state ▼] [📝 Edit State]    ┃   ║
║ ┃                                                     ┃   ║
║ ┃ Current Value: closed                              ┃   ║
║ ┃ New Value:     [open ▼]                            ┃   ║
║ ┃                                                     ┃   ║
║ ┃ Available values: open, closed, locked, broken     ┃   ║
║ ┃                                                     ┃   ║
║ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛   ║
║                                                           ║
║ Description (shown to player when action executes):       ║
║ ┌─────────────────────────────────────────────────────┐   ║
║ │ With a loud creak, the ancient gate slowly swings  │   ║
║ │ open, revealing a dark passage beyond.             │   ║
║ └─────────────────────────────────────────────────────┘   ║
║ ☑ Show description to player                              ║
║                                                           ║
║ Timing:                                                   ║
║ Delay before execution: [____] ms (0 = immediate)         ║
║                                                           ║
║ Internal Note (not shown in game):                        ║
║ [This opens the gate for chapter 1 finale____]            ║
║                                                           ║
║ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ ║
║                                                           ║
║ Preconditions (must ALL be true):       [+ Add Condition] ║
║ ┌─────────────────────────────────────────────────────┐   ║
║ │                                                     │   ║
║ │ 1. Player has item: Golden Key (102)               │   ║
║ │    [✏️ Edit] [🗑️ Delete]                            │   ║
║ │                                                     │   ║
║ │ 2. State 'quest_stage' equals 'ready_for_gate'     │   ║
║ │    [✏️ Edit] [🗑️ Delete]                            │   ║
║ │                                                     │   ║
║ └─────────────────────────────────────────────────────┘   ║
║                                                           ║
║ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ ║
║                                                           ║
║ Preview:                                                  ║
║ When all preconditions are met, this action will:        ║
║  • Change state 'gate_state' from 'closed' to 'open'      ║
║  • Display the description to the player                  ║
║  • Continue to next action in sequence (if any)           ║
║                                                           ║
║             [🧪 Test in Debugger] [✓ Save] [✗ Cancel]     ║
╚═══════════════════════════════════════════════════════════╝
```

### 10.3 Room Graph View (Visual)

```
┌───────────────────────────────────────────────────────────┐
│ Room Graph View              [Zoom: 100%] [Reset] [Export]│
├───────────────────────────────────────────────────────────┤
│                                                           │
│         ┌──────────────────┐                              │
│    ┌────┤  Dark Corridor   │                              │
│    │    │    (Room 5)      │                              │
│    │    └──────────────────┘                              │
│    │              │                                        │
│  west           south                                     │
│    │              │                                        │
│    │              ▼                                        │
│    │    ┌──────────────────┐       ┌──────────────────┐   │
│    │    │   Forest Path    │       │  Hidden Cave     │   │
│    └───►│    (Room 2)      ├──────►│   (Room 6)       │   │
│         │   ⚠️ 1 warning   │ east  │   [Hidden]       │   │
│         └──────────────────┘       └──────────────────┘   │
│                  │                                        │
│                south                                      │
│                  │                                        │
│                  ▼                                        │
│         ┌──────────────────┐                              │
│         │  Starting Room   │◄──────┐                      │
│         │    (Room 1)      │       │                      │
│         │  [Current Edit]  │     north                    │
│         └──────────────────┘       │                      │
│                  │                 │                      │
│                east         ┌──────────────────┐          │
│                  │          │  Castle Gate     │          │
│                  └─────────►│   (Room 3)       │          │
│                             │   🔒 Locked      │          │
│                             └──────────────────┘          │
│                                      │                    │
│                                    north                  │
│                                      │                    │
│                                      ▼                    │
│                             ┌──────────────────┐          │
│                             │   Courtyard      │          │
│                             │   (Room 4)       │          │
│                             └──────────────────┘          │
│                                                           │
│ Legend:  ───► Open  ═══► Locked  ┈┈┈► Hidden  ▓▓▓► Blocked│
│          ⚠️ Warning   🔒 Requires key   [Selected]        │
│                                                           │
│ Selected: Starting Room (1)               [Edit] [Delete] │
└───────────────────────────────────────────────────────────┘
```

**Interaction:**
- Click room → Select for editing
- Double-click → Open editor panel
- Right-click → Context menu (Edit/Delete/Duplicate)
- Drag room → Reposition in graph
- Hover on exit arrow → Show exit details tooltip
- Click exit arrow → Edit exit properties
- Zoom with mouse wheel
- Pan by dragging background

---

## 11. Implementation Phases

### Phase 1: Core Editor (MVP)
**Duration:** 4-6 weeks

**Features:**
- Basic project structure (Gradle multi-module)
- Main window with navigation tree
- Room editor (General tab only)
- Item editor (General tab only)  
- Simple exits (direction + target only)
- Save/Load data.json
- Basic validation

**Goal:** Can create minimal playable adventure

### Phase 2: Actions & Logic
**Duration:** 3-4 weeks

**Features:**
- Action builder dialog
- All action types (MoveTo, ChangeState, etc.)
- Precondition builder
- Item usages editor
- State editor
- Action lists (onExamine, onUse, etc.)

**Goal:** Full logical complexity supported

### Phase 3: Containers & Advanced Features
**Duration:** 2-3 weeks

**Features:**
- Container support
- Open/Lock/Unlock editing
- Key consumption settings
- Exit detailed configuration
- Name/alias management with i18n

**Goal:** All engine features supported

### Phase 4: Visual Enhancements
**Duration:** 2-3 weeks

**Features:**
- Room graph view
- Item locations view
- State dependencies view
- Drag-and-drop where applicable
- Search & replace
- Undo/redo system

**Goal:** Professional user experience

### Phase 5: Testing & Polish
**Duration:** 2-3 weeks

**Features:**
- Integrated game testing
- Templates & wizards
- Import/Export functionality
- Comprehensive error handling
- Documentation & help system
- Keyboard shortcuts

**Goal:** Production-ready application

---

## 12. Design Decisions (RESOLVED)

### 12.1 UI & UX Decisions

#### **Color Theme: ✅ System Theme with Manual Override**
- **Default**: Follow system theme (Light/Dark)
- **User Option**: Manual selection in Preferences (Light/Dark/System)
- **Implementation**: Use Material 3 with dynamic color schemes

#### **Entity ID Management: ✅ Auto-Increment with Manual Override**
- **Default**: Auto-increment to next available ID
- **Manual Entry**: User can enter specific ID
- **Duplicate Prevention**: Validation error if ID already exists
- **Suggestion**: When duplicate detected, suggest next free ID with option to accept or choose different
- **Independent Ranges**: Rooms and Items have separate ID spaces (no range segregation)

**Example Flow:**
```
User creates new Room → Auto-assigns ID 5
User edits ID to 3 → Validation: "ID 3 already exists. Next free: 6" [Use 6] [Choose Other]
```

#### **Validation Strictness: ✅ Three-Level System with Smart Saving**

**ERROR (Red, Blocking):**
- Duplicate entity IDs
- Completely empty required fields (name, ID)
- Invalid JSON structure (internal error)
- **Action**: Cannot save until fixed

**WARNING (Yellow, Non-Blocking):**
- Exit points to non-existent room
- Item references missing state
- Action parameters reference missing entities
- Circular dependencies
- **Action**: Can save, but adventure not playable. Shows "Quick Fix" buttons where possible

**INFO (Blue, Informational):**
- Optimization suggestions
- Best practice hints
- Unused entities
- **Action**: Fully functional, just suggestions

**Quick Fix Examples:**
```
⚠️ WARNING: Exit 'north' in Room 1 points to non-existent Room 99
   [Create Room 99] [Change Target] [Remove Exit]

⚠️ WARNING: Item 102 references state 'door_open' which doesn't exist
   [Create State] [Remove Reference] [Choose Different State]
```

#### **Default Values: ✅ Class Defaults from Engine**
- Use defaults from model classes (e.g., `usable = true`, `carriable = false`)
- Consistent with engine deserialization behavior
- No user-configurable defaults in MVP (future enhancement)

#### **Graph Layout: ✅ Hybrid Auto + Manual**
- **MVP**: Simple hierarchical layout (distance from starting room)
- **Post-MVP**: Force-directed physics layout
- **Always**: Manual drag-to-reposition with snap-to-grid
- **Pan & Zoom**: Mouse wheel zoom, drag background to pan
- **Persistence**: Save positions in separate `.meta` file (not in data.json)

### 12.2 Technical Decisions

#### **Module Structure: ✅ Three-Module Maven + Gradle Hybrid**

```
model-lib (Maven)  ← shared data classes
    ↑          ↑
    │          │
engine      editor
(Maven)    (Gradle)
```

**Benefits:**
- Engine keeps existing Maven workflow
- Editor gets Gradle's superior Compose support
- Perfect code sharing via model-lib artifact
- Selective builds: `mvn -pl engine` or `gradle :editor:build`

**Build Configuration:**
```xml
<!-- model-lib/pom.xml -->
<groupId>net.daddldiddl</groupId>
<artifactId>jbs-adventure-model</artifactId>
<version>1.0-SNAPSHOT</version>

<!-- engine/pom.xml -->
<dependencies>
    <dependency>
        <groupId>net.daddldiddl</groupId>
        <artifactId>jbs-adventure-model</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

```kotlin
// editor/build.gradle.kts
dependencies {
    implementation(files("../model-lib/target/jbs-adventure-model-1.0-SNAPSHOT.jar"))
    implementation(compose.desktop.currentOs)
}
```

#### **Undo/Redo Granularity: ✅ Per-Save-Point**
- Save creates checkpoint in undo stack
- All changes between saves = one undo unit
- Simpler implementation, prevents undo complexity
- Future: Could add per-entity granularity if needed

#### **Large Adventure Performance: ✅ Desktop-First, Optimized for Hundreds**
- Target: 100-300 rooms, 200-500 items smooth performance
- Virtual scrolling for lists > 100 items
- Lazy tree expansion for navigation
- Validation runs on background thread
- Graph view: Culling for off-screen nodes
- No lazy loading initially (all data in memory)

#### **Testing Integration: ✅ Packaged Engine JAR**
- Editor bundles pre-built engine JAR in distribution
- Located in editor's resources or target folder
- Path configurable in preferences (for custom engine builds)
- Editor does NOT build engine on-demand
- Developer workflow: Build model-lib & engine first, then editor

**Distribution:**
```
editor-dist/
├── jbs-adventure-editor.jar   (fat JAR with all editor code)
├── lib/
│   ├── jbs-adventure-model-1.0-SNAPSHOT.jar
│   └── jbs-adventure-engine-1.0-SNAPSHOT.jar  ← Used for testing
└── README.txt
```

#### **File Format Versioning: ✅ Version Tags with Migration Framework**

**Data Format:**
```json
{
  "formatVersion": "1.0",
  "engineVersion": "1.0-SNAPSHOT",
  "title": "My Adventure",
  "Rooms": { ... }
}
```

**Language Format:**
```json
{
  "formatVersion": "1.0",
  "languageKey": "en",
  "directions": { ... }
}
```

**Version Handling:**
- **Load**: Check formatVersion, show warning if mismatch
- **MVP**: No auto-migration, just warning dialog
- **Post-MVP**: Migration helper per version jump
- **Tolerance**: `ignoreUnknownKeys = true` for forward compatibility

**Migration Framework (Future):**
```kotlin
object DataMigrator {
    fun needsMigration(data: JsonObject): Boolean {
        val version = data["formatVersion"]?.jsonPrimitive?.content
        return version != CURRENT_VERSION
    }
    
    fun migrate(data: JsonObject, fromVersion: String): GameData {
        var migrated = data
        // Chain migrations: 0.9 -> 1.0 -> 1.1 -> current
        if (fromVersion < "1.0") migrated = migrateFrom09(migrated)
        if (fromVersion < "1.1") migrated = migrateFrom10(migrated)
        return Json.decodeFromJsonElement(migrated)
    }
}
```

### 12.3 Additional Decisions

#### **Native Packaging: ✅ JAR-Only Initially**
- MVP: Distribute as executable JAR
- Post-MVP: Native installers (MSI/DMG/DEB) via Gradle Compose plugin
- Run: `java -jar jbs-adventure-editor.jar`

#### **Localization: ✅ German + English**
- Editor UI strings in `.properties` files
- English (en) as default
- German (de) with full translation
- User selects in Preferences
- Adventure content remains single-language per file

**Implementation:**
```kotlin
// i18n/messages_en.properties
menu.file=File
menu.edit=Edit
editor.room.title=Room Editor
validation.error.duplicateId=Duplicate ID: {0}

// i18n/messages_de.properties
menu.file=Datei
menu.edit=Bearbeiten
editor.room.title=Raum-Editor
validation.error.duplicateId=Doppelte ID: {0}
```

#### **Keyboard Shortcuts: ✅ Standard IDE Shortcuts**
```
Ctrl+N         New Adventure
Ctrl+O         Open Adventure
Ctrl+S         Save
Ctrl+Shift+S   Save As
Ctrl+Z         Undo
Ctrl+Y         Redo
Ctrl+F         Find/Search
Ctrl+G         Show Graph View
F5             Test in Engine
Ctrl+Shift+V   Validate Adventure
Escape         Close Dialog/Cancel
Delete         Delete Selected Entity
Ctrl+D         Duplicate Selected Entity
```

#### **Action Builder View: ✅ List + Detail View**
- Left: Scrollable list of all actions (compact, one line each)
- Right: Detailed editor for selected action
- Drag-to-reorder in list
- Quick preview tooltip on hover

**Example:**
```
┌─────────────────────┬─────────────────────────────┐
│ Actions (3)         │ Selected: Action 2          │
│                     │                             │
│ 1. Message: "You... │ Type: [ChangeState ▼]       │
│ 2. ChangeState: ... │ State: [gate_state ▼]       │
│ 3. MoveTo: Room 5   │ New Value: [open ▼]         │
│    [+ Add Action]   │ Description:                │
│                     │ ┌─────────────────────────┐ │
│                     │ │The gate opens slowly... │ │
│                     │ └─────────────────────────┘ │
│                     │ [Preconditions: 2]          │
│                     │ [Save] [Delete]             │
└─────────────────────┴─────────────────────────────┘
```

---

## 13. Success Criteria

### 13.1 Functional Requirements

- [ ] Can create complete adventure without touching JSON
- [ ] All engine features are editable
- [ ] Real-time validation with helpful errors
- [ ] Can test game directly from editor
- [ ] Saves valid data.json files
- [ ] Loads existing data.json files correctly

### 13.2 Usability Requirements

- [ ] Intuitive UI for non-programmers
- [ ] Clear visual feedback for all actions
- [ ] Helpful error messages with fix suggestions
- [ ] Undo/redo for all operations
- [ ] Responsive UI (no lag on typical adventures)
- [ ] Keyboard shortcuts for common operations

### 13.3 Quality Requirements

- [ ] No data loss on crashes (auto-save)
- [ ] Consistent with engine behavior (same validators)
- [ ] Cross-platform compatibility (Windows/Linux/macOS)
- [ ] Comprehensive documentation
- [ ] Unit tests for critical logic
- [ ] Performance acceptable for 100+ room adventures

---

## 14. Future Enhancements

### Post-V1.0 Features

1. **Multi-Language Support:**
   - Edit multiple language files side-by-side
   - Translation workflow
   - Missing translation detection

2. **Collaboration Features:**
   - Git integration
   - Merge conflict resolution for data.json
   - Multi-user editing (future)

3. **Asset Management:**
   - Link images to rooms/items (for future engine support)
   - Sound effect references
   - External resource tracking

4. **Scripting Extension:**
   - Custom action types via scripts
   - Lua/JavaScript for advanced logic
   - Plugin system for community extensions

5. **Story Flowchart:**
   - Visual story progression
   - Critical path highlighting
   - Playthrough simulation

6. **Analytics:**
   - Complexity metrics
   - Playability estimates
   - Dead-end detection
   - Unreachable content warnings

7. **AI Assistant:**
   - Generate room descriptions
   - Suggest action sequences
   - Balance check (difficulty estimation)

---

## 15. Appendix: Technology Examples

### 15.1 Build System Configuration

#### Parent Maven POM (root)

```xml
<!-- pom.xml -->
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>net.daddldiddl</groupId>
    <artifactId>jbs-adventure-parent</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    
    <modules>
        <module>model-lib</module>
        <module>engine</module>
    </modules>
    
    <properties>
        <kotlin.version>2.3.21</kotlin.version>
        <java.version>21</java.version>
        <serialization.version>1.11.0</serialization.version>
    </properties>
</project>
```

#### Model-Lib Maven POM

```xml
<!-- model-lib/pom.xml -->
<project>
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>net.daddldiddl</groupId>
        <artifactId>jbs-adventure-parent</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    
    <artifactId>jbs-adventure-model</artifactId>
    <packaging>jar</packaging>
    
    <dependencies>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib</artifactId>
            <version>${kotlin.version}</version>
        </dependency>
        <dependency>
            <groupId>org.jetbrains.kotlinx</groupId>
            <artifactId>kotlinx-serialization-json</artifactId>
            <version>${serialization.version}</version>
        </dependency>
    </dependencies>
    
    <build>
        <sourceDirectory>src/main/kotlin</sourceDirectory>
        <plugins>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>${kotlin.version}</version>
                <configuration>
                    <jvmTarget>${java.version}</jvmTarget>
                    <compilerPlugins>
                        <plugin>kotlinx-serialization</plugin>
                    </compilerPlugins>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>org.jetbrains.kotlin</groupId>
                        <artifactId>kotlin-maven-serialization</artifactId>
                        <version>${kotlin.version}</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>
</project>
```

#### Engine Maven POM (updated)

```xml
<!-- engine/pom.xml -->
<project>
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>net.daddldiddl</groupId>
        <artifactId>jbs-adventure-parent</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    
    <artifactId>jbs-adventure-engine</artifactId>
    <packaging>jar</packaging>
    
    <dependencies>
        <!-- Depend on model-lib -->
        <dependency>
            <groupId>net.daddldiddl</groupId>
            <artifactId>jbs-adventure-model</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
    
    <build>
        <sourceDirectory>src/main/kotlin</sourceDirectory>
        <plugins>
            <!-- Kotlin compiler plugin -->
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <version>${kotlin.version}</version>
            </plugin>
            
            <!-- Fat JAR with dependencies -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.8.0</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>net.daddldiddl.jbsadventure.MainKt</mainClass>
                        </manifest>
                    </archive>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

#### Editor Gradle Configuration

```kotlin
// editor/settings.gradle.kts
rootProject.name = "jbs-adventure-editor"
```

```kotlin
// editor/build.gradle.kts
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.3.21"
    id("org.jetbrains.compose") version "1.6.0"
    kotlin("plugin.serialization") version "2.3.21"
}

group = "net.daddldiddl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Depend on model-lib JAR from Maven build
    implementation(files("../model-lib/target/jbs-adventure-model-1.0-SNAPSHOT.jar"))
    
    // Compose Desktop
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    
    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    
    // Coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")
}

compose.desktop {
    application {
        mainClass = "net.daddldiddl.jbsadventure.editor.MainKt"
        
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "JBs Adventure Editor"
            packageVersion = "1.0.0"
            description = "Visual editor for JB's Adventure Engine"
            copyright = "© 2026 Jochen Brinkmann"
            vendor = "daddldiddl.net"
            
            windows {
                iconFile.set(project.file("src/main/resources/icons/app-icon.ico"))
            }
            linux {
                iconFile.set(project.file("src/main/resources/icons/app-icon.png"))
            }
            macOS {
                iconFile.set(project.file("src/main/resources/icons/app-icon.icns"))
            }
        }
    }
}

// Custom task: Copy engine JAR to editor resources
tasks.register<Copy>("copyEngineJar") {
    dependsOn(":engine:build") // Would need to invoke Maven separately
    from("../engine/target/jbs-adventure-engine-1.0-SNAPSHOT-jar-with-dependencies.jar")
    into("src/main/resources/engine")
    rename { "jbs-adventure-engine.jar" }
}

tasks.named("processResources") {
    // Don't auto-depend, requires manual Maven build first
    // dependsOn("copyEngineJar")
}
```

#### Build Workflow

**1. Full Clean Build:**
```bash
# Step 1: Build model-lib (Maven)
cd model-lib
mvn clean install

# Step 2: Build engine (Maven, depends on model-lib)
cd ../engine
mvn clean package

# Step 3: Build editor (Gradle, uses model-lib JAR)
cd ../editor
gradle build

# Or use parent POM for model + engine:
cd ..
mvn clean package  # Builds model-lib and engine
cd editor
gradle build       # Builds editor
```

**2. Build Only Engine:**
```bash
mvn -pl engine -am package
# -am = also make dependencies (includes model-lib)
```

**3. Build Only Editor:**
```bash
cd editor
gradle build
# Assumes model-lib JAR already exists at ../model-lib/target/
```

**4. Development Workflow:**
```bash
# After changes to model-lib:
cd model-lib && mvn install && cd ../editor && gradle build

# After changes to engine only:
cd engine && mvn package

# After changes to editor only:
cd editor && gradle build
```

**5. Run Editor:**
```bash
cd editor
gradle run
# Or after build:
java -jar build/compose/jars/jbs-adventure-editor-1.0-SNAPSHOT.jar
```

### 15.2 Jetpack Compose Desktop Sample

```kotlin
@Composable
fun RoomEditorPanel(room: Room, onSave: (Room) -> Unit) {
    var name by remember { mutableStateOf(room.name) }
    var description by remember { mutableStateOf(room.description) }
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Room Editor", style = MaterialTheme.typography.h5)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Room Name") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            maxLines = 5
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = { onSave(room.copy(name = name, description = description)) }) {
            Text("Save Changes")
        }
    }
}
```

### 15.2 Build Configuration Sample

```kotlin
// build.gradle.kts (editor module)
plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version "1.6.0"
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
}

compose.desktop {
    application {
        mainClass = "net.daddldiddl.jbsadventure.editor.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "JBs Adventure Editor"
            packageVersion = "1.0.0"
        }
    }
}
```

---

## 16. Conclusion & Summary

The JB's Adventure Editor will transform the adventure creation process from manual JSON editing to an intuitive visual experience. By leveraging Jetpack Compose for Desktop, we can deliver a modern, cross-platform tool that makes adventure game development accessible to non-programmers while providing power users with advanced features.

### Key Design Decisions (FINAL)

✅ **Architecture:**
- Three-module structure: `model-lib` (Maven) + `engine` (Maven) + `editor` (Gradle)
- Direct use of engine classes via shared model-lib
- Independent build targets with selective compilation

✅ **User Experience:**
- System theme with manual Light/Dark override
- Auto-increment IDs with manual entry allowed
- Three-level validation (Error blocks save / Warning allows save / Info suggests)
- Quick Fix buttons for warnings
- Auto-save every 3 minutes with recovery dialog

✅ **Versioning:**
- Format version tags in data.json and language files
- Load tolerance with warnings on version mismatch
- Migration framework planned for future engine updates

✅ **Build System:**
- Maven for existing engine (no disruption)
- Gradle for editor (optimal Compose support)
- JAR distribution initially, native packages post-MVP

✅ **Localization:**
- German + English editor UI
- Separate .properties files for strings

✅ **Technical:**
- Per-save-point undo/redo
- Target: 100-300 rooms, 200-500 items smooth performance
- Bundled engine JAR for testing
- Simple hierarchical graph layout (MVP), force-directed later

### Implementation Roadmap

**Phase 1: Core Editor (4-6 weeks)** → MVP with basic room/item editing
**Phase 2: Actions & Logic (3-4 weeks)** → Full action system support  
**Phase 3: Containers & Advanced (2-3 weeks)** → Complete engine feature parity
**Phase 4: Visual Enhancements (2-3 weeks)** → Graph views, polish
**Phase 5: Testing & Polish (2-3 weeks)** → Production ready

**Total: ~15-20 weeks to v1.0**

### Next Steps

**Immediate (Week 1-2):**
1. ✅ Design document approved
2. Set up multi-module project structure
3. Refactor engine: Move model classes to model-lib
4. Create basic Gradle + Maven integration
5. Prototype main window with Compose

**Phase 1 Start (Week 3):**
1. Implement navigation tree
2. Basic room editor (name, description, ID)
3. Simple item editor
4. Save/Load data.json
5. Basic validation framework

**Development Environment Setup:**
```bash
# Project structure creation
mkdir model-lib engine editor
cd model-lib && mvn archetype:generate ...
cd ../editor && gradle init ...

# First build
mvn clean install         # Builds model-lib
mvn -pl engine package    # Builds engine with fat JAR
cd editor && gradle build # Builds editor
```

### Success Metrics

**MVP Success Criteria:**
- [ ] Can create playable adventure without editing JSON
- [ ] All engine features are editable
- [ ] Real-time validation with helpful errors
- [ ] Can test game directly from editor (F5)
- [ ] Saves valid data.json readable by engine
- [ ] Loads existing adventures without data loss

**v1.0 Success Criteria:**
- [ ] All MVP criteria plus:
- [ ] Visual room graph with auto-layout
- [ ] German + English localization
- [ ] Auto-save with crash recovery
- [ ] Undo/redo fully functional
- [ ] Comprehensive Quick Fix actions
- [ ] Performance smooth for 200+ entities

### Risk Assessment

| Risk | Probability | Impact | Mitigation |
|---|---|---|---|
| Maven/Gradle integration issues | Medium | Medium | Test early, document workarounds |
| Compose Desktop learning curve | Medium | Low | Start with simple UI, iterate |
| Engine refactoring breaks compatibility | Low | High | Comprehensive testing, version tags |
| Performance issues with large adventures | Low | Medium | Profile early, optimize incrementally |
| Complex action builder UX | Medium | Medium | User testing, iterative design |

### Open for Future Consideration

These are explicitly **not in scope** for v1.0 but worth tracking:

- Multi-language adventure support (edit multiple .json per language)
- Git integration and team collaboration
- Visual scripting for complex logic
- Asset management (images, sounds)
- AI-assisted content generation
- Story flowchart view
- Playthrough analytics and testing automation

---

**Document Version:** 2.0  
**Last Updated:** 2026-06-08  
**Status:** ✅ Approved - Ready for Implementation  

**Approved By:** Project Owner  
**Next Review:** After Phase 1 completion or major design changes









