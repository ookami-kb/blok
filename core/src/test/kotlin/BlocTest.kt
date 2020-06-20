import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.junit.Test

class BlocTest {
    @Test
    fun testClass() {
        val scope = CoroutineScope(Dispatchers.Default)
        val bloc = object : Bloc<Int, String>(scope) {
            override val initialState: String = "0"

            override suspend fun FlowCollector<String>.mapEventToState(event: Int) {
                if (event == 2) throw IllegalArgumentException()
                emit(event.toString())
            }
        }
        bloc.onEach { println(it) }.launchIn(scope)
        runBlocking {
            bloc.add(1)
            bloc.add(2)
            bloc.add(3)
            bloc.add(4)
            bloc.add(5)
        }
    }
}
