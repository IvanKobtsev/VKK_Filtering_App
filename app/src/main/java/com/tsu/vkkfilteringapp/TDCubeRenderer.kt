package com.tsu.vkkfilteringapp

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.MediaController
import android.widget.VideoView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.tsu.vkkfilteringapp.databinding.ActivityTdcubeRendererBinding

class TDCubeRenderer : AppCompatActivity() {

    private lateinit var binding: ActivityTdcubeRendererBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTdcubeRendererBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adPlayer = findViewById<VideoView>(R.id.adPlayer)
        val adLink = Uri.parse("android.resource://$packageName/${R.raw.the_battle_of_kulikovo}")
        adPlayer.setVideoURI(adLink)

        binding.fastRenderingView.init()
        binding.showImages.setOnClickListener{
            binding.fastRenderingView.switchImages()
        }

    }

    // Timer
//    val adTimer = object: CountDownTimer(30000, 30000) {
//
//        override fun onTick(millisUntilFinished: Long) {
//
//        }
//
//        override fun onFinish() {
//            binding.adPlayer.alpha = 1F
//            binding.adPlayer.requestFocus(0)
//            binding.adPlayer.start()
//        }
//    }.start()

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
//        binding.myCanvas.init(binding.myCanvas.width, binding.myCanvas.height)
    }
}