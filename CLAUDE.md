# CLAUDE.md

This is an Android app for time control during multiplayer board games.

Upon startup you immediately get to the Settings screen, which let's you configure

- number of players
- their names and colors
- initialTimeBudget and turnIncrement

after configuring, user presses Start and gets to timer screen in a paused state. Timer screen has

- solid background of the color of the current player
- name of the current player
- small buttons for pause/resume, revert, debug mode
- everything except those two buttons is treated as a huge button that passes the turn to the next player

The app stores in it's state the timestamps of all button presses during the current game.
This allows for revert capability, in case a player accidentally tapped the screen and passed the turn to the next player.
We can just press revert, and this will simply cancel the last event in the timeline and recalculate current timers.
This also allows backup mode, in which a user may inspect the timeline of button presses with timestamps.
Each event in the timeline shows app's view on timers of all the players just after the event.
E.g. for a game with 30m initialTimeBudget and 1m turnIncrement timeline can look like this

- 2026-05-07 20:00:00; Game start; Alice 31m; Bob 30m
- 2026-05-07 20:02:07; Turn passed (to Bob); Alice 28m53s; Bob 31m
- 2026-05-07 20:03:00; Pause; Alice 28m53s; Bob 30m7s
- 2026-05-07 20:08:00; Resume; Alice 28m53s; Bob 30m7s
- 2026-05-07 20:18:00; Turn passed (to Alice); Alice 29m53s; Bob 20m7s
- 2026-05-07 20:18:01; Turn passed (to Bob); Alice 29m52s; Bob 21m7s # accidental tap
- 2026-05-07 20:19:00; Revert; Alice 28m53s; Bob 20m7s # players noticed the wrong timer was ticking for the last minute, so they reverted the last tap
- 2026-05-07 20:23:17; Turn passed (to Bob); Alice 24m36s; Bob 21m7s
- ...

When player's timer runs out, app says out loud "one minute remaining", "30 seconds remaining", "10 seconds remaining" and "time is out, game over"

## Backlog

### visible

<!-- - System back during game does revert (not sure it's a good idea yet) -->
<!-- - use settings from the last game instead of placeholder; add a button to clear settings -->
<!-- - save player names used in prev games; suggest autocomplete using the most frequent ones (based on prev games) -->

### hygiene

- migrate off Android Studio's bundled SDK. `local.properties` currently has `sdk.dir=~/.local/share/android-studio/sdk`, which dies if Studio is uninstalled. Replace with either (a) AUR packages `android-sdk-cmdline-tools-latest` + `android-sdk-platform-tools` + `android-sdk-build-tools` + `android-platform` (installs to `/opt/android-sdk`), or (b) a manual cmdline-tools install under `~/Android/sdk` driven by `sdkmanager`. Then update `sdk.dir` (or set `ANDROID_HOME`) accordingly. `just check` should keep working unchanged.
- drop `@file:OptIn(kotlin.time.ExperimentalTime::class)` from `GameEvent.kt`, `GameState.kt`, and `GameStateTest.kt` once `kotlin.time.Instant` graduates to stable (expected in a near-future Kotlin release, ~2.3/2.4). No code change beyond removing the three annotation lines.
