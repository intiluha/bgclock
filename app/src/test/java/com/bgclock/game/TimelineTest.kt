@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.bgclock.game

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class TimelineTest {
    private val alice = Player("Alice", Color.Red)
    private val bob = Player("Bob", Color.Blue)
    private val config = GameConfig(
        players = listOf(alice, bob),
        initialTimeBudget = 30.minutes,
        turnIncrement = 1.minutes,
    )
    private val t0: Instant = Instant.parse("2026-05-07T20:00:00Z")
    private fun t(seconds: Long): Instant = t0 + seconds.seconds
    private val utc: ZoneId = ZoneOffset.UTC

    @Test fun hms_zero() = assertEquals("0s", Duration.ZERO.formatHmsCompact())
    @Test fun hms_secondsOnly() = assertEquals("45s", 45.seconds.formatHmsCompact())
    @Test fun hms_minutesOnly() = assertEquals("30m", 30.minutes.formatHmsCompact())
    @Test fun hms_minutesAndSeconds() =
        assertEquals("28m53s", (28.minutes + 53.seconds).formatHmsCompact())
    @Test fun hms_hoursAndMinutes() = assertEquals("1h30m", 90.minutes.formatHmsCompact())
    @Test fun hms_skipsZeroMinutesWithHours() = assertEquals("1h5s", 3605.seconds.formatHmsCompact())
    @Test fun hms_negative() = assertEquals("-1m5s", (-65).seconds.formatHmsCompact())

    @Test
    fun timestamp_formatsInRequestedZone() {
        assertEquals("2026-05-07 20:00:00", formatTimelineTimestamp(t0, utc))
        assertEquals("2026-05-07 22:00:00", formatTimelineTimestamp(t0, ZoneOffset.ofHours(2)))
    }

    @Test
    fun row_started() {
        val events = listOf(GameEvent.Started(t0))
        assertEquals(
            "2026-05-07 20:00:00; Game start; Alice 31m; Bob 30m",
            formatTimelineRow(config, events, 0, utc),
        )
    }

    @Test
    fun row_turnPassed_namesNextPlayer() {
        val events = listOf(GameEvent.Started(t0), GameEvent.TurnPassed(t(127)))
        assertEquals(
            "2026-05-07 20:02:07; Turn passed (to Bob); Alice 28m53s; Bob 31m",
            formatTimelineRow(config, events, 1, utc),
        )
    }

    @Test
    fun row_pauseAndResume() {
        val events = listOf(
            GameEvent.Started(t0),
            GameEvent.TurnPassed(t(127)),
            GameEvent.Paused(t(180)),
            GameEvent.Resumed(t(480)),
        )
        assertEquals(
            "2026-05-07 20:03:00; Pause; Alice 28m53s; Bob 30m7s",
            formatTimelineRow(config, events, 2, utc),
        )
        assertEquals(
            "2026-05-07 20:08:00; Resume; Alice 28m53s; Bob 30m7s",
            formatTimelineRow(config, events, 3, utc),
        )
    }

    @Test
    fun claudeMdScenario_walkAllRows() {
        val events = listOf(
            GameEvent.Started(t0),
            GameEvent.TurnPassed(t(127)),
            GameEvent.Paused(t(180)),
            GameEvent.Resumed(t(480)),
            GameEvent.TurnPassed(t(1080)),
            GameEvent.TurnPassed(t(1081)),
            GameEvent.Reverted(t(1140)),
            GameEvent.TurnPassed(t(1397)),
        )
        val expected = listOf(
            "2026-05-07 20:00:00; Game start; Alice 31m; Bob 30m",
            "2026-05-07 20:02:07; Turn passed (to Bob); Alice 28m53s; Bob 31m",
            "2026-05-07 20:03:00; Pause; Alice 28m53s; Bob 30m7s",
            "2026-05-07 20:08:00; Resume; Alice 28m53s; Bob 30m7s",
            "2026-05-07 20:18:00; Turn passed (to Alice); Alice 29m53s; Bob 20m7s",
            "2026-05-07 20:18:01; Turn passed (to Bob); Alice 29m52s; Bob 21m7s",
            "2026-05-07 20:19:00; Revert; Alice 28m53s; Bob 20m7s",
            "2026-05-07 20:23:17; Turn passed (to Bob); Alice 24m36s; Bob 21m7s",
        )
        for (i in events.indices) {
            assertEquals(expected[i], formatTimelineRow(config, events, i, utc))
        }
    }
}
