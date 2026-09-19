package com.kwame.money

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs

/**
 * Plain-View keyboard — deliberately NOT Compose. Compose inside
 * InputMethodService required manual Lifecycle/ViewModelStore/SavedState
 * owner plumbing that kept causing crashes we had no way to get a stack
 * trace for on-device. Plain Views need none of that, which removes the
 * whole failure class. Rebuilt from scratch to mirror the old working
 * AI Keyboard app's approach.
 */
class MoneyKeyboardView(context: Context) : LinearLayout(context) {

    var onKey: ((KeyData) -> Unit)? = null
    var onSpaceDrag: ((Float) -> Unit)? = null
    var onSpaceDragEnd: (() -> Unit)? = null

    init {
        orientation = VERTICAL
        setBackgroundColor(Color.parseColor("#1E1E2E"))
        setPadding(8, 8, 8, 8)
    }

    private data class KeySpec(val key: KeyData, val weight: Float)

    /** Call after any KeyboardState change to redraw the keyboard. */
    fun render(state: KeyboardState) {
        removeAllViews()
        val rows = if (state.isSymbolsMode) KeyboardLayouts.symbolRows else KeyboardLayouts.qwertyRows

        rows.forEach { row ->
            addView(buildRow(row.map { KeySpec(it, 1f) }, state))
        }

        val bottomRow = listOf(
            KeySpec(KeyData(if (state.isSymbolsMode) "ABC" else "123", keyType = KeyType.SYMBOLS), 1f),
            KeySpec(KeyData("\uD83D\uDE0A", keyType = KeyType.EMOJI_TOGGLE), 1f),
            KeySpec(KeyData("AI", keyType = KeyType.AI_TOGGLE), 1f),
            KeySpec(KeyData(" ", keyType = KeyType.SPACE), 2f),
            KeySpec(KeyData("enter", keyType = KeyType.ENTER), 1f)
        )
        addView(buildRow(bottomRow, state))
    }

    private fun buildRow(keys: List<KeySpec>, state: KeyboardState): LinearLayout {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 4, 0, 4)
            }
        }
        keys.forEach { spec -> row.addView(buildKeyButton(spec.key, spec.weight, state)) }
        return row
    }

    private fun buildKeyButton(key: KeyData, weight: Float, state: KeyboardState): TextView {
        val displayLabel = when (key.keyType) {
            KeyType.CHARACTER -> if (state.isShifted) key.capsLabel else key.label
            KeyType.SHIFT -> if (state.isCapsLocked) "CAPS" else "\u21e7"
            KeyType.BACKSPACE -> "\u232b"
            KeyType.SPACE -> ""
            KeyType.ENTER -> "\u23ce"
            KeyType.SYMBOLS -> key.label
            KeyType.EMOJI_TOGGLE -> key.label
            KeyType.AI_TOGGLE -> key.label
        }

        // TalkBack needs real words, not raw glyphs like "\u232b" or "\u21e7".
        val accessibilityLabel = when (key.keyType) {
            KeyType.CHARACTER -> if (state.isShifted) key.capsLabel else key.label
            KeyType.SHIFT -> if (state.isCapsLocked) "Caps lock on" else "Shift"
            KeyType.BACKSPACE -> "Backspace"
            KeyType.SPACE -> "Space"
            KeyType.ENTER -> "Enter"
            KeyType.SYMBOLS -> if (state.isSymbolsMode) "Switch to letters" else "Switch to numbers and symbols"
            KeyType.EMOJI_TOGGLE -> "Emoji panel (coming back soon)"
            KeyType.AI_TOGGLE -> "AI assistant (coming back soon)"
        }

        return TextView(context).apply {
            text = displayLabel
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            contentDescription = accessibilityLabel
            setBackgroundColor(Color.parseColor("#2E2E3E"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight).apply {
                setMargins(3, 3, 3, 3)
            }
            setPadding(0, 44, 0, 44)

            if (key.keyType == KeyType.SPACE) {
                var lastX = 0f
                var totalDrag = 0f
                setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            lastX = event.rawX
                            totalDrag = 0f
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val delta = event.rawX - lastX
                            lastX = event.rawX
                            totalDrag += abs(delta)
                            onSpaceDrag?.invoke(delta)
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            onSpaceDragEnd?.invoke()
                            // Small total movement = it was a tap, not a drag: insert a space.
                            if (totalDrag < 24f) onKey?.invoke(key)
                            true
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            onSpaceDragEnd?.invoke()
                            true
                        }
                        else -> false
                    }
                }
            } else {
                setOnClickListener { onKey?.invoke(key) }
            }
        }
    }
}
