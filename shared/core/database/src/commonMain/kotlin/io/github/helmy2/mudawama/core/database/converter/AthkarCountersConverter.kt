package io.github.helmy2.mudawama.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal object AthkarCountersConverter {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromMap(counters: Map<String, Int>): String = json.encodeToString(counters)

    @TypeConverter
    fun toMap(json: String): Map<String, Int> =
        try {
            AthkarCountersConverter.json.decodeFromString(json)
        } catch (_: Exception) {
            emptyMap()
        }
}
