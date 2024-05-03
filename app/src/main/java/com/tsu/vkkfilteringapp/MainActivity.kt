package com.tsu.vkkfilteringapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button: Button = findViewById(R.id.tDCubeOpener)

        button.setOnClickListener {
            val intent = Intent(this, TDCubeRenderer::class.java)
            startActivity(intent)
        }

//        button.setBackgroundColor(0)
    }
}