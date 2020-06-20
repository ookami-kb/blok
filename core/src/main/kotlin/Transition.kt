data class Transition<EVENT, STATE>(val currentState: STATE, val event: EVENT, val nextState: STATE)
