package com.kwame.money

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EditToolbar(
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSelectAll: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onPaste: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF15151F))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ToolbarButton("Undo", onUndo)
        ToolbarButton("Redo", onRedo)
        ToolbarButton("Select All", onSelectAll)
        ToolbarButton("Copy", onCopy)
        ToolbarButton("Cut", onCut)
        ToolbarButton("Paste", onPaste)
    }
}

@Composable
private fun ToolbarButton(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = Color.White,
        fontSize = 12.sp,
        modifier = Modifier
            .clickable { onClick() }
            .padding(6.dp)
    )
}
