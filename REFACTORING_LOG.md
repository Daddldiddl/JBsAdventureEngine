# Refactoring Log - Model/Engine Separation
**Date:** 2026-06-15  
**Goal:** Track post-split status, completed closure work, and active follow-ups.

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

## Completed Since Split
1. Documentation alignment completed for module-aware paths and commands in `AGENTS.md`, `README.md`, and `TUTORIAL.md`.
2. Refactoring status docs updated to reflect the split as done and move completed tasks out of active follow-ups.

## Open Follow-Ups
1. Optionally clean remaining non-critical compiler warnings in engine utility classes.
2. Continue `editor` module implementation (Gradle + Compose Desktop) with local `model-lib` JAR integration.
3. Revisit optional boundary hardening (reduce companion-singleton coupling) only if it improves maintainability without cross-module regression.

## Notes
- `editor` is still planned and intentionally not part of the Maven reactor.
- This log tracks live status only; detailed historical planning belongs in git history.
