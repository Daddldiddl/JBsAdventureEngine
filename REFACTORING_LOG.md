# Refactoring Log - Model-Lib Separation

**Date:** 2026-06-09  
**Goal:** Remove global singleton dependencies (LOG, CONSOLE, DATA, LANG) from model classes

## Strategy

### Chosen Pattern: Companion Objects
After evaluating explicit parameter passing vs. companion objects, the companion object approach was chosen:
- `LanguageData.current` – set at startup / on language switch
- `GameData.current` – set after loading game data

**Rationale:** `LanguageData.current` and `GameData.current` are **self-references within model-lib**
(not engine imports), so they satisfy the module separation goal while keeping method signatures clean.
Language switching is trivially supported: `GlobalContext.switchLanguage(newLang)` updates `LanguageData.current`.

### 1. LOG Removal
**Affected:**
- GameData.kt
- LanguageData.kt
- Action.kt
- Precondition.kt
- DataValidator.kt
- GameLoader.kt

**Solution:** 
- Remove debug/info logging (not needed in model)
- Keep error handling but throw exceptions instead
- Move logging to caller (engine layer)

### 2. CONSOLE Removal
**Affected:**
- Action.kt (outputDescription method)

**Solution:**
- Return messages instead of printing
- Caller decides how to display

### 3. DATA Removal
**Affected:**
- Container.kt
- Item.kt
- OpenLockEnabledEntity.kt

**Solution:**
- Already mostly passed as parameter
- Make explicit: methods take GameData parameter

### 4. LANG Removal  
**Affected:**
- LanguageData.kt
- Container.kt
- Exit.kt
- Item.kt
- Name.kt
- OpenLockEnabledEntity.kt
- Room.kt
- ExitSerializer.kt

**Solution:**
- Inject LanguageData as constructor parameter where needed
- Use explicit parameter passing for formatting methods

## Implementation Order

1. ✅ Create strategy document
2. ✅ Infrastructure: `ILogger` + `IConsole` interfaces, `ConsoleColor` extracted, `GlobalContext` decoupled from engine types, `SimpleFileLog` implements `ILogger`, `ConsoleOutput` implements `IConsole`, `Main.kt` migrated to `GlobalContext`
3. ✅ Fix LanguageData.kt (remove LANG self-reference, remove LOG dependency)
4. ✅ Fix Name.kt & NamedEntity (add lang: LanguageData parameter to all LANG-dependent methods)
5. ✅ **Redesign: Switch to companion objects** – reverted explicit `lang` parameters; `LanguageData.current` and `GameData.current` companion objects introduced; `GlobalContext` simplified; `DATA`/`LANG` global accessors now delegate to companions; model classes (Name, OpenLockEnabledEntity, Exit, Item, Container, Room) use companions directly; `GameData.setCurrentStateValue` throws `IllegalArgumentException` instead of LOG.warn; `Game.kt` reverted to original clean call sites
6. ⏳ Fix Action.kt (remove LOG, CONSOLE)
7. ⏳ Fix Precondition.kt (remove LOG)
8. ⏳ Fix GameData.kt (LOG already removed in Step 5)
9. ⏳ Fix tools/*.kt (DataValidator, GameLoader - remove LOG)
10. ⏳ Fix serializers (if needed)
11. ⏳ Update engine code (Main.kt, Game.kt) – already clean
12. ⏳ Test build
13. ⏳ Split into modules
5. ⏳ Fix model/*.kt (Item, Container, Exit, Room, OpenLockEnabledEntity)
6. ⏳ Fix Action.kt (remove LOG, CONSOLE)
7. ⏳ Fix Precondition.kt (remove LOG)
8. ⏳ Fix GameData.kt (remove LOG)
9. ⏳ Fix tools/*.kt (DataValidator, GameLoader - remove LOG)
10. ⏳ Fix serializers (ExitSerializer - add LANG parameter)
11. ⏳ Update engine code (Main.kt, Game.kt) to pass parameters
12. ⏳ Test build
13. ⏳ Split into modules

### Name.kt & NamedEntity (2026-06-09 – Step 4)
- Removed `import net.daddldiddl.jbsadventure.LANG`
- `Name` data class: changed `genderKey` default from `LANG.defaultPronoun.genderKey` to `Keys.Pronouns.defaultDefaultPronounGroupKey` (constant, no runtime dependency)
- Removed secondary constructor that used LANG
- All LANG-dependent `NamedEntity` interface methods now take explicit `lang: LanguageData` parameter:
  `getArticle`, `getIndefiniteName`, `getDefiniteName`, `getPronounSubject`, `getPronounObject`,
  `getPossessiveAdjective`, `getPossessiveNoun`, `getStateMessage`, `getDescriptiveName`,
  `getDetailedDescription`, `getPronumGroup`, `replacePlaceholdersName`, `replacePlaceholdersTargetName`,
  `replacePlaceholderSubjectPronoun`, `replacePlaceholderObjectPronoun`

### OpenLockEnabledEntity.kt (2026-06-09 – Step 4)
- Removed `import net.daddldiddl.jbsadventure.LANG`
- `getOpenLockState(lang: LanguageData)` – no longer uses LANG global
- `getMessagePartOpenLockedState(lang: LanguageData)` – updated signature
- `getDescriptiveName(definite, lang)` – updated signature (in `OpenLockEnabledNamedEntity`)
- DATA global retained for `open()`/`close()`/`lock()`/`unlock()` (Step 5)

### Exit.kt (2026-06-09 – Step 4)
- Removed `import net.daddldiddl.jbsadventure.LANG`
- Constructor default for `name` changed from `Name(LANG.getDirectionAliasFromKey(direction))` to `Name(direction)` – semantically equivalent (direction key IS the default alias)
- `getDescriptiveName(definite, lang)`, `getDetailedDescription(lang)` updated
- `toString()` simplified to not need lang

### Item.kt (2026-06-09 – Step 4)
- Removed `import net.daddldiddl.jbsadventure.LANG`
- `getDescriptiveName(definite, lang)`, `getStateMessagePart(lang)`, `getDetailedDescription(lang)` updated
- DATA global retained (Step 5)

### Container.kt (2026-06-09 – Step 4)
- Removed `import net.daddldiddl.jbsadventure.LANG`
- `getDescriptiveName(definite, lang)` delegates to `OpenLockEnabledNamedEntity` via lang
- `getDetailedDescription(lang)` updated – uses lang instead of LANG
- DATA global retained (Step 5)

### Room.kt (2026-06-09 – Step 4)
- Removed `import net.daddldiddl.jbsadventure.LANG`
- `findExitByAlias(input, lang)` – added lang parameter

### ExitSerializer.kt (2026-06-09 – Step 4)
- Removed `import net.daddldiddl.jbsadventure.LANG`
- Name default changed to `Name(surrogate.direction)` (no LANG needed)

### Game.kt (2026-06-09 – Step 4)
- All call sites for `getDescriptiveName`, `getDetailedDescription`, `replacePlaceholdersName`,
  `replacePlaceholdersTargetName` updated to pass `LANG` as the `lang` argument

### LanguageData.kt (2026-06-09 – Step 3 completion)
- Removed `import net.daddldiddl.jbsadventure.LOG`
- `getTemplate()` and `getStateValueFromKey()` now return fallback strings silently (no LOG.warn)

---

**Status:** In Progress — Steps 1–4 complete, Step 5 (DATA removal) next
**Estimated Time Remaining:** 1 hour
**Current Step:** Step 5 – Remove DATA global from Item, Container, OpenLockEnabledEntity

**ILogger.kt** (new, `net.daddldiddl.jbsadventure` package)
- Interface with `debug`, `info`, `warn`, `error`, `console` methods
- Implemented by `SimpleFileLog` in the engine

**IConsole.kt** (new, `net.daddldiddl.jbsadventure` package)
- Interface with `print(String?)`, `print(String?, ConsoleColor)`, `print()`, `warn(String?)` methods
- Implemented by `ConsoleOutput` in the engine

**ConsoleColor.kt** (new, `net.daddldiddl.jbsadventure.tools` package)
- Extracted `ConsoleColor` enum from `ConsoleOutput.kt` into its own file
- Required so `IConsole` (model-lib) can reference it without depending on `ConsoleOutput` (engine)

**GlobalContext.kt** (updated)
- `_log`, `log`, `initialize()` now use `ILogger` instead of `SimpleFileLog`
- `_console`, `console`, `initialize()` now use `IConsole` instead of `ConsoleOutput`
- Imports of `SimpleFileLog` and `ConsoleOutput` removed
- Added `initLog(log: ILogger)` for pre-initialization of logger before `loadLanguageData`

**SimpleFileLog.kt** (updated)
- Now implements `ILogger`
- Removed `import net.daddldiddl.jbsadventure.CONSOLE` — color output inlined directly
- Method names aligned to `ILogger` interface: `debug/info/warn/error/console`

**ConsoleOutput.kt** (updated)
- Now implements `IConsole`
- `ConsoleColor` enum definition removed (now in `ConsoleColor.kt`)
- All `override` modifiers added to methods defined in `IConsole`

**Main.kt** (updated)
- Migrated from old `LOG = ...`, `CONSOLE = ...`, `LANG = ...`, `DATA = ...` assignment pattern
- Now uses `GlobalContext.initLog(log)`, then `GlobalContext.initialize(log, console, lang)`, then `GlobalContext.setGameData(gameData)`
- Unused imports `LanguageData`, `GameData` removed

### LanguageData.kt (2026-06-09)
- Removed `import net.daddldiddl.jbsadventure.LANG` (self-reference was incorrect)
- Changed `LANG.getAllDirectionAliases()` → `getAllDirectionAliases()` (same object, `this`)

---

**Status:** In Progress — Infrastructure phase complete, model dependency cleanup next
**Estimated Time Remaining:** 1-2 hours
**Current Step:** Step 4 – Fix Name.kt & NamedEntity (add LANG parameter)
