package cz.swdt.blok.example

import cz.swdt.blok.Bloc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.FlowCollector

sealed class Event {
    object Increment : Event()
    object Decrement : Event()
}

data class State(val value: Int = 0)

class MainBloc(scope: CoroutineScope) : Bloc<Event, State>(scope) {
    override val initialState: State = State()

    override suspend fun FlowCollector<State>.mapEventToState(event: Event) {
        when (event) {
            Event.Increment -> emit()
            Event.Decrement -> TODO()
        }
    }
}
