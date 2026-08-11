package com.jarvis.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var status: TextView
    private lateinit var answer: TextView
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        status = findViewById(R.id.status)
        answer = findViewById(R.id.answer)

        val micButton: Button = findViewById(R.id.micButton)

        tts = TextToSpeech(this, this)

        micButton.setOnClickListener {
            startListening()
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
        }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Speak to Jarvis"
        )

        status.text = "Listening..."

        try {
            startActivityForResult(intent, 101)
        } catch (e: Exception) {
            status.text = "Speech recognition unavailable"
            speak("Speech recognition is not available.")
        }
    }

    @Deprecated("Deprecated API")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != 101) return

        val results =
            data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )

        val command = results?.firstOrNull()

        if (command.isNullOrEmpty()) {
            status.text = "Ready"
            return
        }

        status.text = "Command received"
        answer.text = "You: $command"

        handleCommand(command.lowercase(Locale.getDefault()))
    }

    private fun handleCommand(command: String) {

        val response = when {
            command.contains("hello") ||
            command.contains("hi") ->
                "Hello. How can I help you?"

            command.contains("jarvis") ->
                "Yes, I am listening."

            else ->
                "I heard you. AI features will be added next."
        }

        answer.text = "Jarvis: $response"

        speak(response)
    }

    private fun speak(text: String) {
        if (::tts.isInitialized) {
            tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "JARVIS"
            )
        }
    }

    override fun onInit(statusCode: Int) {

        if (statusCode == TextToSpeech.SUCCESS) {
            tts.language = Locale.getDefault()
            speak("Jarvis is ready.")
        }
    }

    override fun onDestroy() {

        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }

        super.onDestroy()
    }
}