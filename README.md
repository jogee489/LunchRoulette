# LunchRoulette

LunchRoulette takes the indecision out of picking where to eat. Add your favourite restaurants, spin the wheel, and let fate decide.

## What it does

- Maintain a personal list of restaurants (add / swipe-to-delete with undo)
- Spin a colour-coded roulette wheel to pick a random winner
- Haptic feedback on spin start and winner reveal

## Building

Requires Android Studio or the Android command-line tools with a connected device or emulator (minSdk 26).

```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Unit tests (no device needed)
./gradlew test

# Full build + lint
./gradlew build
```

See [CLAUDE.md](CLAUDE.md) for the full command reference.

## Architecture

Single-module Kotlin app (minSdk 26, targetSdk 36):

- **MainActivity** — spin logic, `CountDownTimer`-driven animation, winner determined before spin starts
- **RestaurantsListActivity** — CRUD list via `RecyclerView`; swipe-to-delete with Snackbar undo
- **RouletteView** — custom `View` drawing pie-slice arcs on `Canvas`
- **RestaurantsDBHelper** — SQLite persistence via `SQLiteOpenHelper`
