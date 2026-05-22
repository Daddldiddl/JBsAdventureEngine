package net.daddldiddl.jbsadventure.tools
import net.daddldiddl.jbsadventure.LOG

/**
 * Utility class for console output. Provides a simple interface to print messages 
 * to the console while also logging them via the global [LOG] instance.
 */
class ConsoleOutput {
    public fun print(message: String?) {
        println(message)
        LOG.console(message ?: "")
    }

    public fun print() {
        println()
        LOG.console("")
    }
}
