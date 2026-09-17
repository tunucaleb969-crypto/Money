package com.kwame.money

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.widget.TextView

/**
 * Phase 1 skeleton: proves the IME lifecycle works end to end
 * (install -> enable -> select -> keyboard view appears).
 * Real keys, Compose UI, and Gemini integration come in later phases.
 */
class MoneyKeyboardService : InputMethodService() {

    override fun onCreateInputView(): View {
        return TextView(this).apply {
            text = "Money Keyboard \u2014 Phase 1 skeleton"
            gravity = Gravity.CENTER
            setPadding(32, 64, 32, 64)
            setBackgroundColor(Color.parseColor("#1E1E2E"))
            setTextColor(Color.WHITE)
            textSize = 16f
        }
    }
}
