package cz.swdt.blok

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

abstract class Bloc<EVENT, STATE>(scope: CoroutineScope) : Flow<STATE> {
    abstract val initialState: STATE

    private val stateFlow by lazy { MutableStateFlow(initialState) }

    protected abstract suspend fun FlowCollector<STATE>.mapEventToState(event: EVENT)

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<STATE>) = stateFlow.collect(collector)

    private val eventChannel = Channel<EVENT>()

    suspend fun add(event: EVENT) {
        try {
            doOnEvent(event)
            eventChannel.send(event)
        } catch (e: Throwable) {
            doOnError(e)
        }
    }

    private suspend fun doOnEvent(event: EVENT) {
        BlocSupervisor.delegate?.onEvent(event)
        onEvent(event)
    }

    private suspend fun doOnTransition(transition: Transition<EVENT, STATE>) {
        BlocSupervisor.delegate?.onTransition(transition)
        onTransition(transition)
    }

    private suspend fun doOnError(error: Throwable) {
        BlocSupervisor.delegate?.onError(error)
        onError(error)
    }

    protected open suspend fun onTransition(transition: Transition<EVENT, STATE>) {}

    protected open suspend fun onError(error: Throwable) {}

    protected open suspend fun onEvent(event: EVENT) {}

    init {
        eventChannel.consumeAsFlow()
            .flatMapConcat { event ->
                flow<STATE> { mapEventToState(event) }
                    .catch { doOnError(it) }
                    .map { Transition(stateFlow.value, event, it) }
            }
            .onEach { transition ->
                println(transition)
                if (transition.currentState == transition.nextState) return@onEach
                try {
                    doOnTransition(transition)
                    stateFlow.value = transition.nextState
                } catch (e: Throwable) {
                    doOnError(e)
                }
            }
            .launchIn(scope)
    }
}
