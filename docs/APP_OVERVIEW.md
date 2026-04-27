# KMPTapDuelGame — App Overview

A local 2-player tap battle written **once in `commonMain`** and deployed to **Android, iOS, Desktop (JVM), Web (Wasm), and Web (JS)**. The point of this project is to exercise the real bondades of Kotlin Multiplatform — shared game logic, shared UI, platform code only where it earns its keep — on a tiny but complete game.

> Looking for the runtime story (gradle commands, IDE setup, troubleshooting)? See [Development Commands](DEVELOPMENT_COMMANDS.md), [Running the App](getting-started/RUNNING_THE_APP.md), and [Troubleshooting](getting-started/TROUBLESHOOTING.md).

## What the game does

The screen splits into two zones. Player 1 owns the left side, Player 2 owns the right. Every tap pushes the center divider toward the opponent. The first player to push the divider into the opposite win zone (after **20 net taps**) wins the round.

| Piece | Where it lives | Notes |
|---|---|---|
| **Game state** | `game/TapDuelState.kt` | `@Immutable` data class. `dividerPosition` is derived from tap counts, so float drift never affects the win check. |
| **Game engine** | `game/TapDuelGame.kt` | Pure functions: `reset`, `start`, `tapPlayerOne`, `tapPlayerTwo`. Trivially testable. |
| **Player + status enums** | `game/Player.kt`, `game/GameStatus.kt` | `Player.One`/`Two`, `GameStatus.Ready`/`CountingDown`/`Playing`/`Finished`. |
| **State holder** | `ui/TapDuelViewModel.kt` | Multiplatform `androidx.lifecycle.ViewModel`. Holds `StateFlow<TapDuelState>` plus `StateFlow<Int?>` for the countdown. |
| **Countdown timer** | `ui/TapDuelViewModel.start()` | `viewModelScope.launch` with `delay()` from kotlinx-coroutines. Cancellable on reset. |
| **Tap detection** | `ui/TapDuelScreen.kt` | `pointerInput { awaitEachGesture { awaitFirstDown(requireUnconsumed = false) } }` — every press counts, no swallowed events. |
| **Adaptive layout** | `ui/TapDuelScreen.kt` | `BoxWithConstraints` threshold of 600 dp: horizontal split on tablet/desktop/web, vertical split on phones held portrait. |
| **Animated divider** | `ui/TapDuelScreen.kt` | `animateFloatAsState` smooths the divider movement; weights are coerced to a small minimum so a zone never collapses to 0. |
| **Material 3 theme** | `ui/theme/AppTheme.kt` | Cool blue → `colorScheme.primary`, warm red → `colorScheme.error`. Light + dark schemes ship in the same file. |
| **i18n: EN + ES** | `composeResources/values{,-es}/strings.xml` | Picked from system locale. |

## Game flow

```
Ready  ──[Start]──▶  CountingDown  ──[delay 3·2·1·GO!]──▶  Playing
                                                              │
                                                          tap, tap, tap
                                                              │
                                                              ▼
                                                          Finished  ──[Reset / Play again]──▶  Ready
```

- `Ready` and `Finished`: taps are ignored by the engine.
- `CountingDown`: taps are ignored, overlay shows `3 → 2 → 1 → GO!`.
- `Playing`: taps mutate state. The engine resolves a winner when net taps hit `±TAPS_TO_WIN` (20).

## State, not floats

The `dividerPosition` exposed to Compose is **derived** from `playerOneTaps - playerTwoTaps`:

```kotlin
val dividerPosition: Float
    get() = (INITIAL_POSITION + (playerOneTaps - playerTwoTaps) * TAP_STEP).coerceIn(0f, 1f)
```

That choice is deliberate — accumulating `+= 0.02f` for 20 iterations drifts. Recomputing from the integer counts is exact, and the win condition itself runs on the integer net difference, never on the float.

## Source set layout

```
composeApp/src/
├── commonMain/                     ← all UI, ViewModel, engine, resources
│   ├── kotlin/com/xergioalex/kmptapduelgame/
│   │   ├── App.kt                  ← AppTheme { TapDuelScreen() }
│   │   ├── Platform.kt             ← expect Platform contract (KMP demo, not used by gameplay)
│   │   ├── game/                   ← Player, GameStatus, TapDuelState, TapDuelGame
│   │   └── ui/
│   │       ├── TapDuelScreen.kt    ← split arena, divider, counters, controls, overlays
│   │       ├── TapDuelViewModel.kt ← StateFlow + viewModelScope countdown
│   │       └── theme/AppTheme.kt   ← Material 3 light/dark
│   └── composeResources/values{,-es}/strings.xml
│
├── commonTest/                     ← pure-function engine tests with kotlin.test
│   └── kotlin/com/xergioalex/kmptapduelgame/game/TapDuelGameTest.kt
│
├── androidMain/    ← MainActivity sets content { App() }, Platform.android.kt
├── iosMain/        ← MainViewController() returns ComposeUIViewController { App() }, Platform.ios.kt
├── jvmMain/        ← application { Window { App() } }, Platform.jvm.kt
├── jsMain/         ← Platform.js.kt
├── wasmJsMain/     ← Platform.wasmJs.kt
└── webMain/        ← shared JS+Wasm entry: ComposeViewport { App() }
```

There's intentionally no `nonWebMain` and no platform bridge for gameplay. Compared to the [previous `kmptodoapp` incarnation](https://github.com/xergioalex/kmptodoapp), every piece below was removed because the game doesn't need it:

- SQLDelight + `nonWebMain` (no persistence — local in-memory state)
- multiplatform-settings (no preferences saved)
- kotlinx-datetime (no due dates / timestamps)
- per-platform `TaskSharer` (`expect/actual`) (no share intent)
- `AppContainer` DI-lite holder (no dependencies to wire)

That's the point: KMP earns its keep when shared logic is meaningful and platform glue is reserved for genuine platform calls.

## Architecture in two layers

```
                ┌──────────────────────────────────────┐
   commonMain   │  ui/ (TapDuelScreen + ViewModel)     │  StateFlow + collectAsState
                └──────────────────┬───────────────────┘
                                   │ reads / mutates
                                   ▼
                ┌──────────────────────────────────────┐
                │  game/ (Player, State, GameEngine)   │  Pure Kotlin, zero platform deps
                └──────────────────────────────────────┘
```

- **`game/` is dependency-free Kotlin.** No Compose, no coroutines, no Android Context. Trivially testable — see `TapDuelGameTest`.
- **`ui/` orchestrates.** `TapDuelViewModel` owns state. `TapDuelScreen` reads it and dispatches taps.

## How the KMP tools are exercised here

| Pattern | Where to look |
|---|---|
| **Shared composables** | `TapDuelScreen.kt` and the whole `ui/` tree run on every target. No per-platform `App` clones. |
| **`expect fun` / `actual fun`** | `Platform.kt` in `commonMain` + `Platform.<platform>.kt` actuals in five source sets. Kept as a teaching example even though gameplay doesn't need it. |
| **Multiplatform `ViewModel`** | `TapDuelViewModel` extends `androidx.lifecycle.ViewModel` and uses `viewModelScope` — works identically on Android, iOS, JVM, JS, and Wasm. |
| **Compose Multiplatform resources** | `composeResources/values/strings.xml` + `values-es/strings.xml`; consumed via `Res.string.*` from the generated `kmptapduelgame.composeapp.generated.resources` package. |
| **Multiplatform pointer input** | `pointerInput { awaitEachGesture { awaitFirstDown() } }` works for touch on Android/iOS, mouse on Desktop, and pointer events on Web — same code, every platform. |
| **`@Immutable` for stable Compose recomposition** | `TapDuelState` is annotated `@Immutable` so Compose can skip recomposition when only an unrelated piece of state changes. |
| **kotlin.test in `commonTest`** | `TapDuelGameTest` runs on every Kotlin target with no platform dependencies. |

## Build & run

| Target | Command |
|---|---|
| Desktop (with Compose Hot Reload) | `./gradlew :composeApp:run` |
| Android (debug install) | `./gradlew :composeApp:installDebug` |
| iOS | open `iosApp/iosApp.xcodeproj` in Xcode → ⌘R |
| Web (Wasm, recommended) | `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` |
| Web (JS, fallback) | `./gradlew :composeApp:jsBrowserDevelopmentRun` |
| All Kotlin compiles | `./gradlew :composeApp:assemble` |
| JVM tests | `./gradlew :composeApp:jvmTest` |

> Use Java 21 — `export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"` on macOS. The bundled Kotlin compiler in Gradle 8.14 doesn't recognize Java 26 yet.

## What's next

Hooks intentionally left for follow-ups:

- **Best-of-3 / round counter** — track wins across rounds; small extension of `TapDuelViewModel`.
- **Sound + haptics** per tap — `expect/actual` over Android `Vibrator` / iOS `UIImpactFeedbackGenerator` / a no-op on Desktop+Web. First real platform-bridge use case.
- **High-score persistence** — re-introduce multiplatform-settings to remember the all-time fastest win.
- **Tilt / motion** as input on mobile, falling back to taps on Desktop+Web.
- **AI single-player mode** — pure logic in `commonMain`, no platform code needed.
- **Tests for `TapDuelViewModel`** with `kotlinx-coroutines-test` — covers the countdown timer end-to-end.

None of these need cross-platform plumbing for the engine — they're product decisions waiting for a product owner.
