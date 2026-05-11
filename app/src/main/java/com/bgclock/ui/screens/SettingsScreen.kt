package com.bgclock.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt
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

    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var cardHeightPx by remember { mutableFloatStateOf(0f) }
    val spacingPx = with(LocalDensity.current) { 16.dp.toPx() }
    val haptic = LocalHapticFeedback.current

    var pendingColorChange by remember { mutableStateOf<Pair<Int, Color>?>(null) }

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
            val isDragging = draggingIndex == i
            val scale by animateFloatAsState(
                targetValue = if (isDragging) 1.04f else 1f,
                label = "playerCardScale",
            )
            val elevation by animateFloatAsState(
                targetValue = if (isDragging) 8f else 0f,
                label = "playerCardElevation",
            )
            PlayerCard(
                index = i,
                name = names[i],
                onNameChange = { names[i] = it },
                selectedColor = colors[i],
                onColorChange = { color ->
                    val duplicate = (0 until playerCount).any { j -> j != i && colors[j] == color }
                    if (duplicate) {
                        pendingColorChange = i to color
                    } else {
                        colors[i] = color
                    }
                },
                modifier = Modifier
                    .zIndex(if (isDragging) 1f else 0f)
                    .offset { IntOffset(0, if (isDragging) dragOffsetY.roundToInt() else 0) }
                    .scale(scale)
                    .shadow(elevation.dp, MaterialTheme.shapes.medium)
                    .onSizeChanged { cardHeightPx = it.height.toFloat() },
                dragHandleModifier = Modifier.pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            draggingIndex = i
                            dragOffsetY = 0f
                        },
                        onDragEnd = {
                            draggingIndex = null
                            dragOffsetY = 0f
                        },
                        onDragCancel = {
                            draggingIndex = null
                            dragOffsetY = 0f
                        },
                        onDrag = { change, drag ->
                            change.consume()
                            dragOffsetY += drag.y
                            val di = draggingIndex ?: return@detectDragGesturesAfterLongPress
                            val gap = cardHeightPx + spacingPx
                            if (gap <= 0f) return@detectDragGesturesAfterLongPress
                            if (dragOffsetY > gap / 2 && di < playerCount - 1) {
                                names.swap(di, di + 1)
                                colors.swap(di, di + 1)
                                draggingIndex = di + 1
                                dragOffsetY -= gap
                            } else if (dragOffsetY < -gap / 2 && di > 0) {
                                names.swap(di, di - 1)
                                colors.swap(di, di - 1)
                                draggingIndex = di - 1
                                dragOffsetY += gap
                            }
                        },
                    )
                },
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

    pendingColorChange?.let { (idx, color) ->
        AlertDialog(
            onDismissRequest = { pendingColorChange = null },
            title = { Text("Use the same color twice?") },
            text = { Text("Another player already uses this color. Continue anyway?") },
            confirmButton = {
                TextButton(onClick = {
                    colors[idx] = color
                    pendingColorChange = null
                }) { Text("Use anyway") }
            },
            dismissButton = {
                TextButton(onClick = { pendingColorChange = null }) { Text("Cancel") }
            },
        )
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
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier,
) {
    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = dragHandleModifier
                    .width(20.dp)
                    .height(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("⋮⋮", style = MaterialTheme.typography.titleMedium)
            }
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

private fun <T> MutableList<T>.swap(a: Int, b: Int) {
    val tmp = this[a]
    this[a] = this[b]
    this[b] = tmp
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
                            .then(
                                if (isSelected) {
                                    Modifier.border(3.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                                } else {
                                    Modifier
                                }
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
