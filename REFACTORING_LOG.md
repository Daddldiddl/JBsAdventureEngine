# Refactoring Log - Model/Engine Separation
**Date:** 2026-06-14  
**Goal:** Prepare the current single-module Maven project for the future `model-lib` / `engine` / `editor` split.
## Current Status
The project is still a single Maven module, but the codebase already reflects the most important decoupling work:
- `GlobalContext` now works with `ILogger` and `IConsole`
- `LanguageData.current`, `GameData.current`, and `ILogger.current` are the current global access points
- Language-aware resources are already organized per language under `src/main/resources/lang/<code>/`
- The most recent build succeeded with `mvn -DskipTests package`
## What Is Still Outdated in the Refactoring Docs
The guide and older notes still mention:
- Kotlin `2.3.21` instead of the current `2.4.0`
- the pre-refactor resource layout (`src/main/resources/data.json`, `src/main/resources/lang/en.json`)
- a module split that has not been created yet
- an old step list with duplicated / contradictory progress entries
## Recommended Next Refactoring Step
1. Finish the remaining engine/model cleanup in `Action.kt`.
2. Then update `Precondition.kt`.
3. Then review `tools/DataValidator.kt`, `tools/GameLoader.kt`, and serializers for any remaining engine coupling.
4. Only after that, start the actual module split.
## Why `Action.kt` First
`Action.kt` is the most central place still likely to carry engine-facing behavior such as output or logging. Cleaning it first reduces the amount of follow-up work in the remaining model classes.
## Notes
- The current approach of using companion objects for shared runtime state is working well for the current codebase.
- Keep the refactoring log short and current; preserve only decisions that still matter now.
- Treat the older detailed step-by-step notes below as historical context, not as the live status.
