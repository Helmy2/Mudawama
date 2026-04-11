package io.github.helmy2.mudawama.athkar.presentation.athkar

import androidx.lifecycle.viewModelScope
import io.github.helmy2.mudawama.athkar.domain.model.AthkarGroupType
import io.github.helmy2.mudawama.athkar.domain.usecase.GetAthkarGroupUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.IncrementAthkarItemUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.ObserveAthkarCompletionUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.ObserveAthkarLogUseCase
import io.github.helmy2.mudawama.athkar.domain.usecase.ResetAthkarItemUseCase
import io.github.helmy2.mudawama.core.presentation.mvi.MviViewModel
import io.github.helmy2.mudawama.core.time.TimeProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AthkarViewModel(
    private val getAthkarGroupUseCase: GetAthkarGroupUseCase,
    private val incrementAthkarItemUseCase: IncrementAthkarItemUseCase,
    private val resetAthkarItemUseCase: ResetAthkarItemUseCase,
    private val observeAthkarCompletionUseCase: ObserveAthkarCompletionUseCase,
    private val observeAthkarLogUseCase: ObserveAthkarLogUseCase,
    private val timeProvider: TimeProvider,
) : MviViewModel<AthkarUiState, AthkarUiAction, AthkarUiEvent>(
    initialState = AthkarUiState(today = timeProvider.logicalDate().toString()),
) {

    private var logObserverJob: Job? = null

    init {
        observeCompletion()
        observeAllGroupCounters()
    }

    private fun observeCompletion() {
        intent {
            observeAthkarCompletionUseCase(state.value.today).collect { statusMap ->
                reduce { copy(completionStatus = statusMap) }
            }
        }
    }

    private fun observeAllGroupCounters() {
        AthkarGroupType.entries.forEach { type ->
            intent {
                observeAthkarLogUseCase(type, state.value.today).collect { log ->
                    val counters = log?.counters ?: emptyMap()
                    reduce {
                        copy(allGroupCounters = allGroupCounters + (type to counters))
                    }
                }
            }
        }
    }

    override fun onAction(action: AthkarUiAction) {
        when (action) {
            is AthkarUiAction.OpenGroup -> openGroup(action.type)
            AthkarUiAction.CloseGroup -> {
                logObserverJob?.cancel()
                logObserverJob = null
                reduce { copy(activeGroup = null, activeGroupCounters = emptyMap()) }
            }
            is AthkarUiAction.IncrementItem -> incrementItem(action.itemId)
            is AthkarUiAction.ResetItem -> resetItem(action.itemId)
            is AthkarUiAction.SelectPrayerSlot -> reduce { copy(activePrayerSlot = action.slot) }
        }
    }

    private fun openGroup(type: AthkarGroupType) {
        val group = getAthkarGroupUseCase(type)
        reduce { copy(activeGroup = group, isLoading = true) }

        logObserverJob?.cancel()
        logObserverJob = viewModelScope.launch {
            observeAthkarLogUseCase(type, state.value.today).collectLatest { log ->
                reduce {
                    copy(
                        activeGroupCounters = log?.counters ?: emptyMap(),
                        isLoading = false,
                    )
                }
            }
        }
    }

    private fun incrementItem(itemId: String) {
        val group = state.value.activeGroup ?: return
        val wasComplete = state.value.completionStatus[group.type] ?: false
        val slot = if (group.type == AthkarGroupType.POST_PRAYER) state.value.activePrayerSlot else null

        exclusiveIntent("increment_$itemId") {
            incrementAthkarItemUseCase(group.type, state.value.today, itemId, slot)

            val isNowComplete = state.value.completionStatus[group.type] ?: false
            if (!wasComplete && isNowComplete) {
                emitEvent(AthkarUiEvent.GroupCompleted(group.type))
            }
        }
    }

    private fun resetItem(itemId: String) {
        val group = state.value.activeGroup ?: return
        val slot = if (group.type == AthkarGroupType.POST_PRAYER) state.value.activePrayerSlot else null

        exclusiveIntent("reset_$itemId") {
            resetAthkarItemUseCase(group.type, state.value.today, itemId, slot)
        }
    }
}
