@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.bgclock.game

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class GameStateTest {
    private val alice = Player("Alice", Color.Red)
    private val bob = Player("Bob", Color.Blue)
    private val config = GameConfig(
        players = listOf(alice, bob),
        initialTimeBudget = 30.minutes,
        turnIncrement = 1.minutes,
    )
    private val t0: Instant = Instant.parse("2026-05-07T20:00:00Z")
    private fun t(seconds: Long): Instant = t0 + seconds.seconds

    @Test
    fun emptyEventsBeforeStart_isUnstarted() {
        val s = config.stateAt(emptyList(), t0)
        assertFalse(s.isStarted)
        assertNull(s.activePlayerIndex)
        assertEquals(listOf(30.minutes, 30.minutes), s.remainingTimes)
    }

    @Test
    fun started_givesStartingPlayerTheIncrement() {
        val s = config.stateAt(listOf(GameEvent.Started(t0)), t0)
        assertTrue(s.isStarted)
        assertEquals(0, s.activePlayerIndex)
        assertEquals(31.minutes, s.remainingTimes[0])
        assertEquals(30.minutes, s.remainingTimes[1])
    }

    @Test
    fun activePlayerLosesTimeAsItPasses() {
        val s = config.stateAt(
            events = listOf(GameEvent.Started(t0)),
            now = t(127),
        )
        assertEquals(31.minutes - 127.seconds, s.remainingTimes[0])
        assertEquals(30.minutes, s.remainingTimes[1])
    }

    @Test
    fun turnPassed_addsIncrementToReceiverAndChargesSender() {
        val s = config.stateAt(
            events = listOf(
                GameEvent.Started(t0),
                GameEvent.TurnPassed(t(127)),
            ),
            now = t(127),
        )
        assertEquals(1, s.activePlayerIndex)
        assertEquals(28.minutes + 53.seconds, s.remainingTimes[0])
        assertEquals(31.minutes, s.remainingTimes[1])
    }

    @Test
    fun pause_stopsTheClock_resumeRestarts() {
        val s = config.stateAt(
            events = listOf(
                GameEvent.Started(t0),
                GameEvent.Paused(t(60)),
                GameEvent.Resumed(t(360)),
            ),
            now = t(420),
        )
        assertEquals(31.minutes - 120.seconds, s.remainingTimes[0])
    }

    @Test
    fun revert_undoesLastEvent_butKeepsItInLog() {
        val events = listOf(
            GameEvent.Started(t0),
            GameEvent.TurnPassed(t(127)),
            GameEvent.Reverted(t(180)),
        )
        val s = config.stateAt(events, t(180))
        assertEquals(0, s.activePlayerIndex)
        assertEquals(31.minutes - 180.seconds, s.remainingTimes[0])
        assertEquals(30.minutes, s.remainingTimes[1])
    }

    @Test
    fun consecutiveReverts_eachUndoOneEvent() {
        val events = listOf(
            GameEvent.Started(t0),
            GameEvent.TurnPassed(t(60)),
            GameEvent.TurnPassed(t(120)),
            GameEvent.Reverted(t(125)),
            GameEvent.Reverted(t(130)),
        )
        val s = config.stateAt(events, t(130))
        assertEquals(0, s.activePlayerIndex)
        assertEquals(31.minutes - 130.seconds, s.remainingTimes[0])
        assertEquals(30.minutes, s.remainingTimes[1])
    }

    @Test
    fun rejects_eventBeforeStarted() {
        assertThrows(IllegalArgumentException::class.java) {
            config.stateAt(listOf(GameEvent.TurnPassed(t0)), t0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            config.stateAt(listOf(GameEvent.Paused(t0)), t0)
        }
    }

    @Test
    fun rejects_secondStarted() {
        assertThrows(IllegalArgumentException::class.java) {
            config.stateAt(listOf(GameEvent.Started(t0), GameEvent.Started(t(1))), t(1))
        }
    }

    @Test
    fun rejects_doublePause() {
        assertThrows(IllegalArgumentException::class.java) {
            config.stateAt(
                events = listOf(
                    GameEvent.Started(t0),
                    GameEvent.Paused(t(10)),
                    GameEvent.Paused(t(20)),
                ),
                now = t(20),
            )
        }
    }

    @Test
    fun rejects_resumeWithoutPause() {
        assertThrows(IllegalArgumentException::class.java) {
            config.stateAt(
                events = listOf(
                    GameEvent.Started(t0),
                    GameEvent.Resumed(t(10)),
                ),
                now = t(10),
            )
        }
    }

    @Test
    fun claudeMdScenario_endToEnd() {
        val events = listOf(
            GameEvent.Started(t(0)),
            GameEvent.TurnPassed(t(127)),
            GameEvent.Paused(t(180)),
            GameEvent.Resumed(t(480)),
            GameEvent.TurnPassed(t(1080)),
            GameEvent.TurnPassed(t(1081)),
            GameEvent.Reverted(t(1140)),
            GameEvent.TurnPassed(t(1397)),
        )
        val s = config.stateAt(events, t(1397))
        assertEquals(1, s.activePlayerIndex)
        assertEquals(24.minutes + 36.seconds, s.remainingTimes[0])
        assertEquals(21.minutes + 7.seconds, s.remainingTimes[1])
    }
}
