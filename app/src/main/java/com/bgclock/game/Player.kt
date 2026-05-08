package com.bgclock.game

import androidx.compose.ui.graphics.Color

data class Player(val name: String, val color: Color) {
    init {
        require(name.isNotBlank()) { "player name must not be blank" }
    }
}
