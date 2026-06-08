# Refactoring Log - Model-Lib Separation

**Date:** 2026-06-09  
**Goal:** Remove global singleton dependencies (LOG, CONSOLE, DATA, LANG) from model classes

## Strategy

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
2. ⏳ Fix LanguageData.kt (remove self-references)
3. ⏳ Fix Name.kt & NamedEntity (add LANG parameter)
4. ⏳ Fix model/*.kt (Item, Container, Exit, Room, OpenLockEnabledEntity)
5. ⏳ Fix Action.kt (remove LOG, CONSOLE)
6. ⏳ Fix Precondition.kt (remove LOG)
7. ⏳ Fix GameData.kt (remove LOG)
8. ⏳ Fix tools/*.kt (DataValidator, GameLoader - remove LOG)
9. ⏳ Fix serializers (ExitSerializer - add LANG parameter)
10. ⏳ Update engine code (Main.kt, Game.kt) to pass parameters
11. ⏳ Test build
12. ⏳ Split into modules

---

## Change Log

### LanguageData.kt
- Remove `import LANG` and `import LOG`
- Remove self-referential LANG access
- Make methods pure (no side effects)

### Name.kt & NamedEntity.kt
- Add `lang: LanguageData` parameter to all methods that need i18n
- Remove `LANG` global access

### Item.kt, Container.kt, Exit.kt, Room.kt
- Add `lang: LanguageData` parameter to display methods
- Remove `LANG` and `DATA` global access

### Action.kt
- Remove `LOG` and `CONSOLE` imports
- Remove logging calls
- Return messages instead of printing
- Add `lang: LanguageData` to constructor if needed

### Precondition.kt
- Remove `LOG` import
- Remove logging, throw exceptions instead

### GameData.kt
- Remove `LOG` import
- Remove logging calls

### DataValidator.kt & GameLoader.kt
- Keep as-is in tools, they stay in engine module
- These can use LOG since they're runtime-only

---

**Status:** In Progress
**Estimated Time:** 2-3 hours
**Current Step:** Starting implementation

