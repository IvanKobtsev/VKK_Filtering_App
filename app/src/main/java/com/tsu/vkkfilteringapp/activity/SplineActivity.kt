package com.tsu.vkkfilteringapp.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.widget.AdapterView
import android.widget.AdapterViewAnimator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tsu.vkkfilteringapp.databinding.ActivitySplineBinding
import com.tsu.vkkfilteringapp.spline.SplineCanvasView

class SplineActivity: AppCompatActivity() {
    private lateinit var binding:ActivitySplineBinding
    private var itemPosition:Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplineBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.spSpline.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                //Toast.makeText(this@SplineActivity,"Вы выбрали ${parent?.getItemAtPosition(position).toString()}",Toast.LENGTH_LONG).show()
                itemPosition = position

            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }

        }
        binding.forward.setOnClickListener{
            binding.splineCanvasView.setOperatingModes(5)
        }

        binding.back.setOnClickListener{
            binding.splineCanvasView.setOperatingModes(4)
        }

        binding.update.setOnClickListener{
            binding.splineCanvasView.setOperatingModes(6)
        }


        binding.start.setOnClickListener{
            binding.splineCanvasView.setOperatingModes(itemPosition)
        }
    }
}