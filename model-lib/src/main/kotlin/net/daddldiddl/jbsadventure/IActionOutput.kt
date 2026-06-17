package net.daddldiddl.jbsadventure

/**
 * Output sink for player-visible action text emitted from the model layer.
 *
 * Implemented by the engine layer so model classes can surface action messages
 * without depending on engine-specific console types.
 */
interface IActionOutput {
    fun print(message: String?)

    companion object {
        @Volatile
        var current: IActionOutput = NoOpActionOutput
    }

    object NoOpActionOutput : IActionOutput {
        override fun print(message: String?) {}
    }
}
