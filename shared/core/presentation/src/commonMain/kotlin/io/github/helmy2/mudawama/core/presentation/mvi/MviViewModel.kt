package io.github.helmy2.mudawama.core.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class MviViewModel<S, A, E>(
    initialState: S,
) : ViewModel() {
    private val _stateFlow = MutableStateFlow(initialState)
    val state: StateFlow<S> = _stateFlow.asStateFlow()

    private val _eventChannel = Channel<E>(UNLIMITED)
    val eventFlow: Flow<E> = _eventChannel.receiveAsFlow()

    private val exclusiveJobs = mutableMapOf<String, Job>()

    abstract fun onAction(action: A)

    protected fun reduce(block: S.() -> S) {
        _stateFlow.value = _stateFlow.value.block()
    }

    protected fun intent(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }

    protected fun exclusiveIntent(
        key: String = DEFAULT_EXCLUSIVE_KEY,
        block: suspend () -> Unit,
    ) {
        exclusiveJobs[key]?.cancel()
        val job = viewModelScope.launch { block() }
        exclusiveJobs[key] = job
        job.invokeOnCompletion {
            if (exclusiveJobs[key] === job) {
                exclusiveJobs.remove(key)
            }
        }
    }

    protected suspend fun emitEvent(event: E) {
        _eventChannel.send(event)
    }

    private companion object {
        const val DEFAULT_EXCLUSIVE_KEY = "default"
    }
}
