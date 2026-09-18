package com.kwame.money

data class KeyData(
    val label: String,
    val capsLabel: String = label.uppercase(),
    val keyType: KeyType = KeyType.CHARACTER
)

enum class KeyType {
    CHARACTER,
    SHIFT,
    BACKSPACE,
    SPACE,
    ENTER,
    SYMBOLS,
    EMOJI_TOGGLE
}
