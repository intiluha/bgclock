# bgclock

A multiplayer board-game timer for Android. Runs a Fischer-style chess clock
for 2–8 players: each player has a time budget that ticks down on their turn
and gets a small increment whenever they pass it on.

## Features

- 2–8 players with configurable names and colors
- Per-player initial time budget + turn increment (Fischer increment)
- Tap anywhere on the screen to pass the turn; pause/resume any time
- Revert button (and system back) for accidental taps
- Voice countdown via system TTS at 60s / 30s / 10s / 0s remaining
- TTS mute toggle

## Install

The easiest path is [Obtainium](https://github.com/ImranR98/Obtainium):

1. Add app → paste `https://github.com/intiluha/bgclock`.
2. Obtainium picks up the latest GitHub release and tracks it for updates.

Alternatively, download `app-release.apk` from the
[Releases](https://github.com/intiluha/bgclock/releases) page and
sideload it.

Minimum Android version: 8.0 (API 26).

## Build from source

Requirements: JDK 17, the Android SDK (point AGP at it via `$ANDROID_HOME`
or a `local.properties` file with `sdk.dir=/path/to/android-sdk`), and
[`just`](https://github.com/casey/just).

```sh
just check        # lint + unit tests
just run          # build + install debug APK + launch (needs phone connected via USB with debugging enabled on it)
```

## License

Beerware. See [LICENSE](LICENSE).
