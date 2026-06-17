package net.daddldiddl.jbsadventure.editor.io

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

object FileDialogs {
    fun chooseFileToOpen(): File? {
        val dialog = FileDialog(Frame(), "Open Adventure JSON", FileDialog.LOAD)
        dialog.file = "*.json"
        dialog.isVisible = true
        val fileName = dialog.file ?: return null
        val directory = dialog.directory ?: return null
        return File(directory, fileName)
    }

    fun chooseFileToSave(defaultName: String = "adventure.json"): File? {
        val dialog = FileDialog(Frame(), "Save Adventure JSON", FileDialog.SAVE)
        dialog.file = defaultName
        dialog.isVisible = true
        val fileName = dialog.file ?: return null
        val directory = dialog.directory ?: return null
        return File(directory, fileName)
    }
}
