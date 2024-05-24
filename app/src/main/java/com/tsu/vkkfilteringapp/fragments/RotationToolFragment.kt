package com.tsu.vkkfilteringapp.fragments

import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.tsu.vkkfilteringapp.R
import com.tsu.vkkfilteringapp.SeekbarData
import com.tsu.vkkfilteringapp.TaskViewModel
import com.tsu.vkkfilteringapp.databinding.FragmentAffineToolBinding
import com.tsu.vkkfilteringapp.databinding.FragmentRotationToolBinding

class RotationToolFragment : Fragment() {

    private lateinit var binding: FragmentRotationToolBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var angleButtonTransition: TransitionDrawable
    private val angleSeekbar = SeekbarData(271, -180, 180)
    private val transitionDuration = 300

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]

        angleButtonTransition = binding.rotationAngleCall.background as TransitionDrawable

        binding.rotationAngleCall.setOnClickListener {

            if (taskViewModel.isSeekbarWrapperActuallyHidden) {

                taskViewModel.seekbarFragment.showBar(binding.rotationAngleCall,
                    R.string.rotation_value, taskViewModel.rotationToolAngle, angleSeekbar)
                taskViewModel.seekbarFragment.showBar(binding.rotationAngleCall,
                    R.string.rotation_value, taskViewModel.rotationToolAngle, angleSeekbar)

                taskViewModel.seekbarWrapperHide.value = false
                angleButtonTransition.startTransition(transitionDuration)
            }
            else {
                taskViewModel.seekbarWrapperHide.value = true
                angleButtonTransition.reverseTransition(transitionDuration)
            }
        }

        binding.rotationApply.setOnClickListener {
            taskViewModel.rotationToolNeedToUpdate.value = true
        }

        taskViewModel.rotationToolAngle.observe(requireActivity()) {
            binding.rotationAngleCall.text = resources.getString(R.string.rotation_value, taskViewModel.rotationToolAngle.value!!.toInt())
        }

        binding.rotationAngleCall.text = resources.getString(R.string.rotation_value, taskViewModel.rotationToolAngle.value!!.toInt())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRotationToolBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = RotationToolFragment()
    }
}