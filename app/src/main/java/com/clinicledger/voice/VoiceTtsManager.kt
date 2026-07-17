package com.clinicledger.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * High-level manager for clinical Text-To-Speech.
 * Provides chimes and vocal briefing capabilities.
 */
class VoiceTtsManager(/** context */ context: Context) {

    private var tts: TextToSpeech? = null
    private var isReady = false

    private val initListener = TextToSpeech.OnInitListener { status ->
        if (status == TextToSpeech.SUCCESS) {
            val hindiLocale = Locale.forLanguageTag("hi-IN")
            val result = tts?.setLanguage(hindiLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.US
            }
            isReady = true
        }
    }

    init {
        tts = TextToSpeech(context, initListener)
    }

    /** 
     * Speaks the given text and optionally triggers a callback on completion.
     */
    fun speak(/** narrative */ text: String, /** callback */ onDone: (() -> Unit)? = null) {
        if (!isReady) return

        tts?.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(/** ID */ utteranceId: String?) {}
                override fun onDone(/** ID */ utteranceId: String?) {
                    onDone?.invoke()
                }
                @Deprecated("Legacy")
                override fun onError(/** ID */ utteranceId: String?) {}
            },
        )

        val utteranceId = "vcl_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    /** 
     * Forces immediate silence.
     */
    fun stop() {
        tts?.stop()
    }
}
