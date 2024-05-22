package com.tsu.vkkfilteringapp

import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.tsu.vkkfilteringapp.databinding.FragmentAffineToolBinding
import com.tsu.vkkfilteringapp.graphics2d.Triangle2D

class AffineToolFragment : Fragment() {

    private lateinit var binding: FragmentAffineToolBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var transitions: List<TransitionDrawable>
    private val transitionDuration = 200

    fun setPoint(pointX: Float, pointY: Float) {

        if (taskViewModel.affineToolSelectedPoint.value!! < 3) {
            taskViewModel.affineToolOrigTriangle.vertices[taskViewModel.affineToolSelectedPoint.value!!].setCoordinates(pointX, pointY)
        }
        else {
            taskViewModel.affineToolTransTriangle.vertices[taskViewModel.affineToolSelectedPoint.value!! - 3].setCoordinates(pointX, pointY)
        }

        var newSelectedPoint = taskViewModel.affineToolSelectedPoint.value!! + 1

        if (newSelectedPoint > 5) {
            newSelectedPoint = 0
        }

        selectVertex(newSelectedPoint)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity()
        taskViewModel = ViewModelProvider(activity)[TaskViewModel::class.java]

        transitions = listOf(
            binding.affineButtonOrigTri1.background as TransitionDrawable,
            binding.affineButtonOrigTri2.background as TransitionDrawable,
            binding.affineButtonOrigTri3.background as TransitionDrawable,
            binding.affineButtonTransTri1.background as TransitionDrawable,
            binding.affineButtonTransTri2.background as TransitionDrawable,
            binding.affineButtonTransTri3.background as TransitionDrawable
        )

        transitions[0].startTransition(transitionDuration)
        taskViewModel.affineToolSelectedPoint.value = 0

        binding.affineApply.setOnClickListener {
            taskViewModel.affineToolNeedToUpdate.value = true
        }

        binding.affineButtonOrigTri1.setOnClickListener {
            selectVertex(0)
        }

        binding.affineButtonOrigTri2.setOnClickListener {
            selectVertex(1)
        }

        binding.affineButtonOrigTri3.setOnClickListener {
            selectVertex(2)
        }

        binding.affineButtonTransTri1.setOnClickListener {
            selectVertex(3)
        }

        binding.affineButtonTransTri2.setOnClickListener {
            selectVertex(4)
        }

        binding.affineButtonTransTri3.setOnClickListener {
            selectVertex(5)
        }

        taskViewModel.affineToolCanApply.observe(activity) {
            binding.affineApply.isEnabled = it
            if (it) {
                binding.affineApply.alpha = 0.8F
            }
            else {
                binding.affineApply.alpha = 0.4F
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAffineToolBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = AffineToolFragment()
    }

    private fun selectVertex(newCurrentVertex: Int) {
        transitions[taskViewModel.affineToolSelectedPoint.value!!].reverseTransition(transitionDuration)
        transitions[newCurrentVertex].startTransition(transitionDuration)
        taskViewModel.affineToolSelectedPoint.value = newCurrentVertex
    }
}