package com.tsu.vkkfilteringapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class TDCubeRenderer : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val draw2D = Draw2D(this)
        setContentView(draw2D)
        draw2D.init()

//        val drawTheCube: Button = findViewById(R.id.startDrawingButton)
//
//        drawTheCube.setOnClickListener {
//
//        }
    }
}