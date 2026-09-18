package com.kwame.money

/**
 * Abstraction over any AI text provider. The keyboard's AI features talk only
 * to this interface — never directly to Gemini's SDK/API — so a different
 * provider can be swapped in later without touching any UI or action code.
 */
interface AiProvider {
    suspend fun generate(systemPrompt: String, userText: String): Result<String>
}
