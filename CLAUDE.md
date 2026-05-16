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
./gradlew test --tests "com.thejiltedalchemist.lunchroulette.ExampleUnitTest"

# Run instrumented tests (requires connected device or emulator)
./gradlew connectedAndroidTest

# Full build with lint
./gradlew build
```

## Architecture Overview

Single-module Android app (Kotlin, minSdk 26, targetSdk 34) with two activities and a custom view:

**MainActivity** — entry point. Loads restaurants from SQLite on `onCreate`, renders the `RouletteView`, and drives the spin animation via `CountDownTimer`. The winner is determined *before* the animation starts (the wheel spins to land on a pre-chosen index).

**RestaurantsListActivity** — CRUD list screen. Uses a `RecyclerView` with `RestaurantAdapter`. Swipe-to-delete is handled by `SwipeToDeleteCallback` (extends `ItemTouchHelper.SimpleCallback`); deleted items are held in memory and can be restored via a Snackbar undo action.

**RouletteView** — custom `View` that draws pie-slice arcs with `Canvas.drawArc` and text along each arc's path. It alternates three colors (`colorPrimary`, `colorPrimaryDark`, `design_default_color_secondary_variant`). Call `addRouletteItems(list)` to repopulate and trigger a redraw.

**Data layer** — `RestaurantsDBHelper` (extends `SQLiteOpenHelper`) wraps a single `restaurants` table with a `name TEXT PRIMARY KEY` column. `DBContract.RestaurantsEntry` defines the table/column constants. `RestaurantsModel` is a plain `data class(val name: String)`. Note: the schema also declares an `address` column in `SQL_CREATE_ENTRIES`, but `RestaurantsModel` only stores `name` — `address` is unused.

**View binding** is enabled project-wide (`viewBinding = true`). Layouts: `activity_main.xml` (main screen), `list_page.xml` (list screen), `list_item_restaurant.xml` (row).

## Git Workflow

- Branch prefixes: `feature/`, `bug/`, `chore/` — always follow this convention.
- Never merge into `main` without explicit instruction.

## Known Issues / TODOs

- `onUpgrade` in `RestaurantsDBHelper` calls `onCreate` without dropping the old table — increment `DATABASE_VERSION` when changing the schema and add a proper migration.
- The `address` column exists in the DB schema but is never populated or read.

## Feature Backlog

| Branch | Work |
|---|---|
| `feature/winner-celebration` | Confetti animation + winner popup dialog on spin finish; highlight/pulse the winning wedge in `RouletteView` |
| `feature/shake-to-spin` | Accelerometer-triggered spin via `SensorManager` |
| `feature/spin-history` | SQLite table + UI for last N winners (name + timestamp) |
| `feature/share-result` | Share winner via `Intent.ACTION_SEND` |
| `chore/readme-and-goal` | Add `README.md` with goal statement and build instructions |
| `chore/replace-icons` | Replace `dd_logo.png` (DoorDash asset) and Android launcher icon placeholder |
