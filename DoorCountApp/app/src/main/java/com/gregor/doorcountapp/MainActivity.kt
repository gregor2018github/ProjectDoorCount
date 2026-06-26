package com.gregor.doorcountapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.gregor.doorcountapp.ui.collection.CollectionScreen
import com.gregor.doorcountapp.ui.history.HistoryScreen
import com.gregor.doorcountapp.ui.statistics.StatisticsScreen
import com.gregor.doorcountapp.ui.theme.DoorCountTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoorCountTheme {
                DoorCountApp()
            }
        }
    }
}

@Composable
fun DoorCountApp() {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showHistory by rememberSaveable { mutableStateOf(false) }

    if (showHistory) {
        HistoryScreen(onBack = { showHistory = false })
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.GridOn, contentDescription = "Entry") },
                    label = { Text("Entry") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Statistics") },
                    label = { Text("Statistics") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> CollectionScreen(onOpenHistory = { showHistory = true })
                1 -> StatisticsScreen()
            }
        }
    }
}
