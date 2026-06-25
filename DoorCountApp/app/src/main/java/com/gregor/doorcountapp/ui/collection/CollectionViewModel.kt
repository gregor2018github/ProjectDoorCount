package com.gregor.doorcountapp.ui.collection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.gregor.doorcountapp.data.Measurement
import com.gregor.doorcountapp.data.MeasurementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CollectionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MeasurementRepository(application)

    private val _gateStates = MutableStateFlow(List(6) { false })
    val gateStates: StateFlow<List<Boolean>> = _gateStates.asStateFlow()

    private val _savedMessage = MutableStateFlow<String?>(null)
    val savedMessage: StateFlow<String?> = _savedMessage.asStateFlow()

    fun toggle(index: Int) {
        _gateStates.value = _gateStates.value.toMutableList().also { it[index] = !it[index] }
    }

    fun saveAndReset() {
        val measurement = Measurement(
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            gates = _gateStates.value.toList()
        )
        repository.append(measurement)
        _gateStates.value = List(6) { false }
        _savedMessage.value = "Saved"
    }

    fun clearMessage() {
        _savedMessage.value = null
    }
}
