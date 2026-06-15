package net.daddldiddl.jbsadventure.editor

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import net.daddldiddl.jbsadventure.editor.ui.EditorApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "JB's Adventure Editor"
    ) {
        EditorApp()
    }
}
