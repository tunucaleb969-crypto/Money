package com.kwame.money

import android.inputmethodservice.InputMethodService
import android.os.SystemClock
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle

class MoneyKeyboardService : InputMethodService() {

    private val lifecycleOwner = KeyboardLifecycleOwner()
    private val keyboardState = KeyboardState()
    private val undoRedoManager = UndoRedoManager()
    private lateinit var clipboardHistoryManager: ClipboardHistoryManager

    private var lastSpaceTapTime = 0L
    private var lastShiftTapTime = 0L
    private var currentEnterAction = EditorInfo.IME_ACTION_NONE

    private var spaceDragAccumulatorPx = 0f
    private val dragStepThresholdPx = 40f

    override fun onCreate() {
        super.onCreate()
        clipboardHistoryManager = ClipboardHistoryManager(applicationContext)
        clipboardHistoryManager.startListening()
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onCreateInputView(): View {
        val composeView = ComposeView(this)
        lifecycleOwner.attachToView(composeView)
        composeView.setContent {
            Column {
                EditToolbar(
                    onUndo = { currentInputConnection?.let { undoRedoManager.undo(it) } },
                    onRedo = { currentInputConnection?.let { undoRedoManager.redo(it) } },
                    onSelectAll = { currentInputConnection?.performContextMenuAction(android.R.id.selectAll) },
                    onCopy = { currentInputConnection?.performContextMenuAction(android.R.id.copy) },
                    onCut = { currentInputConnection?.performContextMenuAction(android.R.id.cut) },
                    onPaste = { currentInputConnection?.performContextMenuAction(android.R.id.paste) }
                )
                MoneyKeyboard(
                    state = keyboardState,
                    onKey = ::handleKey,
                    onSpaceDrag = ::onSpaceDrag,
                    onSpaceDragEnd = ::onSpaceDragEnd
                )
            }
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
        clipboardHistoryManager.stopListening()
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }

    private fun handleKey(key: KeyData) {
        val ic = currentInputConnection ?: return

        when (key.keyType) {
            KeyType.CHARACTER -> {
                val text = if (keyboardState.isShifted) key.capsLabel else key.label
                val pos = cursorPosition(ic)
                ic.commitText(text, 1)
                undoRedoManager.recordInsert(pos, text)
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
                val deleted = ic.getTextBeforeCursor(1, 0)?.toString()
                if (!deleted.isNullOrEmpty()) {
                    val pos = cursorPosition(ic) - deleted.length
                    ic.deleteSurroundingText(1, 0)
                    undoRedoManager.recordDelete(pos, deleted)
                }
            }

            KeyType.SPACE -> {
                val now = SystemClock.elapsedRealtime()
                if (now - lastSpaceTapTime < 300) {
                    // Double-tap space: replace the space just typed with ". "
                    val pos = cursorPosition(ic) - 1
                    ic.deleteSurroundingText(1, 0)
                    ic.commitText(". ", 1)
                    undoRedoManager.recordReplace(pos, " ", ". ")
                    keyboardState.isShifted = true
                } else {
                    val pos = cursorPosition(ic)
                    ic.commitText(" ", 1)
                    undoRedoManager.recordInsert(pos, " ")
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

    private fun onSpaceDrag(deltaPx: Float) {
        spaceDragAccumulatorPx += deltaPx
        while (spaceDragAccumulatorPx >= dragStepThresholdPx) {
            moveCursor(1)
            spaceDragAccumulatorPx -= dragStepThresholdPx
        }
        while (spaceDragAccumulatorPx <= -dragStepThresholdPx) {
            moveCursor(-1)
            spaceDragAccumulatorPx += dragStepThresholdPx
        }
    }

    private fun onSpaceDragEnd() {
        spaceDragAccumulatorPx = 0f
    }

    private fun moveCursor(direction: Int) {
        val ic = currentInputConnection ?: return
        val pos = cursorPosition(ic)
        val newPos = (pos + direction).coerceAtLeast(0)
        ic.setSelection(newPos, newPos)
    }

    private fun cursorPosition(ic: InputConnection): Int {
        val extractedText = ic.getExtractedText(ExtractedTextRequest(), 0) ?: return 0
        return extractedText.selectionStart
    }
}
