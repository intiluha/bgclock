@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.bgclock.game

import kotlin.time.Duration
import kotlin.time.Instant

data class GameState(
    val isStarted: Boolean,
    val isPaused: Boolean,
    val activePlayerIndex: Int?,
    val remainingTimes: List<Duration>,
)

fun GameConfig.stateAt(events: List<GameEvent>, now: Instant): GameState {
    val effective = effectiveEvents(events)
    val times = MutableList(players.size) { initialTimeBudget }
    var active: Int? = null
    var paused = false
    var lastTickAt: Instant? = null
    var started = false

    fun applyElapsed(until: Instant) {
        val a = active ?: return
        if (paused) return
        val from = lastTickAt ?: return
        val elapsed = until - from
        require(!elapsed.isNegative()) { "elapsed must be non-negative, got $elapsed (from=$from, until=$until)" }
        times[a] = times[a] - elapsed
    }

    for (e in effective) {
        when (e) {
            is GameEvent.Started -> {
                require(!started) { "Started after the game had already started: $e" }
                started = true
                active = 0
                times[0] = times[0] + turnIncrement
                lastTickAt = e.at
            }
            is GameEvent.TurnPassed -> {
                require(started) { "TurnPassed before Started: $e" }
                applyElapsed(e.at)
                active = ((active ?: 0) + 1) % players.size
                times[active] = times[active] + turnIncrement
                lastTickAt = e.at
            }
            is GameEvent.Paused -> {
                require(started) { "Paused before Started: $e" }
                require(!paused) { "Paused while already paused: $e" }
                applyElapsed(e.at)
                paused = true
                lastTickAt = e.at
            }
            is GameEvent.Resumed -> {
                require(started) { "Resumed before Started: $e" }
                require(paused) { "Resumed while not paused: $e" }
                paused = false
                lastTickAt = e.at
            }
            is GameEvent.Reverted -> {
                // Never appears in `effective`; effectiveEvents() pairs each Reverted
                // with the prior non-skipped event and drops both.
            }
        }
    }
    applyElapsed(now)

    return GameState(
        isStarted = started,
        isPaused = paused,
        activePlayerIndex = active,
        remainingTimes = times.toList(),
    )
}

private fun effectiveEvents(events: List<GameEvent>): List<GameEvent> {
    val effective = ArrayList<GameEvent>(events.size)
    for (e in events) {
        when (e) {
            is GameEvent.Reverted -> if (effective.isNotEmpty()) effective.removeAt(effective.lastIndex)
            else -> effective.add(e)
        }
    }
    return effective
}
