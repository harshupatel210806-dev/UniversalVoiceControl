package com.patel.universalvoicecontrol

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.view.Gravity
import android.widget.*
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var status: TextView
    private val REQ_AUDIO = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val title = TextView(this).apply {
            text = "Universal Voice Control — V1.1"
            textSize = 24f
            gravity = Gravity.CENTER
        }
        root.addView(title, LinearLayout.LayoutParams(-1, -2))

        status = TextView(this).apply {
            textSize = 16f
            setPadding(0, 24, 0, 24)
        }
        root.addView(status)

        val accessibility = Button(this).apply {
            text = "Enable Accessibility Service"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }
        root.addView(accessibility, LinearLayout.LayoutParams(-1, -2))

        val listen = Button(this).apply {
            text = "🎤 Speak Command"
            textSize = 18f
            setOnClickListener { listenOnce() }
        }
        root.addView(listen, LinearLayout.LayoutParams(-1, -2))

        val help = TextView(this).apply {
            text = """
Examples:
• WhatsApp / વોટ્સએપ ખોલો
• YouTube ખોલો અને search GTA 5
• પાછળ / હોમ / Recent Apps
• Scroll Down / Scroll Up
• Tap Send / ટેપ Send
• Type Hello Hardik / લખ Hello Hardik
• Search GTA 5 / સર્ચ GTA 5
            """.trimIndent()
            textSize = 16f
            setPadding(0, 28, 0, 0)
        }
        root.addView(help)

        setContentView(root)
        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val a11y = UniversalAccessibilityService.instance != null
        val mic = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        status.text = "Microphone: ${if (mic) "ON" else "OFF"}\nAccessibility: ${if (a11y) "CONNECTED" else "NOT ENABLED"}"
    }

    private fun listenOnce() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), REQ_AUDIO)
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a command")
        }
        startActivityForResult(intent, 200)
    }

    @Deprecated("Use Activity Result API in future versions")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == RESULT_OK) {
            val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                val result = VoiceCommandParser.parse(this, spoken)
                Toast.makeText(this, result, Toast.LENGTH_LONG).show()
            }
        }
    }
}
