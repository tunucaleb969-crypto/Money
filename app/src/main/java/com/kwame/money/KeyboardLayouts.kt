package com.kwame.money

object KeyboardLayouts {

    val qwertyRows: List<List<KeyData>> = listOf(
        "qwertyuiop".map { KeyData(it.toString()) },
        "asdfghjkl".map { KeyData(it.toString()) },
        listOf(KeyData("shift", keyType = KeyType.SHIFT)) +
            "zxcvbnm".map { KeyData(it.toString()) } +
            listOf(KeyData("back", keyType = KeyType.BACKSPACE))
    )

    val symbolRows: List<List<KeyData>> = listOf(
        "1234567890".map { KeyData(it.toString()) },
        "@#\$_&-+()".map { KeyData(it.toString()) },
        "*\"':;!?".map { KeyData(it.toString()) } +
            listOf(KeyData("back", keyType = KeyType.BACKSPACE))
    )
}
