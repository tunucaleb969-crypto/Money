package com.kwame.money

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Shows word-completion suggestions (while typing a word) or next-word
 * predictions (right after finishing one). Stays a fixed height even when
 * empty so the keyboard layout doesn't jump around as you type.
 */
@Composable
fun SuggestionBar(
    suggestions: List<String>,
    onSuggestionTap: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(Color(0xFF15151F))
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        suggestions.take(3).forEach { word ->
            Text(
                text = word,
                color = Color.White,
                modifier = Modifier
                    .clickable { onSuggestionTap(word) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}
