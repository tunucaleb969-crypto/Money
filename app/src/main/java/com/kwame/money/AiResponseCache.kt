package com.kwame.money

/**
 * Bounded cache so identical AI requests (same action + same source text)
 * don't re-hit the Gemini API. Keyed on action type + exact text, so it only
 * ever serves a response that was genuinely generated for that exact input.
 */
object AiResponseCache {
    private const val MAX_ENTRIES = 30

    private val cache = object : LinkedHashMap<String, String>(MAX_ENTRIES, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
            return size > MAX_ENTRIES
        }
    }

    private fun key(type: AiActionType, sourceText: String) = "${type.name}::$sourceText"

    @Synchronized
    fun get(type: AiActionType, sourceText: String): String? = cache[key(type, sourceText)]

    @Synchronized
    fun put(type: AiActionType, sourceText: String, result: String) {
        cache[key(type, sourceText)] = result
    }
}
