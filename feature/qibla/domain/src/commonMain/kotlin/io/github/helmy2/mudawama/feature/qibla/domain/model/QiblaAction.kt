package io.github.helmy2.mudawama.feature.qibla.domain.model

sealed class QiblaAction {
    object StartCompass : QiblaAction()
    object StopCompass : QiblaAction()
    object NavigateToSettings : QiblaAction()
    object RequestLocationPermission : QiblaAction()
}