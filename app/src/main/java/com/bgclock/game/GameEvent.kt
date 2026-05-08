@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.bgclock.game

import kotlin.time.Instant

sealed interface GameEvent {
    val at: Instant

    data class Started(override val at: Instant) : GameEvent
    data class TurnPassed(override val at: Instant) : GameEvent
    data class Paused(override val at: Instant) : GameEvent
    data class Resumed(override val at: Instant) : GameEvent
    data class Reverted(override val at: Instant) : GameEvent
}
