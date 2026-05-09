package com.bgclock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bgclock.game.GameConfig
import com.bgclock.game.Palette
import com.bgclock.game.Player
import com.bgclock.ui.theme.BgclockTheme
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun SettingsScreen(
    onStart: (GameConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    val maxPlayers = Palette.all.size
    var playerCount by remember { mutableIntStateOf(2) }
    val names = remember { List(maxPlayers) { "" }.toMutableStateList() }
    val colors = remember { Palette.all.toMutableStateList() }
    var initialMinutes by remember { mutableIntStateOf(30) }
    var incrementSeconds by remember { mutableIntStateOf(30) }

    val canStart = (0 until playerCount).all { names[it].trim().isNotEmpty() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        StepperRow(
            label = "Players: $playerCount",
            decEnabled = playerCount > 2,
            incEnabled = playerCount < maxPlayers,
            onDec = { playerCount-- },
            onInc = { playerCount++ },
        )

        for (i in 0 until playerCount) {
            PlayerCard(
                index = i,
                name = names[i],
                onNameChange = { names[i] = it },
                selectedColor = colors[i],
                onColorChange = { colors[i] = it },
            )
        }

        DurationRow(
            label = "Time per player",
            value = initialMinutes,
            onValueChange = { initialMinutes = it },
            range = 1..240,
            unit = "min",
        )

        DurationRow(
            label = "Turn increment",
            value = incrementSeconds,
            onValueChange = { incrementSeconds = it },
            range = 0..300,
            unit = "s",
        )

        Button(
            enabled = canStart,
            onClick = {
                val players = (0 until playerCount).map { Player(names[it].trim(), colors[it]) }
                onStart(GameConfig(players, initialMinutes.minutes, incrementSeconds.seconds))
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Start")
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    decEnabled: Boolean,
    incEnabled: Boolean,
    onDec: () -> Unit,
    onInc: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        StepperButton(text = "−", enabled = decEnabled, onClick = onDec)
        StepperButton(text = "+", enabled = incEnabled, onClick = onInc)
    }
}

@Composable
private fun RowScope.StepperButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    FilledIconButton(enabled = enabled, onClick = onClick) {
        Text(text, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun DurationRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    unit: String,
) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            OutlinedTextField(
                value = text,
                onValueChange = { input ->
                    text = input
                    val parsed = input.toIntOrNull()
                    if (parsed != null && parsed in range) {
                        onValueChange(parsed)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text(unit) },
                modifier = Modifier
                    .width(112.dp)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) text = value.toString()
                    },
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt().coerceIn(range)) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
        )
    }
}

@Composable
private fun PlayerCard(
    index: Int,
    name: String,
    onNameChange: (String) -> Unit,
    selectedColor: Color,
    onColorChange: (Color) -> Unit,
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text("Player ${index + 1}") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            ColorSwatchGrid(
                selectedColor = selectedColor,
                onColorChange = onColorChange,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ColorSwatchGrid(
    selectedColor: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    val perRow = (Palette.all.size + 1) / 2
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (rowColors in Palette.all.chunked(perRow)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (color in rowColors) {
                    val isSelected = color == selectedColor
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.onBackground,
                                shape = CircleShape,
                            )
                            .clickable { onColorChange(color) },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    BgclockTheme {
        SettingsScreen(onStart = {})
    }
}
