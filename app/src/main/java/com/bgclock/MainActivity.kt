package com.bgclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bgclock.game.GameConfig
import com.bgclock.game.GameEvent
import com.bgclock.ui.screens.SettingsScreen
import com.bgclock.ui.screens.TimerScreen
import com.bgclock.ui.theme.BgclockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BgclockTheme {
                BgclockApp()
            }
        }
    }
}

private object Routes {
    const val SETTINGS = "settings"
    const val TIMER = "timer"
}

@Composable
private fun BgclockApp() {
    val navController = rememberNavController()
    var gameConfig by remember { mutableStateOf<GameConfig?>(null) }
    val events = remember { mutableStateListOf<GameEvent>() }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SETTINGS,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onStart = { config ->
                        gameConfig = config
                        events.clear()
                        navController.navigate(Routes.TIMER)
                    },
                )
            }
            composable(Routes.TIMER) {
                val config = gameConfig
                if (config == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    TimerScreen(
                        config = config,
                        events = events,
                        onAppendEvent = { events.add(it) },
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainPreview() {
    BgclockTheme {
        BgclockApp()
    }
}
