package com.gregor.doorcountapp.ui.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.gregor.doorcountapp.data.Measurement
import com.gregor.doorcountapp.data.MeasurementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MeasurementRepository(application)

    private val _measurements = MutableStateFlow<List<Measurement>>(emptyList())
    val measurements: StateFlow<List<Measurement>> = _measurements.asStateFlow()

    init {
        loadMeasurements()
    }

    fun loadMeasurements() {
        _measurements.value = repository.load()
    }

    fun gateOccupancyRates(): List<Float> {
        val list = _measurements.value
        if (list.isEmpty()) return List(6) { 0f }
        return List(6) { i -> list.count { it.gates[i] }.toFloat() / list.size }
    }

    fun measurementsPerDay(): List<Pair<String, Int>> {
        return _measurements.value
            .groupBy { it.timestamp.substring(0, 10) }
            .entries
            .sortedBy { it.key }
            .takeLast(30)
            .map { it.key to it.value.size }
    }

    fun measurementsPerHour(): List<Int> {
        val counts = IntArray(24)
        _measurements.value.forEach { m ->
            val hour = LocalDateTime.parse(m.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME).hour
            counts[hour]++
        }
        return counts.toList()
    }

    // Returns the average fraction of gates occupied for each hour (null = no data that hour).
    fun averageOccupancyRatePerHour(): List<Float?> {
        val groups = Array(24) { mutableListOf<Float>() }
        _measurements.value.forEach { m ->
            val hour = LocalDateTime.parse(m.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME).hour
            groups[hour].add(m.gates.count { it }.toFloat() / m.gates.size)
        }
        return groups.map { if (it.isEmpty()) null else it.average().toFloat() }
    }

    val totalMeasurements: Int get() = _measurements.value.size

    fun overallOccupancyStats(): Pair<Int, Int> {
        val list = _measurements.value
        val totalOccupied = list.sumOf { m -> m.gates.count { it } }
        val totalPossible = list.size * 6
        return totalOccupied to totalPossible
    }
}
