package com.kwame.money

import android.inputmethodservice.InputMethodService
import android.os.SystemClock
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle

class MoneyKeyboardService : InputMethodService() {

    private val lifecycleOwner = KeyboardLifecycleOwner()
    private val keyboardState = KeyboardState()

    private var lastSpaceTapTime = 0L
    private var lastShiftTapTime = 0L
    private var currentEnterAction = EditorInfo.IME_ACTION_NONE

    override fun onCreate() {
        super.onCreate()
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this)
        lifecycleOwner.attachToView(composeView)
        composeView.setContent {
            MoneyKeyboard(state = keyboardState, onKey = ::handleKey)
        }
        return composeView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentEnterAction = (info?.imeOptions ?: 0) and EditorInfo.IME_MASK_ACTION
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onDestroy() {
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }

    private fun handleKey(key: KeyData) {
        val ic = currentInputConnection ?: return

        when (key.keyType) {
            KeyType.CHARACTER -> {
                val text = if (keyboardState.isShifted) key.capsLabel else key.label
                ic.commitText(text, 1)
                keyboardState.onLetterCommitted()
            }

            KeyType.SHIFT -> {
                val now = SystemClock.elapsedRealtime()
                if (now - lastShiftTapTime < 300) {
                    keyboardState.isCapsLocked = !keyboardState.isCapsLocked
                    keyboardState.isShifted = keyboardState.isCapsLocked
                } else {
                    keyboardState.isCapsLocked = false
                    keyboardState.isShifted = !keyboardState.isShifted
                }
                lastShiftTapTime = now
            }

            KeyType.BACKSPACE -> {
                ic.deleteSurroundingText(1, 0)
            }

            KeyType.SPACE -> {
                val now = SystemClock.elapsedRealtime()
                if (now - lastSpaceTapTime < 300) {
                    // Double-tap space: replace the space just typed with ". "
                    ic.deleteSurroundingText(1, 0)
                    ic.commitText(". ", 1)
                    keyboardState.isShifted = true
                } else {
                    ic.commitText(" ", 1)
                }
                lastSpaceTapTime = now
            }

            KeyType.ENTER -> {
                if (currentEnterAction != EditorInfo.IME_ACTION_NONE &&
                    currentEnterAction != EditorInfo.IME_ACTION_UNSPECIFIED
                ) {
                    ic.performEditorAction(currentEnterAction)
                } else {
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                }
            }

            KeyType.SYMBOLS -> {
                keyboardState.isSymbolsMode = !keyboardState.isSymbolsMode
            }
        }
    }
}
