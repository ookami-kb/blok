package cz.swdt.blok

interface BlocDelegate {
    fun <EVENT> onEvent(event: EVENT)
    fun <EVENT, STATE> onTransition(transition: Transition<EVENT, STATE>)
    fun onError(error: Throwable)
}
