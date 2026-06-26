package com.gregor.doorcountapp.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.gregor.doorcountapp.data.Measurement
import com.gregor.doorcountapp.data.MeasurementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MeasurementRepository(application)

    private val _measurements = MutableStateFlow<List<Measurement>>(emptyList())
    val measurements: StateFlow<List<Measurement>> = _measurements.asStateFlow()

    init {
        load()
    }

    fun load() {
        _measurements.value = repository.load()
    }

    fun delete(index: Int) {
        val list = _measurements.value.toMutableList()
        list.removeAt(index)
        repository.save(list)
        _measurements.value = list
    }

    fun update(index: Int, gates: List<Boolean>) {
        val list = _measurements.value.toMutableList()
        list[index] = list[index].copy(gates = gates)
        repository.save(list)
        _measurements.value = list
    }
}
