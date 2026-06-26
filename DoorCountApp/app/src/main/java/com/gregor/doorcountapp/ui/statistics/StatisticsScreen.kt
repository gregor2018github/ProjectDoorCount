package com.gregor.doorcountapp.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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
    val hourlyOccupancy = remember(measurements) { vm.averageOccupancyRatePerHour() }
    val overallStats = remember(measurements) { vm.overallOccupancyStats() }

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

            GateOccupancyChart(rates = occupancyRates, total = vm.totalMeasurements)
            Spacer(modifier = Modifier.height(16.dp))
            DailyMeasurementsChart(data = dailyData)
            Spacer(modifier = Modifier.height(16.dp))
            HourlyDistributionChart(data = hourlyData)
            Spacer(modifier = Modifier.height(16.dp))
            HourlyOccupancyRateChart(data = hourlyOccupancy)
            Spacer(modifier = Modifier.height(16.dp))
            OverallOccupancyCard(occupied = overallStats.first, total = overallStats.second)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
private fun YAxisLabels(topLabel: String, height: Dp) {
    val style = MaterialTheme.typography.labelSmall
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Box(modifier = Modifier.width(32.dp).height(height)) {
        Text(topLabel, style = style, color = color, modifier = Modifier.align(Alignment.TopEnd))
        Text("0", style = style, color = color, modifier = Modifier.align(Alignment.BottomEnd))
    }
}

@Composable
private fun InfoText(text: String?) {
    if (text != null) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )
    } else {
        // Reserve space so the layout doesn't jump
        Text(
            text = " ",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 6.dp)
        )
    }
}

// ── Charts ────────────────────────────────────────────────────────────────────

@Composable
fun OverallOccupancyCard(occupied: Int, total: Int) {
    val rate = if (total > 0) occupied.toFloat() / total else 0f

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Overall Occupation",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
            Text(
                text = "${(rate * 100).toInt()}%",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$occupied/$total occupied",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun GateOccupancyChart(rates: List<Float>, total: Int = 0) {
    var selectedGate by remember { mutableStateOf<Int?>(null) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Gate Occupation Rate",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            InfoText(selectedGate?.let {
                val count = (rates[it] * total).toInt()
                "Gate ${it + 1}: $count/$total occupied"
            })

            rates.forEachIndexed { index, rate ->
                val isSelected = selectedGate == index
                val alpha = if (selectedGate == null || isSelected) 1f else 0.3f
                val barColor = primaryColor.copy(alpha = alpha)
                val bgColor = surfaceVariantColor.copy(alpha = alpha)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedGate = if (selectedGate == index) null else index }
                ) {
                    Text(
                        text = "Gate ${index + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.width(56.dp)
                    )
                    Canvas(
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                    ) {
                        drawRect(color = bgColor, size = size)
                        drawRect(
                            color = barColor,
                            size = Size(size.width * rate.coerceIn(0f, 1f), size.height)
                        )
                    }
                    Text(
                        text = "${(rate * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.width(40.dp).padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DailyMeasurementsChart(data: List<Pair<String, Int>>) {
    if (data.isEmpty()) return

    var selectedBar by remember { mutableStateOf<Int?>(null) }
    val barColor = MaterialTheme.colorScheme.secondary
    val gridColor = Color.Gray.copy(alpha = 0.2f)
    val maxCount = data.maxOf { it.second }.toFloat().coerceAtLeast(1f)
    val chartHeight = 120.dp

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
                modifier = Modifier.padding(bottom = 4.dp)
            )
            InfoText(selectedBar?.let { "${data[it].first.substring(5)}: ${data[it].second} measurements" })

            Row(verticalAlignment = Alignment.CenterVertically) {
                YAxisLabels(topLabel = maxCount.toInt().toString(), height = chartHeight)
                Spacer(modifier = Modifier.width(4.dp))
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(chartHeight)
                        .pointerInput(data) {
                            detectTapGestures { offset ->
                                val idx = (offset.x / (size.width.toFloat() / data.size))
                                    .toInt().coerceIn(0, data.size - 1)
                                selectedBar = if (selectedBar == idx) null else idx
                            }
                        }
                ) {
                    listOf(0.25f, 0.5f, 0.75f, 1f).forEach { frac ->
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, size.height * (1f - frac)),
                            end = Offset(size.width, size.height * (1f - frac)),
                            strokeWidth = 1f
                        )
                    }
                    val slotWidth = size.width / data.size
                    val barWidth = slotWidth * 0.75f
                    data.forEachIndexed { i, (_, count) ->
                        val isSelected = selectedBar == i
                        val alpha = if (selectedBar == null || isSelected) 1f else 0.3f
                        val barHeight = (count / maxCount) * size.height
                        drawRect(
                            color = barColor.copy(alpha = alpha),
                            topLeft = Offset(i * slotWidth + slotWidth * 0.125f, size.height - barHeight),
                            size = Size(barWidth, barHeight)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 36.dp, top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(data.first().first.substring(5), style = MaterialTheme.typography.labelSmall)
                Text(data.last().first.substring(5), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun HourlyDistributionChart(data: List<Int>) {
    var selectedBar by remember { mutableStateOf<Int?>(null) }
    val barColor = MaterialTheme.colorScheme.tertiary
    val gridColor = Color.Gray.copy(alpha = 0.2f)
    val maxCount = data.max().toFloat().coerceAtLeast(1f)
    val chartHeight = 100.dp

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
                modifier = Modifier.padding(bottom = 4.dp)
            )
            InfoText(selectedBar?.let { "${it}h–${it + 1}h: ${data[it]} measurements" })

            Row(verticalAlignment = Alignment.CenterVertically) {
                YAxisLabels(topLabel = maxCount.toInt().toString(), height = chartHeight)
                Spacer(modifier = Modifier.width(4.dp))
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(chartHeight)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val hour = (offset.x / (size.width.toFloat() / 24f))
                                    .toInt().coerceIn(0, 23)
                                selectedBar = if (selectedBar == hour) null else hour
                            }
                        }
                ) {
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
                            val isSelected = selectedBar == hour
                            val alpha = if (selectedBar == null || isSelected) 1f else 0.3f
                            val barHeight = (count / maxCount) * size.height
                            drawRect(
                                color = barColor.copy(alpha = alpha),
                                topLeft = Offset(hour * slotWidth + slotWidth * 0.1f, size.height - barHeight),
                                size = Size(barWidth, barHeight)
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 36.dp, top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("0h", "6h", "12h", "18h", "24h").forEach {
                    Text(text = it, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun HourlyOccupancyRateChart(data: List<Float?>) {
    if (data.none { it != null }) return

    var selectedBar by remember { mutableStateOf<Int?>(null) }
    val barColor = MaterialTheme.colorScheme.primary
    val gridColor = Color.Gray.copy(alpha = 0.2f)
    val chartHeight = 120.dp

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Avg. Occupation Rate by Hour",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Average share of gates occupied per hour of day",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            InfoText(
                selectedBar?.let { h ->
                    val rate = data[h]
                    if (rate != null) "${h}h–${h + 1}h: ${(rate * 100).toInt()}% avg. occupied"
                    else "${h}h–${h + 1}h: no data"
                }
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                YAxisLabels(topLabel = "100%", height = chartHeight)
                Spacer(modifier = Modifier.width(4.dp))
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(chartHeight)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val hour = (offset.x / (size.width.toFloat() / 24f))
                                    .toInt().coerceIn(0, 23)
                                selectedBar = if (selectedBar == hour) null else hour
                            }
                        }
                ) {
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
                    data.forEachIndexed { hour, rate ->
                        if (rate != null && rate > 0f) {
                            val isSelected = selectedBar == hour
                            val alpha = if (selectedBar == null || isSelected) 1f else 0.3f
                            drawRect(
                                color = barColor.copy(alpha = alpha),
                                topLeft = Offset(
                                    x = hour * slotWidth + slotWidth * 0.1f,
                                    y = size.height - rate * size.height
                                ),
                                size = Size(barWidth, rate * size.height)
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 36.dp, top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("0h", "6h", "12h", "18h", "24h").forEach {
                    Text(text = it, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
