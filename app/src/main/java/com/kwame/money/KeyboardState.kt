package com.kwame.money

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Holds the keyboard's live UI state. Uses Compose's mutableStateOf so any
 * change here automatically triggers recomposition of MoneyKeyboard.
 */
class KeyboardState {
    var isShifted by mutableStateOf(false)
    var isCapsLocked by mutableStateOf(false)
    var isSymbolsMode by mutableStateOf(false)
    var isEmojiPanelOpen by mutableStateOf(false)
    var isAiPanelOpen by mutableStateOf(false)

    /** True for password/PIN fields or fields that request no personalized
     * learning. When true: no AI actions, no word suggestions, no learning. */
    var isSensitiveField by mutableStateOf(false)

    /** One-shot shift resets after a single letter; caps lock stays on. */
    fun onLetterCommitted() {
        if (isShifted && !isCapsLocked) {
            isShifted = false
        }
    }
}
