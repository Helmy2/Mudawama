package io.github.helmy2.mudawama.athkar.presentation.notification

import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.athkar.domain.model.NotificationPreference
import io.github.helmy2.mudawama.athkar.domain.usecase.ObserveNotificationPreferenceUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.SaveNotificationPreferenceUseCase
import io.github.helmy2.mudawama.core.domain.Result
import io.github.helmy2.mudawama.core.domain.notification.NotificationPermissionChecker
import io.github.helmy2.mudawama.core.domain.notification.NotificationPermissionResult
import io.github.helmy2.mudawama.core.presentation.mvi.MviViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AthkarNotificationViewModel(
    private val observeNotificationPreferenceUseCase: ObserveNotificationPreferenceUseCase,
    private val saveNotificationPreferenceUseCase: SaveNotificationPreferenceUseCase,
    private val permissionChecker: NotificationPermissionChecker,
    private val dispatcher: CoroutineDispatcher,
) : MviViewModel<AthkarNotificationUiState, AthkarNotificationUiAction, AthkarNotificationUiEvent>(
    initialState = AthkarNotificationUiState(),
) {

    init {
        observeMorning()
        observeEvening()
    }

    private fun observeMorning() {
        intent {
            observeNotificationPreferenceUseCase(AthkarGroupType.MORNING).collect { pref ->
                reduce { copy(morningPreference = pref) }
            }
        }
    }

    private fun observeEvening() {
        intent {
            observeNotificationPreferenceUseCase(AthkarGroupType.EVENING).collect { pref ->
                reduce { copy(eveningPreference = pref) }
            }
        }
    }

    override fun onAction(action: AthkarNotificationUiAction) {
        when (action) {
            is AthkarNotificationUiAction.ToggleMorning -> savePreference(
                preference = state.value.morningPreference.copy(enabled = action.enabled),
                title = action.title,
                body = action.body,
            )
            is AthkarNotificationUiAction.ToggleEvening -> savePreference(
                preference = state.value.eveningPreference.copy(enabled = action.enabled),
                title = action.title,
                body = action.body,
            )
            is AthkarNotificationUiAction.SetMorningTime -> savePreference(
                preference = state.value.morningPreference.copy(
                    hour = action.hour,
                    minute = action.minute,
                    enabled = true,
                ),
                title = action.title,
                body = action.body,
            )
            is AthkarNotificationUiAction.SetEveningTime -> savePreference(
                preference = state.value.eveningPreference.copy(
                    hour = action.hour,
                    minute = action.minute,
                    enabled = true,
                ),
                title = action.title,
                body = action.body,
            )
        }
    }

    private fun savePreference(preference: NotificationPreference, title: String, body: String) {
        exclusiveIntent("save_${preference.groupType}") {
            // Only check permission when enabling; disabling should always succeed.
            if (preference.enabled) {
                val permissionResult = withContext(dispatcher) {
                    permissionChecker.requestPermission()
                }
                if (permissionResult is NotificationPermissionResult.Denied) {
                    emitEvent(AthkarNotificationUiEvent.PermissionDenied)
                    return@exclusiveIntent
                }
            }

            val result = withContext(dispatcher) {
                saveNotificationPreferenceUseCase(preference, title, body)
            }
            if (result is Result.Failure) {
                emitEvent(AthkarNotificationUiEvent.SaveError)
            }
        }
    }
}
