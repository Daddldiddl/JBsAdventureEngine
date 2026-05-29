package net.daddldiddl.jbsadventure.model

import kotlinx.serialization.Serializable

@Serializable
data class State(
    val stateKey: String,
    var currentValue: String,
    val description: String,
    val possibleValues: List<String>,
    val comment: String? = null
) {
    /**
     * Returns a debug-friendly name for the state, including its key.
     */
    fun debugName(): String {
        return "$stateKey (currentValue=$currentValue)"
    }

}
