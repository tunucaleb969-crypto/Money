package com.kwame.money

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(context: Context) {
    var soundEnabled by remember { mutableStateOf(Prefs.getSoundEnabled(context)) }
    var vibrateEnabled by remember { mutableStateOf(Prefs.getVibrateEnabled(context)) }
    var autoCapitalize by remember { mutableStateOf(Prefs.getAutoCapitalize(context)) }
    var wordSuggestions by remember { mutableStateOf(Prefs.getWordSuggestionsEnabled(context)) }
    var autoCorrect by remember { mutableStateOf(Prefs.getAutoCorrectEnabled(context)) }
    var apiKey by remember { mutableStateOf(Prefs.getApiKey(context)) }
    var newWord by remember { mutableStateOf("") }
    var dictionaryWords by remember { mutableStateOf(Prefs.getDictionaryWords(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Money Keyboard", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }) {
            Text("Enable / Select Keyboard")
        }

        Spacer(Modifier.height(24.dp))
        Text("Typing", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        SettingRow("Key sound", soundEnabled) {
            soundEnabled = it; Prefs.setSoundEnabled(context, it)
        }
        SettingRow("Vibrate on key press", vibrateEnabled) {
            vibrateEnabled = it; Prefs.setVibrateEnabled(context, it)
        }
        SettingRow("Auto-capitalize sentences", autoCapitalize) {
            autoCapitalize = it; Prefs.setAutoCapitalize(context, it)
        }
        SettingRow("Word suggestions", wordSuggestions) {
            wordSuggestions = it; Prefs.setWordSuggestionsEnabled(context, it)
        }
        SettingRow("Autocorrect", autoCorrect) {
            autoCorrect = it; Prefs.setAutoCorrectEnabled(context, it)
        }

        Spacer(Modifier.height(24.dp))
        Text("Gemini API Key", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it; Prefs.setApiKey(context, it) },
            label = { Text("Paste your Gemini API key") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))
        Text("Personal Dictionary", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedTextField(
                value = newWord,
                onValueChange = { newWord = it },
                label = { Text("Add a word") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (newWord.isNotBlank()) {
                        Prefs.addDictionaryWord(context, newWord)
                        dictionaryWords = Prefs.getDictionaryWords(context)
                        newWord = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Add")
            }
        }

        dictionaryWords.forEach { word ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(word)
                Text(
                    "Remove",
                    color = Color.Red,
                    modifier = Modifier.clickable {
                        Prefs.removeDictionaryWord(context, word)
                        dictionaryWords = Prefs.getDictionaryWords(context)
                    }
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
