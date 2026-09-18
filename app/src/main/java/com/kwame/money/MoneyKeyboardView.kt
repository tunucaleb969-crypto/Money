package com.kwame.money

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MoneyKeyboard(
    state: KeyboardState,
    onKey: (KeyData) -> Unit,
    onSpaceDrag: (Float) -> Unit = {},
    onSpaceDragEnd: () -> Unit = {}
) {
    val rows = if (state.isSymbolsMode) KeyboardLayouts.symbolRows else KeyboardLayouts.qwertyRows

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val rowSpacing = if (isLandscape) 1.dp else 2.dp
    val keyVerticalPadding = if (isLandscape) 8.dp else 14.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E2E))
            .padding(4.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = rowSpacing),
                horizontalArrangement = Arrangement.Center
            ) {
                row.forEach { key ->
                    KeyButton(
                        key = key,
                        state = state,
                        onKey = onKey,
                        onSpaceDrag = onSpaceDrag,
                        onSpaceDragEnd = onSpaceDragEnd,
                        verticalPadding = keyVerticalPadding
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = rowSpacing),
            horizontalArrangement = Arrangement.Center
        ) {
            KeyButton(
                key = KeyData(if (state.isSymbolsMode) "ABC" else "123", keyType = KeyType.SYMBOLS),
                state = state,
                onKey = onKey,
                verticalPadding = keyVerticalPadding
            )
            KeyButton(
                key = KeyData("\uD83D\uDE0A", keyType = KeyType.EMOJI_TOGGLE),
                state = state,
                onKey = onKey,
                verticalPadding = keyVerticalPadding
            )
            KeyButton(
                key = KeyData("AI", keyType = KeyType.AI_TOGGLE),
                state = state,
                onKey = onKey,
                verticalPadding = keyVerticalPadding
            )
            KeyButton(
                key = KeyData(" ", keyType = KeyType.SPACE),
                state = state,
                onKey = onKey,
                onSpaceDrag = onSpaceDrag,
                onSpaceDragEnd = onSpaceDragEnd,
                weight = 2f,
                verticalPadding = keyVerticalPadding
            )
            KeyButton(
                key = KeyData("enter", keyType = KeyType.ENTER),
                state = state,
                onKey = onKey,
                verticalPadding = keyVerticalPadding
            )
        }
    }
}

@Composable
private fun RowScope.KeyButton(
    key: KeyData,
    state: KeyboardState,
    onKey: (KeyData) -> Unit,
    onSpaceDrag: (Float) -> Unit = {},
    onSpaceDragEnd: () -> Unit = {},
    weight: Float = 1f,
    verticalPadding: Dp = 14.dp
) {
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
        KeyType.EMOJI_TOGGLE -> "Emoji panel"
        KeyType.AI_TOGGLE -> "AI assistant"
    }

    val dragModifier = if (key.keyType == KeyType.SPACE) {
        Modifier.pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragEnd = { onSpaceDragEnd() },
                onDragCancel = { onSpaceDragEnd() }
            ) { change, dragAmount ->
                change.consume()
                onSpaceDrag(dragAmount)
            }
        }
    } else Modifier

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(if (isPressed) 0.9f else 1f, label = "keyPressScale")

    Box(
        modifier = Modifier
            .weight(weight)
            .padding(2.dp)
            .scale(pressScale)
            .background(Color(0xFF2E2E3E))
            .then(dragModifier)
            .clickable(interactionSource = interactionSource, indication = null) { onKey(key) }
            .semantics { contentDescription = accessibilityLabel }
            .padding(vertical = verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(text = displayLabel, color = Color.White)
    }
}
