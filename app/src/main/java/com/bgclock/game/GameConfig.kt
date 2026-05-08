package com.bgclock.game

import kotlin.time.Duration

data class GameConfig(
    val players: List<Player>,
    val initialTimeBudget: Duration,
    val turnIncrement: Duration,
) {
    init {
        require(players.size >= 2) { "need at least 2 players, got ${players.size}" }
        require(initialTimeBudget.isPositive()) { "initialTimeBudget must be positive" }
        require(!turnIncrement.isNegative()) { "turnIncrement must be non-negative" }
    }
}
