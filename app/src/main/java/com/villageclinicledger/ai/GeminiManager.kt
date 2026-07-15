package com.villageclinicledger.ai

import com.villageclinicledger.data.models.Village
import com.villageclinicledger.voice.ParsedVoiceIntent
import com.villageclinicledger.voice.VoiceIntentParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Local voice parsing facade.
 *
 * The app must work offline and must not ship cloud API keys in the client APK.
 * Keep this boundary so a future server-side AI enhancer can be added without
 * making cloud output the ledger's source of truth.
 */
object GeminiManager {
    fun isGeminiActive(): Boolean = false

    suspend fun parseVoiceInput(
        text: String,
        villages: List<Village>
    ): ParsedVoiceIntent = withContext(Dispatchers.Default) {
        VoiceIntentParser.parse(text, villages)
    }

    suspend fun getBilingualTranslation(input: String): String = withContext(Dispatchers.Default) {
        input.trim()
    }
}
