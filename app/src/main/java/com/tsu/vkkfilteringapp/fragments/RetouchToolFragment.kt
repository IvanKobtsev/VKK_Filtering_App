package com.tsu.vkkfilteringapp.fragments

import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.tsu.vkkfilteringapp.R
import com.tsu.vkkfilteringapp.SeekbarData
import com.tsu.vkkfilteringapp.TaskViewModel
import com.tsu.vkkfilteringapp.databinding.FragmentAffineToolBinding
import com.tsu.vkkfilteringapp.databinding.FragmentRetouchToolBinding

class RetouchToolFragment : Fragment() {

    private lateinit var binding: FragmentRetouchToolBinding
    private lateinit var taskViewModel: TaskViewModel

    private lateinit var buttonTransitions: List<TransitionDrawable>
    private lateinit var buttons: List<Button>
    private var currentSeekBar = -1
    private val transitionDuration = 300

    private val stringIDs = listOf(
        R.string.retouch_brush_radius,
        R.string.retouch_intensity
    )

    // Seekbars
    private val seekbarsData = listOf(
        SeekbarData(19, 1, 50),
        SeekbarData(9, 1, 20, 10F)
    )

    // Live-data values
    private lateinit var liveData: List<MutableLiveData<Float>>
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]

        liveData = listOf(
            taskViewModel.retouchToolBrushRadius,
            taskViewModel.retouchToolIntensity
        )

        // Buttons
        buttons = listOf(
            binding.brushRadiusCall,
            binding.intensityCall
        )

        // Transitions setting
        buttonTransitions = listOf(
            binding.brushRadiusCall.background as TransitionDrawable,
            binding.intensityCall.background as TransitionDrawable
        )

        binding.retouchApply.setOnClickListener {
            taskViewModel.retouchToolNeedToUpdate.value = true
            binding.retouchApply.isEnabled = false
            binding.retouchApply.alpha = 0.5F
        }

        binding.brushRadiusCall.setOnClickListener {
            switchSeekbar(0)
        }

        binding.intensityCall.setOnClickListener {
            switchSeekbar(1)
            switchSeekbar(1)
        }

        taskViewModel.retouchToolBrushRadius.observe(requireActivity()) {
            binding.brushRadiusCall.text = resources.getString(R.string.retouch_brush_radius, it.toString())
        }

        taskViewModel.retouchToolIntensity.observe(requireActivity()) {
            binding.intensityCall.text = resources.getString(R.string.retouch_intensity, it.toString())
        }

        taskViewModel.seekbarWrapperHide.observe(requireActivity()) {
            if (it && currentSeekBar != -1) {
                buttonTransitions[currentSeekBar].reverseTransition(transitionDuration)
                currentSeekBar = -1
            }
        }

        taskViewModel.retouchToolBrushRadius.value = seekbarsData[0].getFloatValue()
        taskViewModel.retouchToolIntensity.value = seekbarsData[1].getFloatValue()

        currentSeekBar = -1

        binding.retouchApply.isEnabled = false
        binding.retouchApply.alpha = 0.5F
    }

    private fun switchSeekbar(seekbarToShow: Int) {
        taskViewModel.seekbarFragment.showBar(buttons[seekbarToShow], stringIDs[seekbarToShow], liveData[seekbarToShow], seekbarsData, seekbarToShow)
        taskViewModel.seekbarWrapperHide.value = false

        if (currentSeekBar != -1) {
            buttonTransitions[currentSeekBar].reverseTransition(transitionDuration)
        }

        buttonTransitions[seekbarToShow].startTransition(transitionDuration)
        currentSeekBar = seekbarToShow
    }

    fun enableSaving() {
        binding.retouchApply.isEnabled = true
        binding.retouchApply.alpha = 1F
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRetouchToolBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = RetouchToolFragment()
    }
}