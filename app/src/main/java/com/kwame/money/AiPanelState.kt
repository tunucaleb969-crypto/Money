package com.kwame.money

/** UI state for the AI action panel — mirrors the state-machine idea from the
 * project's architecture notes (Idle -> Loading -> Result/Error), kept simple
 * for this first working slice. */
sealed class AiPanelState {
    object Idle : AiPanelState()
    object Loading : AiPanelState()
    data class Ready(val resultText: String) : AiPanelState()
    data class Failed(val message: String) : AiPanelState()
}
