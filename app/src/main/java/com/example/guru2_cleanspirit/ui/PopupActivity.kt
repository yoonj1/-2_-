package com.example.guru2_cleanspirit.ui

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.guru2_cleanspirit.R

class PopupActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popup)

        // Find UI elements
        val messageTextView: TextView = findViewById(R.id.messageTextView)
        val closeButton: Button = findViewById(R.id.closebutton)

        // Set a message (customize as needed)
        messageTextView.text = "Target app is running!"

        // Close button click listener
        closeButton.setOnClickListener {
            finish() // Close the activity
        }
    }
}
