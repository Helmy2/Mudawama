package io.github.helmy2.mudawama.feature.qibla.domain.model

sealed class QiblaEvent {
    object NavigateToSettings : QiblaEvent()
}