@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.bgclock.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bgclock.game.GameConfig
import com.bgclock.game.GameEvent
import com.bgclock.game.Palette
import com.bgclock.game.Player
import com.bgclock.game.formatTimelineRow
import com.bgclock.game.stateAt
import com.bgclock.ui.theme.BgclockTheme
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Composable
fun TimerScreen(
    config: GameConfig,
    events: List<GameEvent>,
    onAppendEvent: (GameEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var nowTick by remember { mutableStateOf(Clock.System.now()) }
    var debugMode by remember { mutableStateOf(false) }
    var ttsMuted by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            nowTick = Clock.System.now()
            delay(100)
        }
    }

    val state = config.stateAt(events, nowTick)
    val activeIndex = state.activePlayerIndex ?: 0
    val activePlayer = config.players[activeIndex]
    val remaining = state.remainingTimes[activeIndex]

    val tts = rememberTts()
    val announced = remember(state.activePlayerIndex, state.isStarted) {
        mutableSetOf<Long>()
    }
    SideEffect {
        if (tts == null || !state.isStarted || state.isPaused) return@SideEffect
        val seconds = remaining.inWholeSeconds
        for (threshold in WARNING_THRESHOLDS) {
            if (seconds <= threshold && threshold !in announced) {
                announced.add(threshold)
                if (!ttsMuted) {
                    tts.speak(
                        warningFor(threshold),
                        TextToSpeech.QUEUE_ADD,
                        null,
                        "bgclock-${state.activePlayerIndex}-$threshold",
                    )
                }
            }
        }
    }

    val tapInteraction = remember { MutableInteractionSource() }

    fun appendNow(makeEvent: (Instant) -> GameEvent) {
        val now = Clock.System.now()
        nowTick = now
        onAppendEvent(makeEvent(now))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(activePlayer.color)
            .clickable(
                interactionSource = tapInteraction,
                indication = null,
                onClick = {
                    if (state.isPaused) return@clickable
                    appendNow { if (state.isStarted) GameEvent.TurnPassed(it) else GameEvent.Started(it) }
                },
            ),
    ) {
        TextButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
        ) {
            Text("← Back")
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TextButton(
                onClick = { ttsMuted = !ttsMuted },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
            ) {
                Text(if (ttsMuted) "🔇" else "🔊")
            }
            TextButton(
                onClick = {
                    appendNow { if (state.isPaused) GameEvent.Resumed(it) else GameEvent.Paused(it) }
                },
                enabled = state.isStarted,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
            ) {
                Text(if (state.isPaused) "▶ Resume" else "❙❙ Pause")
            }
            TextButton(
                onClick = { appendNow { GameEvent.Reverted(it) } },
                enabled = state.isStarted,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
            ) {
                Text("↶ Revert")
            }
            TextButton(
                onClick = { debugMode = !debugMode },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
            ) {
                Text("Debug")
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = activePlayer.name,
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = remaining.formatMmSs(),
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
            )
        }

        Text(
            text = when {
                !state.isStarted -> "Tap anywhere to start"
                state.isPaused -> "Paused"
                else -> "Tap anywhere to pass turn"
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )

        if (debugMode) {
            DebugOverlay(config = config, events = events, onClose = { debugMode = false })
        }
    }
}

@Composable
private fun DebugOverlay(config: GameConfig, events: List<GameEvent>, onClose: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.92f),
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Timeline (${events.size} events)",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                TextButton(
                    onClick = onClose,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                ) { Text("Close") }
            }
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (events.isEmpty()) {
                    Text("(no events yet)", color = Color.White)
                } else {
                    for (i in events.indices) {
                        Text(
                            formatTimelineRow(config, events, i),
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

private fun Duration.formatMmSs(): String {
    val total = inWholeSeconds
    val sign = if (total < 0) "-" else ""
    val abs = if (total < 0) -total else total
    val mins = abs / 60
    val secs = abs % 60
    return "$sign$mins:${secs.toString().padStart(2, '0')}"
}

private val WARNING_THRESHOLDS = listOf(60L, 30L, 10L, 0L)

private fun warningFor(threshold: Long): String = when (threshold) {
    60L -> "one minute remaining"
    30L -> "30 seconds remaining"
    10L -> "10 seconds remaining"
    0L -> "time is out, game over"
    else -> ""
}

@Composable
private fun rememberTts(): TextToSpeech? {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        var instance: TextToSpeech? = null
        instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                instance?.language = Locale.US
                tts = instance
            }
        }
        onDispose {
            instance?.stop()
            instance?.shutdown()
            tts = null
        }
    }
    return tts
}

@Preview(showBackground = true, name = "Timer — mid-game")
@Composable
private fun TimerPreviewMidGame() {
    val now = Clock.System.now()
    BgclockTheme {
        TimerScreen(
            config = GameConfig(
                players = listOf(
                    Player("Alice", Palette.Red),
                    Player("Bob", Palette.Blue),
                    Player("Carol", Palette.Green),
                ),
                initialTimeBudget = 30.minutes,
                turnIncrement = 30.seconds,
            ),
            events = listOf(
                GameEvent.Started(now - 7.minutes),
                GameEvent.TurnPassed(now - 5.minutes),
                GameEvent.TurnPassed(now - 90.seconds),
            ),
            onAppendEvent = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "Timer — paused")
@Composable
private fun TimerPreviewPaused() {
    val now = Clock.System.now()
    BgclockTheme {
        TimerScreen(
            config = GameConfig(
                players = listOf(
                    Player("Alice", Palette.Red),
                    Player("Bob", Palette.Blue),
                ),
                initialTimeBudget = 30.minutes,
                turnIncrement = 30.seconds,
            ),
            events = listOf(
                GameEvent.Started(now - 3.minutes),
                GameEvent.Paused(now - 10.seconds),
            ),
            onAppendEvent = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "Timer — not started")
@Composable
private fun TimerPreviewNotStarted() {
    BgclockTheme {
        TimerScreen(
            config = GameConfig(
                players = listOf(
                    Player("Alice", Palette.Red),
                    Player("Bob", Palette.Blue),
                ),
                initialTimeBudget = 30.minutes,
                turnIncrement = 30.seconds,
            ),
            events = emptyList(),
            onAppendEvent = {},
            onBack = {},
        )
    }
}
