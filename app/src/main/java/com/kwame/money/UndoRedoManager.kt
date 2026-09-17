package com.kwame.money

import android.view.inputmethod.InputConnection

/**
 * Records typing edits so they can be undone/redone. Works at the
 * character/operation level rather than diffing full text, which keeps it
 * cheap and reliable for a keyboard (no need to read the whole text field).
 */
sealed class TextEdit {
    data class Insert(val position: Int, val text: String) : TextEdit()
    data class Delete(val position: Int, val text: String) : TextEdit()
    data class Replace(val position: Int, val oldText: String, val newText: String) : TextEdit()
}

class UndoRedoManager {
    private val undoStack = ArrayDeque<TextEdit>()
    private val redoStack = ArrayDeque<TextEdit>()
    private val maxHistory = 100

    private fun push(edit: TextEdit) {
        undoStack.addLast(edit)
        if (undoStack.size > maxHistory) undoStack.removeFirst()
        redoStack.clear()
    }

    fun recordInsert(position: Int, text: String) = push(TextEdit.Insert(position, text))
    fun recordDelete(position: Int, text: String) = push(TextEdit.Delete(position, text))
    fun recordReplace(position: Int, oldText: String, newText: String) =
        push(TextEdit.Replace(position, oldText, newText))

    fun undo(ic: InputConnection): Boolean {
        val edit = undoStack.removeLastOrNull() ?: return false
        when (edit) {
            is TextEdit.Insert -> {
                ic.setSelection(edit.position, edit.position + edit.text.length)
                ic.commitText("", 1)
            }
            is TextEdit.Delete -> {
                ic.setSelection(edit.position, edit.position)
                ic.commitText(edit.text, 1)
            }
            is TextEdit.Replace -> {
                ic.setSelection(edit.position, edit.position + edit.newText.length)
                ic.commitText(edit.oldText, 1)
            }
        }
        redoStack.addLast(edit)
        return true
    }

    fun redo(ic: InputConnection): Boolean {
        val edit = redoStack.removeLastOrNull() ?: return false
        when (edit) {
            is TextEdit.Insert -> {
                ic.setSelection(edit.position, edit.position)
                ic.commitText(edit.text, 1)
            }
            is TextEdit.Delete -> {
                ic.setSelection(edit.position, edit.position + edit.text.length)
                ic.commitText("", 1)
            }
            is TextEdit.Replace -> {
                ic.setSelection(edit.position, edit.position + edit.oldText.length)
                ic.commitText(edit.newText, 1)
            }
        }
        undoStack.addLast(edit)
        return true
    }
}
