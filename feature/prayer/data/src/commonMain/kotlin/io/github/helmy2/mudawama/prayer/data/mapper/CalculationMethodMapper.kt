package io.github.helmy2.mudawama.prayer.data.mapper

import io.github.helmy2.mudawama.settings.domain.CalculationMethod

fun CalculationMethod.toAladhanMethodId(): String = when (this) {
    CalculationMethod.MUSLIM_WORLD_LEAGUE -> "2"
    CalculationMethod.EGYPTIAN -> "5"
    CalculationMethod.UMM_AL_QURA -> "4"
    CalculationMethod.KARACHI -> "1"
    CalculationMethod.ISNA -> "2"  // ISNA uses MWL
    CalculationMethod.DUBAI -> "8"
    CalculationMethod.KUWAIT -> "9"
    CalculationMethod.QATAR -> "10"
    CalculationMethod.MOON_SIGHTING_COMMITTEE -> "11"
    CalculationMethod.SINGAPORE -> "3"
    CalculationMethod.TURKEY -> "6"
    CalculationMethod.TEHRAN -> "7"
}
