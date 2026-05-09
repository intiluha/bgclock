@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.bgclock

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.bgclock.game.GameConfig
import com.bgclock.game.GameEvent
import com.bgclock.game.Player
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

val NullableGameConfigSaver: Saver<GameConfig?, Any> = listSaver(
    save = { config ->
        if (config == null) {
            emptyList()
        } else {
            buildList<Any> {
                add(config.players.size)
                for (p in config.players) {
                    add(p.name)
                    add(p.color.toArgb())
                }
                add(config.initialTimeBudget.inWholeMilliseconds)
                add(config.turnIncrement.inWholeMilliseconds)
            }
        }
    },
    restore = { list ->
        if (list.isEmpty()) {
            null
        } else {
            var i = 0
            val n = list[i++] as Int
            val players = (0 until n).map {
                val name = list[i++] as String
                val argb = list[i++] as Int
                Player(name, Color(argb))
            }
            val initialMs = list[i++] as Long
            val incrementMs = list[i++] as Long
            GameConfig(players, initialMs.milliseconds, incrementMs.milliseconds)
        }
    },
)

val EventListSaver: Saver<SnapshotStateList<GameEvent>, Any> = listSaver(
    save = { events ->
        buildList<Any> {
            for (event in events) {
                val tag = when (event) {
                    is GameEvent.Started -> 0
                    is GameEvent.TurnPassed -> 1
                    is GameEvent.Paused -> 2
                    is GameEvent.Resumed -> 3
                    is GameEvent.Reverted -> 4
                }
                add(tag)
                add(event.at.toEpochMilliseconds())
            }
        }
    },
    restore = { list ->
        val restored = mutableStateListOf<GameEvent>()
        var i = 0
        while (i < list.size) {
            val tag = list[i++] as Int
            val at = Instant.fromEpochMilliseconds(list[i++] as Long)
            restored.add(
                when (tag) {
                    0 -> GameEvent.Started(at)
                    1 -> GameEvent.TurnPassed(at)
                    2 -> GameEvent.Paused(at)
                    3 -> GameEvent.Resumed(at)
                    4 -> GameEvent.Reverted(at)
                    else -> error("unknown event tag $tag")
                }
            )
        }
        restored
    },
)
