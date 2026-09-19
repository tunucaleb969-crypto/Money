package com.kwame.money

import android.inputmethodservice.InputMethodService
import android.os.SystemClock
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

private enum class KeyboardPanel { KEYBOARD, EMOJI, AI }

class MoneyKeyboardService : InputMethodService() {

    private val lifecycleOwner = KeyboardLifecycleOwner()
    private val keyboardState = KeyboardState()
    private val undoRedoManager = UndoRedoManager()
    private lateinit var clipboardHistoryManager: ClipboardHistoryManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var aiPanelState by mutableStateOf<AiPanelState>(AiPanelState.Idle)
    private var lastAiActionType: AiActionType = AiActionType.FIX_GRAMMAR
    private var lastAiSourceText: String = ""

    private var suggestions by mutableStateOf(listOf<String>())
    private var recentEmojis by mutableStateOf(listOf<String>())

    private var lastSpaceTapTime = 0L
    private var lastShiftTapTime = 0L
    private var currentEnterAction = EditorInfo.IME_ACTION_NONE

    private var spaceDragAccumulatorPx = 0f
    private val dragStepThresholdPx = 40f

    override fun onCreate() {
        super.onCreate()
        clipboardHistoryManager = ClipboardHistoryManager(applicationContext)
        clipboardHistoryManager.startListening()
        recentEmojis = Prefs.getRecentEmojis(applicationContext)
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

                val panel = when {
                    keyboardState.isEmojiPanelOpen -> KeyboardPanel.EMOJI
                    keyboardState.isAiPanelOpen -> KeyboardPanel.AI
                    else -> KeyboardPanel.KEYBOARD
                }

                AnimatedContent(
                    targetState = panel,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "keyboardPanelSwitch"
                ) { targetPanel ->
                    when (targetPanel) {
                        KeyboardPanel.EMOJI -> {
                            EmojiPanel(
                                recentEmojis = recentEmojis,
                                onEmojiTap = ::onEmojiTap,
                                onClose = { keyboardState.isEmojiPanelOpen = false }
                            )
                        }
                        KeyboardPanel.AI -> {
                            AiPanel(
                                state = aiPanelState,
                                onAction = ::onAiAction,
                                onInsert = ::onAiInsert,
                                onRegenerate = ::onAiRegenerate,
                                onDismiss = ::onAiDismiss
                            )
                        }
                        KeyboardPanel.KEYBOARD -> {
                            Column {
                                SuggestionBar(
                                    suggestions = suggestions,
                                    onSuggestionTap = ::onSuggestionTap
                                )
                                MoneyKeyboard(
                                    state = keyboardState,
                                    onKey = ::handleKey,
                                    onSpaceDrag = ::onSpaceDrag,
                                    onSpaceDragEnd = ::onSpaceDragEnd
                                )
                            }
                        }
                    }
                }
            }
        }
        return composeView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentEnterAction = (info?.imeOptions ?: 0) and EditorInfo.IME_MASK_ACTION
        keyboardState.isSensitiveField = computeIsSensitiveField(info)
        keyboardState.isEmojiPanelOpen = false
        keyboardState.isAiPanelOpen = false
        aiPanelState = AiPanelState.Idle
        updateSuggestions()
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onDestroy() {
        clipboardHistoryManager.stopListening()
        serviceScope.cancel()
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
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
                return // no text changed, skip suggestion refresh
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
                return // no text changed, skip suggestion refresh
            }

            KeyType.EMOJI_TOGGLE -> {
                keyboardState.isEmojiPanelOpen = !keyboardState.isEmojiPanelOpen
                return // no text changed, skip suggestion refresh
            }

            KeyType.AI_TOGGLE -> {
                if (keyboardState.isSensitiveField) return // AI disabled entirely in secure fields
                keyboardState.isAiPanelOpen = !keyboardState.isAiPanelOpen
                if (keyboardState.isAiPanelOpen) aiPanelState = AiPanelState.Idle
                return // no text changed, skip suggestion refresh
            }
        }

        updateSuggestions()
    }

    // ---- AI actions ----

    private fun onAiAction(type: AiActionType) {
        if (keyboardState.isSensitiveField) return // defense in depth
        val ic = currentInputConnection ?: return
        val fullText = getFullText(ic)
        if (fullText.isBlank()) {
            aiPanelState = AiPanelState.Failed("There's no text to work with yet.")
            return
        }
        lastAiActionType = type
        lastAiSourceText = fullText
        runAiAction(type, fullText, forceFresh = false)
    }

    private fun onAiRegenerate() {
        if (lastAiSourceText.isNotBlank()) {
            runAiAction(lastAiActionType, lastAiSourceText, forceFresh = true)
        }
    }

    private fun runAiAction(type: AiActionType, sourceText: String, forceFresh: Boolean) {
        if (!forceFresh) {
            val cached = AiResponseCache.get(type, sourceText)
            if (cached != null) {
                aiPanelState = AiPanelState.Ready(cached)
                return
            }
        }

        if (!NetworkUtils.isOnline(applicationContext)) {
            aiPanelState = AiPanelState.Failed(
                "No internet connection. AI actions need Gemini online \u2014 typing, suggestions, and emoji still work fine offline."
            )
            return
        }

        aiPanelState = AiPanelState.Loading
        val targetLanguage = if (type == AiActionType.TRANSLATE) "Twi" else null
        val (systemPrompt, wrappedText) = AiActions.buildPrompt(type, sourceText, targetLanguage)
        val provider: AiProvider = GeminiProvider(Prefs.getApiKey(applicationContext))
        serviceScope.launch {
            val result = provider.generate(systemPrompt, wrappedText)
            result.onSuccess { AiResponseCache.put(type, sourceText, it) }
            aiPanelState = result.fold(
                onSuccess = { AiPanelState.Ready(it) },
                onFailure = { AiPanelState.Failed(it.message ?: "Unknown error") }
            )
        }
    }

    private fun onAiInsert(resultText: String) {
        val ic = currentInputConnection ?: return
        val oldText = lastAiSourceText
        // Replace the whole field's text with the AI result, bypassing the
        // normal per-key path so autocorrect can't immediately mangle it.
        // Reply-suggestion is different: it's a suggested response, not an edit
        // of the existing text, so it's appended instead of replacing anything.
        if (lastAiActionType == AiActionType.REPLY_SUGGESTION) {
            val pos = cursorPosition(ic)
            ic.commitText(resultText, 1)
            undoRedoManager.recordInsert(pos, resultText)
        } else {
            ic.setSelection(0, oldText.length)
            ic.commitText(resultText, 1)
            undoRedoManager.recordReplace(0, oldText, resultText)
        }

        keyboardState.isAiPanelOpen = false
        aiPanelState = AiPanelState.Idle
        updateSuggestions()
    }

    private fun onAiDismiss() {
        aiPanelState = AiPanelState.Idle
        keyboardState.isAiPanelOpen = false
    }

    private fun getFullText(ic: InputConnection): String {
        val request = ExtractedTextRequest().apply { hintMaxChars = 10000 }
        return ic.getExtractedText(request, 0)?.text?.toString() ?: ""
    }

    // ---- Emoji / suggestions ----

    private fun onEmojiTap(emoji: String) {
        val ic = currentInputConnection ?: return
        val pos = cursorPosition(ic)
        ic.commitText(emoji, 1)
        undoRedoManager.recordInsert(pos, emoji)
        Prefs.addRecentEmoji(applicationContext, emoji)
        recentEmojis = Prefs.getRecentEmojis(applicationContext)
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
