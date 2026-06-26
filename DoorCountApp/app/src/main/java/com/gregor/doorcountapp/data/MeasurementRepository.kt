package com.gregor.doorcountapp.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class MeasurementRepository(private val file: File) {
    internal constructor(context: Context) : this(File(context.filesDir, "measurements.json"))

    private val gson = Gson()
    private val listType = object : TypeToken<List<Measurement>>() {}.type

    fun load(): List<Measurement> {
        if (!file.exists()) return emptyList()
        return try {
            gson.fromJson(file.readText(), listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun append(measurement: Measurement) {
        val list = load().toMutableList()
        list.add(measurement)
        save(list)
    }

    fun save(list: List<Measurement>) {
        file.writeText(gson.toJson(list))
    }
}
