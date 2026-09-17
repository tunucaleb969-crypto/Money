package com.kwame.money

// Ported from the previous keyboard app (com.kwame.aikeyboard.EmojiSuggester).
object EmojiSuggester {
    val commonEmojis = listOf(
        "\uD83D\uDE00","\uD83D\uDE02","\uD83D\uDE0A","\uD83D\uDE0D","\uD83D\uDE18","\uD83D\uDE09","\uD83D\uDE0E","\uD83E\uDD14","\uD83D\uDE22","\uD83D\uDE2D",
        "\uD83D\uDE21","\uD83D\uDE34","\uD83E\uDD73","\uD83D\uDE31","\uD83D\uDE44","\uD83D\uDE07","\uD83E\uDD17","\uD83D\uDE4F","\uD83D\uDC4D","\uD83D\uDC4E",
        "\uD83D\uDC4F","\uD83D\uDE4C","\uD83D\uDCAA","\u270C\uFE0F","\uD83E\uDD1D","\u2764\uFE0F","\uD83D\uDC94","\uD83D\uDD25","\u2728","\uD83C\uDF89",
        "\uD83D\uDCAF","\uD83D\uDC40","\uD83D\uDE48","\uD83D\uDE05","\uD83E\uDD37","\uD83D\uDC80","\uD83C\uDF82","\u2615","\uD83C\uDF55","\u26BD",
        "\uD83D\uDCF1","\uD83D\uDCBB","\uD83D\uDD50","\uD83D\uDCC5","\u2705","\u274C","\uD83D\uDCAC","\uD83D\uDCE9","\uD83D\uDE34","\uD83E\uDD7A"
    )

    private val wordToEmoji = mapOf(
        "happy" to "\uD83D\uDE0A", "sad" to "\uD83D\uDE22", "love" to "\u2764\uFE0F", "laugh" to "\uD83D\uDE02",
        "funny" to "\uD83D\uDE02", "angry" to "\uD83D\uDE21", "tired" to "\uD83D\uDE34", "excited" to "\uD83E\uDD73",
        "cry" to "\uD83D\uDE2D", "party" to "\uD83C\uDF89", "fire" to "\uD83D\uDD25", "cool" to "\uD83D\uDE0E",
        "thanks" to "\uD83D\uDE4F", "thank" to "\uD83D\uDE4F", "please" to "\uD83D\uDE4F", "sorry" to "\uD83D\uDE14",
        "congrats" to "\uD83C\uDF89", "congratulations" to "\uD83C\uDF89", "birthday" to "\uD83C\uDF82",
        "food" to "\uD83C\uDF55", "coffee" to "\u2615", "yes" to "\u2705", "no" to "\u274C",
        "ok" to "\uD83D\uDC4D", "okay" to "\uD83D\uDC4D", "good" to "\uD83D\uDC4D", "great" to "\uD83D\uDC4F",
        "amazing" to "\u2728", "wow" to "\uD83D\uDE31", "shock" to "\uD83D\uDE31", "shocked" to "\uD83D\uDE31",
        "sleep" to "\uD83D\uDE34", "sleepy" to "\uD83D\uDE34", "strong" to "\uD83D\uDCAA", "win" to "\uD83C\uDFC6",
        "lol" to "\uD83D\uDE02", "haha" to "\uD83D\uDE02", "hello" to "\uD83D\uDC4B", "hi" to "\uD83D\uDC4B",
        "bye" to "\uD83D\uDC4B", "goodbye" to "\uD83D\uDC4B", "phone" to "\uD83D\uDCF1", "call" to "\uD83D\uDCDE",
        "time" to "\uD83D\uDD50", "today" to "\uD83D\uDCC5", "message" to "\uD83D\uDCAC", "hot" to "\uD83D\uDD25"
    )

    fun suggestForWord(word: String): String? = wordToEmoji[word.lowercase()]

    private val emojiKeywords = mapOf(
        "\uD83D\uDE00" to listOf("grin", "happy", "smile"),
        "\uD83D\uDE02" to listOf("laugh", "lol", "funny", "tears"),
        "\uD83D\uDE0A" to listOf("happy", "smile", "blush"),
        "\uD83D\uDE0D" to listOf("love", "heart eyes", "crush"),
        "\uD83D\uDE18" to listOf("kiss", "love"),
        "\uD83D\uDE09" to listOf("wink"),
        "\uD83D\uDE0E" to listOf("cool", "sunglasses"),
        "\uD83E\uDD14" to listOf("think", "hmm", "wonder"),
        "\uD83D\uDE22" to listOf("sad", "cry", "tear"),
        "\uD83D\uDE2D" to listOf("cry", "sob", "sad"),
        "\uD83D\uDE21" to listOf("angry", "mad", "rage"),
        "\uD83D\uDE34" to listOf("sleep", "tired", "zzz"),
        "\uD83E\uDD73" to listOf("party", "celebrate", "excited"),
        "\uD83D\uDE31" to listOf("shock", "scared", "surprised"),
        "\uD83D\uDE44" to listOf("eyeroll", "annoyed"),
        "\uD83D\uDE07" to listOf("angel", "innocent"),
        "\uD83E\uDD17" to listOf("hug"),
        "\uD83D\uDE4F" to listOf("thanks", "please", "pray"),
        "\uD83D\uDC4D" to listOf("thumbs up", "yes", "ok", "good"),
        "\uD83D\uDC4E" to listOf("thumbs down", "no", "bad"),
        "\uD83D\uDC4F" to listOf("clap", "great", "applause"),
        "\uD83D\uDE4C" to listOf("celebrate", "praise", "hands up"),
        "\uD83D\uDCAA" to listOf("strong", "muscle", "flex"),
        "\u270C\uFE0F" to listOf("peace", "victory"),
        "\uD83E\uDD1D" to listOf("handshake", "deal", "agree"),
        "\u2764\uFE0F" to listOf("love", "heart"),
        "\uD83D\uDC94" to listOf("heartbreak", "sad", "breakup"),
        "\uD83D\uDD25" to listOf("fire", "hot", "lit"),
        "\u2728" to listOf("sparkle", "amazing", "shine"),
        "\uD83C\uDF89" to listOf("party", "celebrate", "congrats"),
        "\uD83D\uDCAF" to listOf("hundred", "perfect"),
        "\uD83D\uDC40" to listOf("eyes", "look", "watching"),
        "\uD83D\uDE48" to listOf("shy", "embarrassed", "monkey"),
        "\uD83D\uDE05" to listOf("sweat", "nervous laugh"),
        "\uD83E\uDD37" to listOf("shrug", "idk"),
        "\uD83D\uDC80" to listOf("dead", "skull", "dying laughing"),
        "\uD83C\uDF82" to listOf("birthday", "cake"),
        "\u2615" to listOf("coffee", "tea"),
        "\uD83C\uDF55" to listOf("pizza", "food"),
        "\u26BD" to listOf("soccer", "football", "sport"),
        "\uD83D\uDCF1" to listOf("phone", "mobile"),
        "\uD83D\uDCBB" to listOf("laptop", "computer"),
        "\uD83D\uDD50" to listOf("clock", "time"),
        "\uD83D\uDCC5" to listOf("calendar", "date"),
        "\u2705" to listOf("check", "done", "yes"),
        "\u274C" to listOf("cross", "no", "wrong"),
        "\uD83D\uDCAC" to listOf("chat", "message", "speech"),
        "\uD83D\uDCE9" to listOf("mail", "email", "message"),
        "\uD83E\uDD7A" to listOf("pleading", "puppy eyes", "please")
    )

    fun searchEmojis(query: String): List<String> {
        if (query.isBlank()) return emptyList()
        val q = query.lowercase()
        return commonEmojis.filter { emoji ->
            emojiKeywords[emoji]?.any { it.contains(q) } == true
        }
    }
}
