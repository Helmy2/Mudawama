package io.github.helmy2.mudawama.feature.qibla.data.sensor

import io.github.helmy2.mudawama.feature.qibla.domain.model.CompassHeading
import kotlinx.coroutines.flow.Flow

expect class CompassSensorManager {
    fun observeHeading(): Flow<CompassHeading>
}