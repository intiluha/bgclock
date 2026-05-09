@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.bgclock.game

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.Instant

fun formatTimelineRow(
    config: GameConfig,
    events: List<GameEvent>,
    i: Int,
    zone: ZoneId = ZoneId.systemDefault(),
): String {
    val event = events[i]
    val state = config.stateAt(events.subList(0, i + 1), event.at)
    val description = when (event) {
        is GameEvent.Started -> "Game start"
        is GameEvent.TurnPassed ->
            "Turn passed (to ${config.players[state.activePlayerIndex ?: 0].name})"
        is GameEvent.Paused -> "Pause"
        is GameEvent.Resumed -> "Resume"
        is GameEvent.Reverted -> "Revert"
    }
    val timers = config.players.zip(state.remainingTimes) { p, t ->
        "${p.name} ${t.formatHmsCompact()}"
    }.joinToString("; ")
    return "${formatTimelineTimestamp(event.at, zone)}; $description; $timers"
}

private val TIMESTAMP_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun formatTimelineTimestamp(at: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
    TIMESTAMP_FORMAT.withZone(zone)
        .format(java.time.Instant.ofEpochMilli(at.toEpochMilliseconds()))

fun Duration.formatHmsCompact(): String {
    val total = inWholeSeconds
    val sign = if (total < 0) "-" else ""
    val abs = if (total < 0) -total else total
    val h = abs / 3600
    val m = (abs % 3600) / 60
    val s = abs % 60
    val sb = StringBuilder()
    if (h > 0) sb.append("${h}h")
    if (m > 0) sb.append("${m}m")
    if (s > 0) sb.append("${s}s")
    if (sb.isEmpty()) sb.append("0s")
    return sign + sb
}
