# Refactoring Log - Model/Engine Separation
**Date:** 2026-06-14  
**Goal:** Completed split to `model-lib` + `engine`, keep follow-up steps visible.

## Current Status
- Maven reactor is active at root (`pom.xml`, packaging `pom`) with modules `model-lib` and `engine`
- Shared model/i18n/loader/validator/serializers are in `model-lib`
- Runtime loop, CLI, logging/config and save handling are in `engine`
- Bundled language resources are now in `engine/src/main/resources/lang/<code>/`
- Run scripts (`runDE.sh`, `runEN.sh`, `testDE.sh`, `testEN.sh`) target `engine/target/...-jar-with-dependencies.jar`

## Completed Refactoring Highlights
- Decoupled model-layer classes from engine globals where possible (`ILogger.current`, companion-based access)
- Moved `Action` output responsibility to game loop execution context
- Updated serializers/loaders for module separation
- Updated parent/module POMs and validated reactor build flow

## Open Follow-Ups
1. Keep docs aligned with the split (`README.md`, `TUTORIAL.md`, `AGENTS.md`, `REFACTORING_GUIDE.md`)
2. Optionally clean remaining non-critical compiler warnings in engine utility classes
3. Start `editor` module implementation (Gradle + Compose Desktop) once scope is fixed

## Notes
- `editor` is still planned and intentionally not part of the Maven reactor.
- This log tracks live status only; detailed historical planning belongs in git history.
