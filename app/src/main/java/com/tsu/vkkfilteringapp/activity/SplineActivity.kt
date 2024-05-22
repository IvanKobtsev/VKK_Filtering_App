package com.tsu.vkkfilteringapp.activity

import android.os.Bundle
import android.util.Log
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import androidx.appcompat.app.AppCompatActivity
import com.tsu.vkkfilteringapp.databinding.ActivitySplineBinding
import com.tsu.vkkfilteringapp.spline.SplineCanvasView

class SplineActivity: AppCompatActivity() {
    private lateinit var binding:ActivitySplineBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("splineE","13")
        binding = ActivitySplineBinding.inflate(layoutInflater)
        Log.e("splineE","16")
        setContentView(binding.root)

        binding.back.setOnClickListener{
            binding.splineCanvasView.setOperatingModes(2)
        }

        binding.update.setOnClickListener{
            binding.splineCanvasView.setOperatingModes(3)
        }

        binding.closeSpline.setOnClickListener {
            binding.splineCanvasView.setOperatingModes(4)
        }

        binding.start.setOnClickListener{
            binding.splineCanvasView.setOperatingModes(5)
        }
    }
}