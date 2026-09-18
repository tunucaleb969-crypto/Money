package com.kwame.money

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun MoneyKeyboard(
    state: KeyboardState,
    onKey: (KeyData) -> Unit,
    onSpaceDrag: (Float) -> Unit = {},
    onSpaceDragEnd: () -> Unit = {}
) {
    val rows = if (state.isSymbolsMode) KeyboardLayouts.symbolRows else KeyboardLayouts.qwertyRows

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E2E))
            .padding(4.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                row.forEach { key ->
                    KeyButton(key = key, state = state, onKey = onKey, onSpaceDrag = onSpaceDrag, onSpaceDragEnd = onSpaceDragEnd)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            KeyButton(
                key = KeyData(if (state.isSymbolsMode) "ABC" else "123", keyType = KeyType.SYMBOLS),
                state = state,
                onKey = onKey
            )
            KeyButton(
                key = KeyData("\uD83D\uDE0A", keyType = KeyType.EMOJI_TOGGLE),
                state = state,
                onKey = onKey
            )
            KeyButton(
                key = KeyData(" ", keyType = KeyType.SPACE),
                state = state,
                onKey = onKey,
                onSpaceDrag = onSpaceDrag,
                onSpaceDragEnd = onSpaceDragEnd,
                weight = 3f
            )
            KeyButton(
                key = KeyData("enter", keyType = KeyType.ENTER),
                state = state,
                onKey = onKey
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
    weight: Float = 1f
) {
    val displayLabel = when (key.keyType) {
        KeyType.CHARACTER -> if (state.isShifted) key.capsLabel else key.label
        KeyType.SHIFT -> if (state.isCapsLocked) "CAPS" else "\u21e7"
        KeyType.BACKSPACE -> "\u232b"
        KeyType.SPACE -> ""
        KeyType.ENTER -> "\u23ce"
        KeyType.SYMBOLS -> key.label
        KeyType.EMOJI_TOGGLE -> key.label
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

    Box(
        modifier = Modifier
            .weight(weight)
            .padding(2.dp)
            .background(Color(0xFF2E2E3E))
            .then(dragModifier)
            .clickable { onKey(key) }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = displayLabel, color = Color.White)
    }
}
