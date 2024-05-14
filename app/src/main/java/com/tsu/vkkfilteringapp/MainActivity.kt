package com.tsu.vkkfilteringapp

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.VideoView
import com.tsu.vkkfilteringapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var animFadeIn: Animation
    private lateinit var animFadeOut: Animation
    private lateinit var animAppear: Animation
    private lateinit var animDisappear: Animation


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

        // Setting binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Loading animation
        animFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        animFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        animAppear = AnimationUtils.loadAnimation(this, R.anim.appear)

        // Event listeners
        binding.tDCubeOpener.setOnClickListener {
            val intent = Intent(this, TDCubeRenderer::class.java)
            startActivity(intent)
        }

        binding.imageEditorOpener.setOnClickListener{
            val intent = Intent(this,ImageActivity::class.java)
            startActivity(intent)
        }

        // Video view handling
        val bgPlayer = findViewById<VideoView>(R.id.bgPlayer)
        val adLink = Uri.parse("android.resource://$packageName/${R.raw.title_bg}")
        bgPlayer.setVideoURI(adLink)

        Log.e("WTF", "wtf")

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

    // Timer
    private val overlayAnimTimer = object: CountDownTimer(2000, 2000) {

        override fun onTick(millisUntilFinished: Long) {
            binding.overlay.alpha = 1F
        }

        override fun onFinish() {
            binding.overlay.startAnimation(animFadeOut)
        }
    }

    override fun onResume() {
        super.onResume()

        binding.overlay.startAnimation(animAppear)
        Log.e("resumed", "resumed" + binding.overlay.alpha.toString())
        overlayAnimTimer.start()
    }
}