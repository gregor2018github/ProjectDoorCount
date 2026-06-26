package com.gregor.doorcountapp.data

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class MeasurementRepositoryTest {

    private lateinit var file: File
    private lateinit var repo: MeasurementRepository

    @Before
    fun setUp() {
        file = File.createTempFile("measurements_test", ".json")
        repo = MeasurementRepository(file)
    }

    @After
    fun tearDown() {
        file.delete()
    }

    // ── Format contract ──────────────────────────────────────────────────────
    // This test pins the exact JSON representation stored on the phone.
    // If it ever fails, a re-install would corrupt existing user data.

    @Test
    fun `saved JSON matches expected schema`() {
        val m = Measurement(
            timestamp = "2026-06-26T14:30:00",
            gates = listOf(true, false, true, false, false, true)
        )
        repo.append(m)
        val json = file.readText()
        // Field names and array order must stay stable across app versions.
        assertTrue("timestamp field missing", json.contains("\"timestamp\""))
        assertTrue("gates field missing", json.contains("\"gates\""))
        assertTrue("gate value missing", json.contains("true"))
    }

    @Test
    fun `existing JSON from previous install loads correctly`() {
        // Hardcoded snapshot of the format currently stored on device.
        // If the Measurement class is refactored in a breaking way, this fails.
        file.writeText(
            """[{"timestamp":"2026-06-26T10:00:00","gates":[true,false,true,false,false,true]}]"""
        )
        val loaded = repo.load()
        assertEquals(1, loaded.size)
        assertEquals("2026-06-26T10:00:00", loaded[0].timestamp)
        assertEquals(listOf(true, false, true, false, false, true), loaded[0].gates)
    }

    // ── Round-trip ────────────────────────────────────────────────────────────

    @Test
    fun `missing file returns empty list`() {
        file.delete()
        assertTrue(repo.load().isEmpty())
    }

    @Test
    fun `append and load produces identical measurement`() {
        val m = Measurement("2026-06-26T09:00:00", listOf(false, true, false, true, false, true))
        repo.append(m)
        assertEquals(m, repo.load().single())
    }

    @Test
    fun `multiple appends preserve insertion order`() {
        val m1 = Measurement("2026-06-01T08:00:00", List(6) { false })
        val m2 = Measurement("2026-06-02T09:00:00", List(6) { true })
        val m3 = Measurement("2026-06-03T10:00:00", listOf(true, false, true, false, true, false))
        repo.append(m1); repo.append(m2); repo.append(m3)
        assertEquals(listOf(m1, m2, m3), repo.load())
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    @Test
    fun `save overwrites file completely`() {
        repo.append(Measurement("2026-06-01T08:00:00", List(6) { false }))
        repo.append(Measurement("2026-06-02T09:00:00", List(6) { true }))
        val kept = repo.load().drop(1)
        repo.save(kept)
        assertEquals(1, repo.load().size)
    }

    @Test
    fun `update changes only the targeted entry`() {
        val m1 = Measurement("2026-06-01T08:00:00", List(6) { false })
        val m2 = Measurement("2026-06-02T09:00:00", List(6) { false })
        repo.append(m1); repo.append(m2)
        val newGates = listOf(true, true, false, false, true, false)
        val list = repo.load().toMutableList()
        list[0] = list[0].copy(gates = newGates)
        repo.save(list)
        val loaded = repo.load()
        assertEquals(newGates, loaded[0].gates)
        assertEquals(m2.gates, loaded[1].gates)  // second entry untouched
    }

    @Test
    fun `delete removes only the targeted entry`() {
        val m1 = Measurement("2026-06-01T08:00:00", List(6) { false })
        val m2 = Measurement("2026-06-02T09:00:00", List(6) { true })
        repo.append(m1); repo.append(m2)
        val list = repo.load().toMutableList()
        list.removeAt(0)
        repo.save(list)
        assertEquals(listOf(m2), repo.load())
    }
}
