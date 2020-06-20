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

    suspend fun add(event: EVENT) = eventChannel.send(event)

    private suspend fun doOnTransition(transition: Transition<EVENT, STATE>) {
        onTransition(transition)
    }

    private suspend fun doOnError(error: Throwable) {
        onError(error)
    }

    protected open suspend fun onTransition(transition: Transition<EVENT, STATE>) {}

    protected open suspend fun onError(error: Throwable) {}

    init {
        eventChannel.consumeAsFlow()
            .flatMapConcat { event ->
                flow<STATE> { mapEventToState(event) }
                    .catch { doOnError(it) }
                    .map { Transition(stateFlow.value, event, it) }
            }
            .onEach { transition ->
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
