package io.github.helmy2.mudawama.prayer.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AladhanResponseDto(
    val code: Int,
    val data: AladhanDataDto
)

@Serializable
data class AladhanDataDto(
    val timings: AladhanTimingsDto
)

@Serializable
data class AladhanTimingsDto(
    @SerialName("Fajr") val fajr: String,
    @SerialName("Dhuhr") val dhuhr: String,
    @SerialName("Asr") val asr: String,
    @SerialName("Maghrib") val maghrib: String,
    @SerialName("Isha") val isha: String
)
