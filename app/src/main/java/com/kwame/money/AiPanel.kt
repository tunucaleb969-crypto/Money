package com.kwame.money

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shows the current AI action state. Deliberately never replaces the user's
 * text on its own — Ready state always requires an explicit Insert tap.
 */
@Composable
fun AiPanel(
    state: AiPanelState,
    onAction: (AiActionType) -> Unit,
    onInsert: (String) -> Unit,
    onRegenerate: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E2E))
            .padding(8.dp)
    ) {
        when (state) {
            is AiPanelState.Idle -> {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    ActionChip("Fix Grammar") { onAction(AiActionType.FIX_GRAMMAR) }
                    ActionChip("Formal") { onAction(AiActionType.REWRITE_FORMAL) }
                    ActionChip("Casual") { onAction(AiActionType.REWRITE_CASUAL) }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                ) {
                    ActionChip("Shorten") { onAction(AiActionType.SHORTEN) }
                    ActionChip("Expand") { onAction(AiActionType.EXPAND) }
                    ActionChip("Translate \u2192 Twi") { onAction(AiActionType.TRANSLATE) }
                    ActionChip("Suggest Reply") { onAction(AiActionType.REPLY_SUGGESTION) }
                }
            }

            is AiPanelState.Loading -> {
                Text("Working on it\u2026", color = Color.White, modifier = Modifier.padding(12.dp))
            }

            is AiPanelState.Ready -> {
                Text(
                    text = state.resultText,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2E2E3E))
                        .padding(10.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActionChip("Insert") { onInsert(state.resultText) }
                    ActionChip("Regenerate", onRegenerate)
                    ActionChip("Dismiss", onDismiss)
                }
            }

            is AiPanelState.Failed -> {
                Text("Couldn't complete that: ${state.message}", color = Color(0xFFFF8A80), modifier = Modifier.padding(8.dp))
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    ActionChip("Dismiss", onDismiss)
                }
            }
        }
    }
}

@Composable
private fun ActionChip(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = Color.White,
        fontSize = 12.sp,
        modifier = Modifier
            .background(Color(0xFF3A3A4A))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    )
}
