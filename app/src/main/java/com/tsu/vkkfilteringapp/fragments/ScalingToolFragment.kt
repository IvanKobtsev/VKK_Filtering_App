package com.tsu.vkkfilteringapp.fragments

import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.tsu.vkkfilteringapp.R
import com.tsu.vkkfilteringapp.SeekbarData
import com.tsu.vkkfilteringapp.TaskViewModel
import com.tsu.vkkfilteringapp.databinding.FragmentScalingToolBinding

class ScalingToolFragment : Fragment() {

    private lateinit var binding: FragmentScalingToolBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var scaleButtonTransition: TransitionDrawable
    private val scaleSeekbar = SeekbarData(50, 5, 400, 100F)
    private val transitionDuration = 300

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]

        scaleButtonTransition = binding.scalingScaleCall.background as TransitionDrawable

        binding.scalingScaleCall.setOnClickListener {

            if (taskViewModel.isSeekbarWrapperActuallyHidden) {

                taskViewModel.seekbarFragment.showBar(binding.scalingScaleCall,
                    R.string.scaling_value, taskViewModel.scalingToolScale, scaleSeekbar)
                taskViewModel.seekbarFragment.showBar(binding.scalingScaleCall,
                    R.string.scaling_value, taskViewModel.scalingToolScale, scaleSeekbar)

                taskViewModel.seekbarWrapperHide.value = false
                scaleButtonTransition.startTransition(transitionDuration)
            }
            else {
                taskViewModel.seekbarWrapperHide.value = true
                scaleButtonTransition.reverseTransition(transitionDuration)
            }
        }

        binding.scalingApply.setOnClickListener {
            taskViewModel.scalingToolNeedToUpdate.value = true
        }

        taskViewModel.scalingToolScale.observe(requireActivity()) {
            binding.scalingScaleCall.text = resources.getString(R.string.scaling_value, taskViewModel.scalingToolScale.value!!.toString())
        }

        binding.scalingScaleCall.text = resources.getString(R.string.scaling_value, taskViewModel.scalingToolScale.value!!.toString())

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentScalingToolBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = ScalingToolFragment()
    }
}