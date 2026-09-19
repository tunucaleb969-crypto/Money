package com.kwame.money

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.os.SystemClock
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Rebuilt without Compose. Compose inside InputMethodService needed manual
 * Lifecycle/ViewModelStore/SavedState owner plumbing (KeyboardLifecycleOwner)
 * that kept causing crashes with no way to get a stack trace on-device.
 * Plain Android Views need none of that — same approach the old AI Keyboard
 * app used successfully. AI panel and emoji panel are temporarily disabled
 * (their toggle keys are no-ops) while we confirm this baseline is solid;
 * they'll come back in plain-View form once typing + suggestions are
 * confirmed working.
 */
class MoneyKeyboardService : InputMethodService() {

    private val keyboardState = KeyboardState()
    private val undoRedoManager = UndoRedoManager()
    private lateinit var clipboardHistoryManager: ClipboardHistoryManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var suggestionBarView: SuggestionBarView
    private lateinit var keyboardView: MoneyKeyboardView

    private var suggestions: List<String> = emptyList()

    private var lastSpaceTapTime = 0L
    private var lastShiftTapTime = 0L
    private var currentEnterAction = EditorInfo.IME_ACTION_NONE

    private var spaceDragAccumulatorPx = 0f
    private val dragStepThresholdPx = 40f

    override fun onCreate() {
        super.onCreate()
        clipboardHistoryManager = ClipboardHistoryManager(applicationContext)
        clipboardHistoryManager.startListening()
    }

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        root.addView(buildToolbarRow())

        suggestionBarView = SuggestionBarView(this).apply {
            onSuggestionTap = ::onSuggestionTap
        }
        root.addView(suggestionBarView)

        keyboardView = MoneyKeyboardView(this).apply {
            onKey = ::handleKey
            onSpaceDrag = ::onSpaceDrag
            onSpaceDragEnd = ::onSpaceDragEnd
        }
        root.addView(keyboardView)

        refreshUi()
        return root
    }

    private fun buildToolbarRow(): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#12121A"))
        }
        val actions = listOf(
            "Undo" to { currentInputConnection?.let { undoRedoManager.undo(it) } },
            "Redo" to { currentInputConnection?.let { undoRedoManager.redo(it) } },
            "Select All" to { currentInputConnection?.performContextMenuAction(android.R.id.selectAll); Unit },
            "Copy" to { currentInputConnection?.performContextMenuAction(android.R.id.copy); Unit },
            "Cut" to { currentInputConnection?.performContextMenuAction(android.R.id.cut); Unit },
            "Paste" to { currentInputConnection?.performContextMenuAction(android.R.id.paste); Unit }
        )
        actions.forEach { (label, action) ->
            row.addView(TextView(this).apply {
                text = label
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                setPadding(12, 16, 12, 16)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener { action() }
            })
        }
        return row
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentEnterAction = (info?.imeOptions ?: 0) and EditorInfo.IME_MASK_ACTION
        keyboardState.isSensitiveField = computeIsSensitiveField(info)
        keyboardState.isEmojiPanelOpen = false
        keyboardState.isAiPanelOpen = false
        updateSuggestions()
        refreshUi()
    }

    override fun onDestroy() {
        clipboardHistoryManager.stopListening()
        serviceScope.cancel()
        super.onDestroy()
    }

    /**
     * True for password/PIN fields, or any field that explicitly asks not to
     * be personalized/learned from. When true, AI actions and word
     * suggestions/learning are fully disabled for that field — not just
     * hidden, actually never invoked — matching the privacy plan agreed on
     * when we reviewed the master spec.
     */
    private fun computeIsSensitiveField(info: EditorInfo?): Boolean {
        val inputType = info?.inputType ?: 0
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        val cls = inputType and InputType.TYPE_MASK_CLASS
        val isPasswordVariation = variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
            (cls == InputType.TYPE_CLASS_NUMBER && variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD)
        val noLearningFlag =
            ((info?.imeOptions ?: 0) and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0
        return isPasswordVariation || noLearningFlag
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
                refreshUi()
                return
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
                refreshUi()
                return
            }

            KeyType.EMOJI_TOGGLE -> {
                // Temporarily disabled while we confirm the plain-View rebuild is stable.
                return
            }

            KeyType.AI_TOGGLE -> {
                // Temporarily disabled while we confirm the plain-View rebuild is stable.
                return
            }
        }

        updateSuggestions()
        refreshUi()
    }

    private fun onSuggestionTap(word: String) {
        val ic = currentInputConnection ?: return
        val currentWord = getCurrentWord(ic)

        if (currentWord.isNotBlank()) {
            // Completing a word: replace the typed prefix with the full word.
            val pos = cursorPosition(ic) - currentWord.length
            ic.deleteSurroundingText(currentWord.length, 0)
            ic.commitText("$word ", 1)
            undoRedoManager.recordReplace(pos, currentWord, "$word ")
        } else {
            // Next-word prediction or emoji suggestion: just insert it.
            val pos = cursorPosition(ic)
            ic.commitText("$word ", 1)
            undoRedoManager.recordInsert(pos, "$word ")
        }
        keyboardState.isShifted = false
        updateSuggestions()
        refreshUi()
    }

    private fun updateSuggestions() {
        if (keyboardState.isSensitiveField) {
            suggestions = emptyList()
            return
        }
        val ic = currentInputConnection ?: return
        val currentWord = getCurrentWord(ic)
        suggestions = if (currentWord.isNotBlank()) {
            // Personal dictionary words take priority over the built-in list.
            val personal = Prefs.getDictionaryWords(applicationContext).filter {
                it.startsWith(currentWord, ignoreCase = true) && !it.equals(currentWord, ignoreCase = true)
            }
            (personal + WordSuggester.suggest(currentWord)).distinct().take(3)
        } else {
            val lastWord = getLastCompletedWord(ic)
            val nextWords = NextWordPredictor.predict(lastWord)
            val emoji = EmojiSuggester.suggestForWord(lastWord)
            if (emoji != null) (nextWords + emoji).distinct().take(3) else nextWords
        }
    }

    /** The word currently being typed (no trailing space yet), or "" if none. */
    private fun getCurrentWord(ic: InputConnection): String {
        val before = ic.getTextBeforeCursor(20, 0)?.toString() ?: ""
        return Regex("[\\p{L}']+$").find(before)?.value ?: ""
    }

    /** The most recently finished word (before the trailing space/punctuation). */
    private fun getLastCompletedWord(ic: InputConnection): String {
        val before = ic.getTextBeforeCursor(30, 0)?.toString()?.trimEnd() ?: ""
        return Regex("[\\p{L}']+$").find(before)?.value ?: ""
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
        updateSuggestions()
        refreshUi()
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

    /** Call after any state change to redraw. No Compose recomposition anymore, so this is manual. */
    private fun refreshUi() {
        keyboardView.render(keyboardState)
        suggestionBarView.render(if (keyboardState.isSensitiveField) emptyList() else suggestions)
    }
}
