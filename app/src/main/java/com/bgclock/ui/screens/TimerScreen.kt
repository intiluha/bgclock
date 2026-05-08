package com.bgclock.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bgclock.ui.theme.BgclockTheme

@Composable
fun TimerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Timer", style = MaterialTheme.typography.headlineLarge)
        Text("(placeholder — game UI goes here)")
        Button(onClick = onBack, modifier = Modifier.padding(top = 24.dp)) {
            Text("Back to settings")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimerPreview() {
    BgclockTheme {
        TimerScreen(onBack = {})
    }
}
