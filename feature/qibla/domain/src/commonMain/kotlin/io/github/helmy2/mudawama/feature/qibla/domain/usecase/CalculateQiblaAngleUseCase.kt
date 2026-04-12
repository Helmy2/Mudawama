package io.github.helmy2.mudawama.feature.qibla.domain.usecase

import io.github.helmy2.mudawama.core.location.Coordinates
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class CalculateQiblaAngleUseCase {
    
    companion object {
        private val MECCA_LATITUDE = 21.4225
        private val MECCA_LONGITUDE = 39.8262
        
        private fun toRadians(deg: Double): Double = deg * PI / 180.0
        private fun toDegrees(rad: Double): Double = rad * 180.0 / PI
        private const val PI = 3.141592653589793238
    }
    
    operator fun invoke(origin: Coordinates): Double {
        val userLat = toRadians(origin.latitude)
        val userLon = toRadians(origin.longitude)
        val meccaLat = toRadians(MECCA_LATITUDE)
        val meccaLon = toRadians(MECCA_LONGITUDE)
        
        val dLon = meccaLon - userLon
        
        val y = sin(dLon) * cos(meccaLat)
        val x = cos(userLat) * sin(meccaLat) - sin(userLat) * cos(meccaLat) * cos(dLon)
        
        var bearing = toDegrees(atan2(y, x))
        bearing = (bearing + 360) % 360
        
        return bearing
    }
}