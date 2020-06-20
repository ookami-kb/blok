import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class BlocTest {
    private val delegate = object : BlocDelegate {
        val events = mutableListOf<Any?>()
        val transitions = mutableListOf<Any?>()
        val errors = mutableListOf<Throwable>()

        override fun <EVENT> onEvent(event: EVENT) {
            events.add(event)
        }

        override fun <EVENT, STATE> onTransition(transition: Transition<EVENT, STATE>) {
            transitions.add(transition)
        }

        override fun onError(error: Throwable) {
            errors.add(error)
        }
    }

    @Before
    fun setUp() {
        BlocSupervisor.delegate = delegate
    }

    @Test
    fun `collects events and transitions properly`() {
        runBlocking {
            val scope = CoroutineScope(coroutineContext + Job())

            val bloc = object : Bloc<Int, String>(scope) {
                override val initialState: String = "0"

                override suspend fun FlowCollector<String>.mapEventToState(event: Int) {
                    emit(event.toString())
                }
            }
            bloc.add(1)
            bloc.add(2)
            bloc.add(3)

            scope.cancel()
        }

        Assert.assertEquals(listOf(1, 2, 3), delegate.events)

        val expectedTransitions = listOf(
            Transition("0", 1, "1"),
            Transition("1", 2, "2"),
            Transition("2", 3, "3")
        )
        Assert.assertEquals(expectedTransitions, delegate.transitions)

        Assert.assertTrue(delegate.errors.isEmpty())
    }

    @Test
    fun `collects errors properly`() {
        val error = IllegalArgumentException()
        runBlocking {
            val scope = CoroutineScope(coroutineContext + Job())

            val bloc = object : Bloc<Int, String>(scope) {
                override val initialState: String = "0"

                override suspend fun FlowCollector<String>.mapEventToState(event: Int) {
                    if (event == 2) throw error
                    emit(event.toString())
                }
            }
            bloc.add(1)
            bloc.add(2)
            bloc.add(3)

            scope.cancel()
        }

        Assert.assertEquals(listOf(1, 2, 3), delegate.events)

        val expectedTransitions = listOf(
            Transition("0", 1, "1"),
            Transition("1", 3, "3")
        )
        Assert.assertEquals(expectedTransitions, delegate.transitions)

        Assert.assertEquals(listOf(error), delegate.errors)
    }
}
