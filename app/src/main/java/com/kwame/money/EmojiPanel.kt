package com.kwame.money

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Full emoji panel: search box, recent emojis, and the common-emoji grid.
 * Replaces the main keyboard while open (toggled by the \uD83D\uDE0A key).
 */
@Composable
fun EmojiPanel(
    recentEmojis: List<String>,
    onEmojiTap: (String) -> Unit,
    onClose: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val displayed = if (query.isBlank()) {
        (recentEmojis + EmojiSuggester.commonEmojis).distinct()
    } else {
        EmojiSuggester.searchEmojis(query)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E2E))
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
            Box(modifier = Modifier.weight(1f).background(Color(0xFF2E2E3E)).padding(8.dp)) {
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    decorationBox = { innerTextField ->
                        if (query.isEmpty()) {
                            Text("Search emoji", color = Color.Gray, fontSize = 14.sp)
                        }
                        innerTextField()
                    }
                )
            }
            Text(
                text = "\u2715",
                color = Color.White,
                modifier = Modifier.clickable { onClose() }.padding(8.dp)
            )
        }

        if (displayed.isEmpty()) {
            Text("No emoji found", color = Color.Gray, modifier = Modifier.padding(12.dp))
        } else {
            displayed.chunked(8).forEach { rowEmojis ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    rowEmojis.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 22.sp,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onEmojiTap(emoji) }
                                .padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}
