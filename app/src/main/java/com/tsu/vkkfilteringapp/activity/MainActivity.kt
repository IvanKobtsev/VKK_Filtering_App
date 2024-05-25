package com.tsu.vkkfilteringapp.activity

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.CountDownTimer
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tsu.vkkfilteringapp.R
import com.tsu.vkkfilteringapp.TDCubeRenderer
import com.tsu.vkkfilteringapp.databinding.ActivityMainBinding
import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var binding: ActivityMainBinding
    private lateinit var animFadeIn: Animation
    private lateinit var animFadeOut: Animation
    private lateinit var animAppear: Animation
    private lateinit var buttons: List<Button>
    private var fullyLoaded = false

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

        // Loading animations
        animFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        animFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        animAppear = AnimationUtils.loadAnimation(this, R.anim.appear)

        // Buttons
        buttons = listOf(binding.imageEditorOpener, binding.splineEditorOpener, binding.tDCubeOpener)

        // Event listeners
        binding.tDCubeOpener.setOnClickListener {
            val intent = Intent(this, TDCubeRenderer::class.java)
            startActivity(intent)
        }

        binding.imageEditorOpener.setOnClickListener{
            val intent = Intent(this, ImageActivity::class.java)
            startActivity(intent)
        }

        binding.splineEditorOpener.setOnClickListener{
            val intent = Intent(this, SplineActivity::class.java)
            startActivity(intent)
        }

        // Video view handling
        val bgPlayer = findViewById<VideoView>(R.id.bgPlayer)
        val adLink = Uri.parse("android.resource://$packageName/${R.raw.title_bg}")
        bgPlayer.setVideoURI(adLink)

        binding.bgPlayer.setOnPreparedListener { mediaPlayer: MediaPlayer ->

            mediaPlayer.isLooping = true
            fullyLoaded = true

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

            binding.overlay.startAnimation(animAppear)
            overlayAnimTimer.start()
        }

        registerPermission()
        requestPermission()

        if(!OpenCVLoader.initDebug()) {
//            Toast.makeText(this, "OpenCV is not found.", Toast.LENGTH_SHORT).show()
        }
        else{
//            Toast.makeText(this, "OpenCV is SUCCESSFULLY found!", Toast.LENGTH_SHORT).show()
        }
    }

    // Timer
    private val overlayAnimTimer = object: CountDownTimer(2000, 2000) {

        override fun onTick(millisUntilFinished: Long) {
            binding.overlay.alpha = 1F
        }

        override fun onFinish() {
            binding.overlay.startAnimation(animFadeOut)

            for (button in buttons) {
                button.isEnabled = true
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (!fullyLoaded) {
            for (button in buttons) {
                button.isEnabled = false
            }
        }
    }

    private fun registerPermission() {

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
    }

    private fun requestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("Permission: ", "Granted")
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> {
                Toast.makeText(
                    this,
                    "Нам необходимо разрешение, чтобы вы могли загружать отснятые фото напрямую в редактор",
                    Toast.LENGTH_LONG
                ).show()
            }


            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}