package com.bgclock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bgclock.game.GameConfig
import com.bgclock.game.Palette
import com.bgclock.game.Player
import com.bgclock.ui.theme.BgclockTheme
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun TimerScreen(
    config: GameConfig?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Timer (placeholder)", style = MaterialTheme.typography.headlineMedium)

        if (config == null) {
            Text("Game not configured. Go back to settings.")
        } else {
            Text("${config.players.size} players", style = MaterialTheme.typography.titleMedium)
            for (player in config.players) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(player.color),
                    )
                    Text(player.name)
                }
            }
            Text("Initial budget: ${config.initialTimeBudget}")
            Text("Turn increment: ${config.turnIncrement}")
        }

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to settings")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimerPreview() {
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
            onBack = {},
        )
    }
}
