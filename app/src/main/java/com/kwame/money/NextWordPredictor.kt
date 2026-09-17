package com.kwame.money

// Ported from the previous keyboard app (com.kwame.aikeyboard.NextWordPredictor).
object NextWordPredictor {
    // Maps a word to its most likely next words, based on common English phrase patterns.
    private val predictions = mapOf(
        "how" to listOf("are", "do", "much"),
        "thank" to listOf("you"),
        "i" to listOf("am", "will", "think"),
        "you" to listOf("are", "can", "want"),
        "is" to listOf("it", "there", "this"),
        "are" to listOf("you", "we", "there"),
        "do" to listOf("you", "not", "we"),
        "can" to listOf("you", "we", "i"),
        "will" to listOf("be", "you", "it"),
        "let" to listOf("me", "us", "know"),
        "i'm" to listOf("not", "going", "sorry"),
        "what" to listOf("is", "do", "are"),
        "good" to listOf("morning", "night", "luck"),
        "see" to listOf("you", "if", "what"),
        "looking" to listOf("forward", "for"),
        "talk" to listOf("to", "soon", "about"),
        "on" to listOf("the", "my", "it"),
        "in" to listOf("the", "a", "case"),
        "at" to listOf("the", "least", "all"),
        "for" to listOf("the", "you", "me"),
        "have" to listOf("a", "you", "to"),
        "want" to listOf("to", "you", "me"),
        "need" to listOf("to", "you", "help"),
        "going" to listOf("to", "on"),
        "just" to listOf("a", "wanted", "to"),
        "please" to listOf("let", "note", "send"),
        "sorry" to listOf("for", "about", "i"),
        "hope" to listOf("you", "so", "this"),
        "looking forward" to listOf("to"),
        "not" to listOf("sure", "yet", "really"),
        "of" to listOf("the", "course", "my"),
        "we" to listOf("can", "will", "should"),
        "it" to listOf("is", "was", "will"),
        "the" to listOf("same", "best", "first")
    )

    fun predict(previousWord: String): List<String> {
        if (previousWord.isBlank()) return emptyList()
        return predictions[previousWord.lowercase()] ?: emptyList()
    }
}
