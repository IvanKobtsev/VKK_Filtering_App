package com.tsu.vkkfilteringapp

import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore.Audio.Media
import android.widget.VideoView
import androidx.appcompat.app.ActionBar
import com.tsu.vkkfilteringapp.databinding.ActivityTdcubeRendererBinding

class TDCubeRenderer : AppCompatActivity() {

    private lateinit var binding: ActivityTdcubeRendererBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTdcubeRendererBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bgPlayer = findViewById<VideoView>(R.id.bgPlayer)
        val adLink = Uri.parse("android.resource://$packageName/${R.raw.cube_bg}")
        bgPlayer.setVideoURI(adLink)

        binding.fastRenderingView.setZOrderOnTop(true)
        binding.fastRenderingView.holder.setFormat(PixelFormat.TRANSLUCENT)
        binding.fastRenderingView.init(binding.showImages.isChecked)

        binding.showImages.setOnClickListener{
            binding.fastRenderingView.switchImages(binding.showImages.isChecked)
        }

        binding.previousShapeButton.setOnClickListener{
            binding.nextShapeButton.setEnabled(true)
            binding.nextShapeButton.alpha = 1F
            binding.fastRenderingView.previousShape(binding.previousShapeButton)
        }

        binding.nextShapeButton.setOnClickListener{
            binding.previousShapeButton.setEnabled(true)
            binding.previousShapeButton.alpha = 1F
            binding.fastRenderingView.nextShape(binding.nextShapeButton)
        }

        binding.bgPlayer.setOnPreparedListener { mediaPlayer: MediaPlayer ->

            mediaPlayer.isLooping = true

            mediaPlayer.setScreenOnWhilePlaying(false)
            val videoRatio = mediaPlayer.videoWidth / mediaPlayer.videoHeight.toFloat()
            val screenRatio = binding.bgPlayer.width / binding.bgPlayer.height.toFloat()
            val scaleX = videoRatio / screenRatio
            if (scaleX >= 1f) {
                binding.bgPlayer.scaleX = scaleX
            } else {
                binding.bgPlayer.scaleY = 1F / scaleX
            }

            mediaPlayer.start()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (!hasFocus) {
            binding.fastRenderingView.surfaceDestroyed(binding.fastRenderingView.holder)
        }
        else {
            binding.showImages.isChecked = false
        }
    }
}