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
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tsu.vkkfilteringapp.R
import com.tsu.vkkfilteringapp.TDCubeRenderer
import com.tsu.vkkfilteringapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var binding: ActivityMainBinding
    private lateinit var animFadeIn: Animation
    private lateinit var animFadeOut: Animation
    private lateinit var animAppear: Animation
    private lateinit var animDisappear: Animation
    private lateinit var buttons: List<Button>


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
            Log.e("resumed", "resumed" + binding.overlay.alpha.toString())
            overlayAnimTimer.start()
        }

        registerPermission()
        requestPermission()
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

        for (button in buttons) {
            button.isEnabled = false
        }
    }

    private fun registerPermission() {

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            }
    }

    private fun informationAboutPermission(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.dialog_title)
        builder.setMessage(R.string.dialog_message)
        val dialog = builder.create()
        dialog.show()
    }

    private fun requestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(this,"Здравствуйте! ",Toast.LENGTH_SHORT).show()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> {
                Log.e("resumed", "req123")
                informationAboutPermission()
            }


            else -> {
                informationAboutPermission()
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}