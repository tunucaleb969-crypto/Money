package com.kwame.money

import android.content.Context

// Ported from the previous keyboard app (com.kwame.aikeyboard.Prefs) —
// same settings storage layer, package renamed. Not every getter/setter here
// has a matching UI yet (that comes in the Settings phase) — but the storage
// is ready for all of it now, matching what already worked before.
object Prefs {
    private const val FILE = "money_keyboard_prefs"
    private const val KEY_API = "api_key"
    private const val KEY_SOUND = "key_sound"
    private const val KEY_VIBRATE = "key_vibrate"
    private const val KEY_AUTOCAP = "auto_capitalize"
    private const val KEY_HEIGHT = "keyboard_height"
    private const val KEY_CLIPS = "clip_history"
    private const val CLIP_DIVIDER = "\u0001"
    private const val MAX_CLIPS = 8
    private const val KEY_WORD_SUGGEST = "word_suggestions_enabled"
    private const val KEY_AUTOCORRECT = "auto_correct_enabled"
    private const val KEY_UNDO_AUTOCORRECT = "undo_autocorrect_enabled"
    private const val KEY_DOUBLE_SPACE_PERIOD = "double_space_period_enabled"
    private const val KEY_AUTO_SPACE_PUNCT = "auto_space_punct_enabled"
    private const val KEY_REMEMBER_CAPS = "remember_caps_enabled"
    private const val KEY_CLIPBOARD_SUGGEST = "clipboard_suggestions_enabled"
    private const val KEY_VIBRATE_DURATION = "vibrate_duration_ms"
    private const val KEY_SOUND_VOLUME = "sound_volume"
    private const val KEY_LONGPRESS_SOUND = "longpress_sound_enabled"
    private const val KEY_REPEATED_VIBRATE = "repeated_vibrate_enabled"
    private const val KEY_THEME = "keyboard_theme"

    private const val KEY_ONE_HANDED = "one_handed_enabled"
    private const val KEY_ONE_HANDED_SIDE = "one_handed_side"
    private const val KEY_ONE_HANDED_WIDTH = "one_handed_width_percent"
    private const val KEY_SMARTBAR_ENABLED = "smartbar_enabled"
    private const val KEY_SMARTBAR_TOP = "smartbar_on_top"
    private const val KEY_LONGPRESS_DELAY = "longpress_delay_ms"
    private const val KEY_HINTED_NUMBERS = "hinted_numbers_enabled"

    private const val KEY_DICTIONARY_WORDS = "dictionary_words"
    private const val DICT_DIVIDER = "\u0002"

    private const val KEY_LAST_CAPS_LOCK = "last_caps_lock_state"

    fun getApiKey(context: Context): String =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY_API, "") ?: ""

    fun setApiKey(context: Context, value: String) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putString(KEY_API, value).apply()
    }

    fun getSoundEnabled(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_SOUND, true)

    fun setSoundEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_SOUND, value).apply()
    }

    fun getVibrateEnabled(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_VIBRATE, true)

    fun setVibrateEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_VIBRATE, value).apply()
    }

    fun getAutoCapitalize(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_AUTOCAP, true)

    fun setAutoCapitalize(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_AUTOCAP, value).apply()
    }

    fun getKeyboardHeight(context: Context): Int =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getInt(KEY_HEIGHT, 1)

    fun setKeyboardHeight(context: Context, value: Int) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putInt(KEY_HEIGHT, value).apply()
    }

    fun getClipHistory(context: Context): List<String> {
        val raw = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY_CLIPS, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(CLIP_DIVIDER).filter { it.isNotBlank() }
    }

    fun addClip(context: Context, text: String) {
        if (text.isBlank()) return
        val current = getClipHistory(context).toMutableList()
        current.remove(text)
        current.add(0, text)
        val trimmed = current.take(MAX_CLIPS)
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putString(KEY_CLIPS, trimmed.joinToString(CLIP_DIVIDER)).apply()
    }

    fun clearClips(context: Context) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().remove(KEY_CLIPS).apply()
    }

    fun getWordSuggestionsEnabled(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_WORD_SUGGEST, true)

    fun setWordSuggestionsEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_WORD_SUGGEST, value).apply()
    }

    fun getAutoCorrectEnabled(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_AUTOCORRECT, true)

    fun setAutoCorrectEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_AUTOCORRECT, value).apply()
    }

    fun getUndoAutocorrectEnabled(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_UNDO_AUTOCORRECT, true)

    fun setUndoAutocorrectEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_UNDO_AUTOCORRECT, value).apply()
    }

    fun getDoubleSpacePeriodEnabled(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_DOUBLE_SPACE_PERIOD, true)

    fun setDoubleSpacePeriodEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_DOUBLE_SPACE_PERIOD, value).apply()
    }

    fun getAutoSpacePunctuationEnabled(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_AUTO_SPACE_PUNCT, false)

    fun setAutoSpacePunctuationEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_AUTO_SPACE_PUNCT, value).apply()
    }

    fun getRememberCapsEnabled(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_REMEMBER_CAPS, false)

    fun setRememberCapsEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_REMEMBER_CAPS, value).apply()
    }

    fun getClipboardSuggestionsEnabled(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_CLIPBOARD_SUGGEST, true)

    fun setClipboardSuggestionsEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_CLIPBOARD_SUGGEST, value).apply()
    }

    fun getVibrateDuration(context: Context): Int =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getInt(KEY_VIBRATE_DURATION, 35)

    fun setVibrateDuration(context: Context, value: Int) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putInt(KEY_VIBRATE_DURATION, value).apply()
    }

    fun getSoundVolume(context: Context): Int =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getInt(KEY_SOUND_VOLUME, 50)

    fun setSoundVolume(context: Context, value: Int) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putInt(KEY_SOUND_VOLUME, value).apply()
    }

    fun getLongPressSoundEnabled(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_LONGPRESS_SOUND, false)

    fun setLongPressSoundEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_LONGPRESS_SOUND, value).apply()
    }

    fun getRepeatedVibrateEnabled(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_REPEATED_VIBRATE, true)

    fun setRepeatedVibrateEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_REPEATED_VIBRATE, value).apply()
    }

    fun getTheme(context: Context): String =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY_THEME, "indigo") ?: "indigo"

    fun setTheme(context: Context, value: String) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY_THEME, value).apply()
    }

    private const val KEY_RECENT_EMOJIS = "recent_emojis"
    private const val EMOJI_DIVIDER = "\u0003"
    private const val MAX_RECENT_EMOJIS = 20

    fun getRecentEmojis(context: Context): List<String> {
        val raw = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY_RECENT_EMOJIS, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(EMOJI_DIVIDER).filter { it.isNotBlank() }
    }

    fun addRecentEmoji(context: Context, emoji: String) {
        val current = getRecentEmojis(context).toMutableList()
        current.remove(emoji)
        current.add(0, emoji)
        val trimmed = current.take(MAX_RECENT_EMOJIS)
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putString(KEY_RECENT_EMOJIS, trimmed.joinToString(EMOJI_DIVIDER)).apply()
    }

    private const val KEY_PINNED_CLIPS = "pinned_clips"

    fun getPinnedClips(context: Context): List<String> {
        val raw = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY_PINNED_CLIPS, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(CLIP_DIVIDER).filter { it.isNotBlank() }
    }

    fun pinClip(context: Context, text: String) {
        val current = getPinnedClips(context).toMutableList()
        if (current.contains(text)) return
        current.add(0, text)
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putString(KEY_PINNED_CLIPS, current.joinToString(CLIP_DIVIDER)).apply()
    }

    fun unpinClip(context: Context, text: String) {
        val current = getPinnedClips(context).toMutableList()
        current.remove(text)
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putString(KEY_PINNED_CLIPS, current.joinToString(CLIP_DIVIDER)).apply()
    }

    private const val KEY_TOOLBAR_CHIPS = "toolbar_visible_chips"

    fun getVisibleToolbarChips(context: Context): Set<String> {
        val raw = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY_TOOLBAR_CHIPS, "") ?: ""
        return if (raw.isBlank()) emptySet() else raw.split(",").filter { it.isNotBlank() }.toSet()
    }

    fun setVisibleToolbarChips(context: Context, chipIds: Set<String>) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putString(KEY_TOOLBAR_CHIPS, chipIds.joinToString(",")).apply()
    }

    fun getOneHandedEnabled(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_ONE_HANDED, false)

    fun setOneHandedEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_ONE_HANDED, value).apply()
    }

    fun getOneHandedSide(context: Context): String =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY_ONE_HANDED_SIDE, "right") ?: "right"

    fun setOneHandedSide(context: Context, value: String) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY_ONE_HANDED_SIDE, value).apply()
    }

    fun getOneHandedWidth(context: Context): Int =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getInt(KEY_ONE_HANDED_WIDTH, 80)

    fun setOneHandedWidth(context: Context, value: Int) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putInt(KEY_ONE_HANDED_WIDTH, value).apply()
    }

    fun getSmartBarEnabled(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_SMARTBAR_ENABLED, true)

    fun setSmartBarEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_SMARTBAR_ENABLED, value).apply()
    }

    fun getSmartBarOnTop(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_SMARTBAR_TOP, true)

    fun setSmartBarOnTop(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_SMARTBAR_TOP, value).apply()
    }

    fun getLongPressDelay(context: Context): Int =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getInt(KEY_LONGPRESS_DELAY, 300)

    fun setLongPressDelay(context: Context, value: Int) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putInt(KEY_LONGPRESS_DELAY, value).apply()
    }

    fun getHintedNumbersEnabled(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_HINTED_NUMBERS, false)

    fun setHintedNumbersEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_HINTED_NUMBERS, value).apply()
    }

    fun getDictionaryWords(context: Context): List<String> {
        val raw = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY_DICTIONARY_WORDS, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(DICT_DIVIDER).filter { it.isNotBlank() }
    }

    fun addDictionaryWord(context: Context, word: String) {
        val trimmed = word.trim()
        if (trimmed.isBlank()) return
        val current = getDictionaryWords(context).toMutableList()
        if (current.any { it.equals(trimmed, ignoreCase = true) }) return
        current.add(0, trimmed)
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putString(KEY_DICTIONARY_WORDS, current.joinToString(DICT_DIVIDER)).apply()
    }

    fun removeDictionaryWord(context: Context, word: String) {
        val current = getDictionaryWords(context).toMutableList()
        current.removeAll { it.equals(word, ignoreCase = true) }
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putString(KEY_DICTIONARY_WORDS, current.joinToString(DICT_DIVIDER)).apply()
    }

    fun getLastCapsLockState(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_LAST_CAPS_LOCK, false)

    fun setLastCapsLockState(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(KEY_LAST_CAPS_LOCK, value).apply()
    }
}
