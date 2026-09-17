package com.kwame.money

import android.content.ClipboardManager
import android.content.Context
import android.view.inputmethod.InputConnection

/**
 * Tracks recently copied text in memory so a future clipboard picker UI has
 * something to show. NOTE: in-memory only for now — history is lost if the
 * keyboard process dies. Persisting this with Room is planned for a later
 * phase (matches the original architecture plan), not done here yet.
 */
class ClipboardHistoryManager(private val context: Context) {

    private val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private val _history = mutableListOf<String>()
    val history: List<String> get() = _history

    private val maxHistory = 20

    private val listener = ClipboardManager.OnPrimaryClipChangedListener {
        val clip = clipboardManager.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).coerceToText(context)?.toString()
            if (!text.isNullOrBlank() && (_history.isEmpty() || _history[0] != text)) {
                _history.add(0, text)
                if (_history.size > maxHistory) {
                    _history.removeAt(_history.size - 1)
                }
            }
        }
    }

    fun startListening() {
        clipboardManager.addPrimaryClipChangedListener(listener)
    }

    fun stopListening() {
        clipboardManager.removePrimaryClipChangedListener(listener)
    }

    fun pasteFromHistory(ic: InputConnection, index: Int) {
        val text = _history.getOrNull(index) ?: return
        ic.commitText(text, 1)
    }
}
