# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Run unit tests (JVM, no device needed)
./gradlew test

# Run a single unit test class
./gradlew test --tests "com.thejiltedalchemist.lunchroulette.WheelGeometryTest"

# Run instrumented tests (requires connected device or emulator)
./gradlew connectedAndroidTest

# Full build with lint
./gradlew build
```

## Architecture Overview

Single-module Android app (Kotlin, minSdk 26, targetSdk 35) with two activities and a custom view:

**MainActivity** — entry point. Reloads restaurants from SQLite in `onResume` (so edits made on the list screen show up), renders the `RouletteView`, and drives the spin with an `ObjectAnimator` on the view's rotation. The winner is determined *before* the animation starts, and `WheelGeometry.rotationForWinner` (pure, unit-tested) gives the rotation that puts that slice under the 12 o'clock pointer.

**RestaurantsListActivity** — CRUD list screen. Uses a `RecyclerView` with `RestaurantAdapter`. Swipe-to-delete is handled by `SwipeToDeleteCallback` (extends `ItemTouchHelper.SimpleCallback`); deleted items are held in memory and can be restored via a Snackbar undo action.

**RouletteView** — custom `View` that draws pie-slice arcs with `Canvas.drawArc` and text along each arc's path. It alternates three colors (`colorPrimary`, `colorPrimaryDark`, `design_default_color_secondary_variant`). Call `addRouletteItems(list)` to repopulate and trigger a redraw.

**Data layer** — `RestaurantsDBHelper` (extends `SQLiteOpenHelper`) wraps a single `restaurants` table with a `name TEXT COLLATE NOCASE PRIMARY KEY` column. `DBContract.RestaurantsEntry` defines the table/column constants. `RestaurantsModel` is a plain `data class(val name: String)`; `normalizeRestaurantName` (trim + collapse whitespace) is applied to user input. `insertRestaurant`/`insertRestaurantAt` return `InsertResult` (INSERTED/DUPLICATE/FAILED); rows are read back in rowid order, so `insertRestaurantAt` rewrites the table to keep stored order equal to on-screen order (used by Undo). Note: the schema also declares an `address` column in `SQL_CREATE_ENTRIES`, but `RestaurantsModel` only stores `name` — `address` is unused.

**View binding** is enabled project-wide (`viewBinding = true`). Layouts: `activity_main.xml` (main screen), `list_page.xml` (list screen), `list_item_restaurant.xml` (row).

## Git Workflow

- Branch prefixes: `feature/`, `bug/`, `chore/` — always follow this convention.
- Never merge into `main` without explicit instruction.

## Known Issues / TODOs

- Schema is at version 2 (`name` is `COLLATE NOCASE` so names are unique ignoring ASCII case). When it changes again, increment `DATABASE_VERSION` and add the next `oldVersion < N` step to `onUpgrade`. `RestaurantsDBHelperTest` (instrumented) covers the v1→v2 migration.
- The `address` column exists in the DB schema but is never populated or read.
