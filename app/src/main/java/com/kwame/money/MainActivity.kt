package com.kwame.money

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }

        val title = TextView(this).apply {
            text = "Money Keyboard"
            textSize = 22f
        }

        val subtitle = TextView(this).apply {
            text = "Phase 1: keyboard skeleton. Enable it below, then select it as your keyboard."
            textSize = 14f
            setPadding(0, 24, 0, 48)
        }

        val enableButton = Button(this).apply {
            text = "Enable Money Keyboard"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(enableButton)

        setContentView(layout)
    }
}
