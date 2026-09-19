package com.kwame.money

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

/** Plain-View suggestion bar (word predictions / dictionary matches). */
class SuggestionBarView(context: Context) : LinearLayout(context) {

    var onSuggestionTap: ((String) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        setBackgroundColor(Color.parseColor("#161620"))
    }

    fun render(suggestions: List<String>) {
        removeAllViews()
        if (suggestions.isEmpty()) {
            // Keep a minimum height so the keyboard doesn't jump when suggestions appear/disappear.
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0)
            return
        }
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        suggestions.forEach { word ->
            addView(TextView(context).apply {
                text = word
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setPadding(16, 28, 16, 28)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener { onSuggestionTap?.invoke(word) }
            })
        }
    }
}
