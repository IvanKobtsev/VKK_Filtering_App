package com.tsu.vkkfilteringapp

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.tsu.vkkfilteringapp.databinding.ActivityMainBinding
import android.content.Intent
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    init {
        instance = this
    }

    companion object {
        private var instance: MainActivity? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val button: Button = findViewById(R.id.tDCubeOpener)

        button.setOnClickListener {
            val intent = Intent(this, TDCubeRenderer::class.java)
            startActivity(intent)
        }
        binding.imageEditorOpener.setOnClickListener{
            Log.e("binding","37")
            val intent = Intent(this,ImageActivity::class.java)
            Log.e("binding","38")
            startActivity(intent)
            Log.e("binding","39")
        }


    }
}