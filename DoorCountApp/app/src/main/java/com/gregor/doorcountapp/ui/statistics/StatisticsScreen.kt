package com.gregor.doorcountapp.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun StatisticsScreen(vm: StatisticsViewModel = viewModel()) {
    val measurements by vm.measurements.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadMeasurements()
    }

    val occupancyRates = remember(measurements) { vm.gateOccupancyRates() }
    val dailyData = remember(measurements) { vm.measurementsPerDay() }
    val hourlyData = remember(measurements) { vm.measurementsPerHour() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (measurements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No data yet.\nStart recording gate occupancy.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Text(
                text = "${vm.totalMeasurements} total measurements",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            GateOccupancyChart(rates = occupancyRates)
            Spacer(modifier = Modifier.height(16.dp))
            DailyMeasurementsChart(data = dailyData)
            Spacer(modifier = Modifier.height(16.dp))
            HourlyDistributionChart(data = hourlyData)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GateOccupancyChart(rates: List<Float>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Gate Occupation Rate",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            rates.forEachIndexed { index, rate ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Gate ${index + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(56.dp)
                    )
                    Canvas(
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                    ) {
                        drawRect(color = surfaceVariantColor, size = size)
                        drawRect(
                            color = primaryColor,
                            size = Size(size.width * rate.coerceIn(0f, 1f), size.height)
                        )
                    }
                    Text(
                        text = "${(rate * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .width(40.dp)
                            .padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DailyMeasurementsChart(data: List<Pair<String, Int>>) {
    if (data.isEmpty()) return
    val barColor = MaterialTheme.colorScheme.secondary
    val maxCount = data.maxOf { it.second }.toFloat().coerceAtLeast(1f)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Measurements Per Day",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "${data.size} days shown (last 30)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val slotWidth = size.width / data.size
                val barWidth = slotWidth * 0.75f
                val gap = slotWidth * 0.25f
                data.forEachIndexed { i, (_, count) ->
                    val barHeight = (count / maxCount) * size.height
                    drawRect(
                        color = barColor,
                        topLeft = Offset(i * slotWidth + gap / 2, size.height - barHeight),
                        size = Size(barWidth, barHeight)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = data.first().first.substring(5),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = data.last().first.substring(5),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun HourlyDistributionChart(data: List<Int>) {
    val barColor = MaterialTheme.colorScheme.tertiary
    val gridColor = Color.Gray.copy(alpha = 0.2f)
    val maxCount = data.max().toFloat().coerceAtLeast(1f)
    val totalByHour = data.sum()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Measurement Times",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Distribution by hour of day",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                // Subtle horizontal grid lines
                listOf(0.25f, 0.5f, 0.75f, 1f).forEach { frac ->
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, size.height * (1f - frac)),
                        end = Offset(size.width, size.height * (1f - frac)),
                        strokeWidth = 1f
                    )
                }
                val slotWidth = size.width / 24f
                val barWidth = slotWidth * 0.8f
                data.forEachIndexed { hour, count ->
                    if (count > 0) {
                        val barHeight = (count / maxCount) * size.height
                        drawRect(
                            color = barColor,
                            topLeft = Offset(hour * slotWidth + slotWidth * 0.1f, size.height - barHeight),
                            size = Size(barWidth, barHeight)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("0h", "6h", "12h", "18h", "24h").forEach {
                    Text(text = it, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
