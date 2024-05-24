package com.tsu.vkkfilteringapp.fragments

import android.annotation.SuppressLint
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
import com.tsu.vkkfilteringapp.databinding.FragmentMaskingToolBinding

class MaskingToolFragment : Fragment() {

    private lateinit var binding: FragmentMaskingToolBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var buttonTransitions: List<TransitionDrawable>
    private lateinit var buttons: List<Button>
    private var currentSeekBar = -1
    private val transitionDuration = 300

    private val stringIDs = listOf(
        R.string.masking_core_radius,
        R.string.masking_amount
    )

    // Seekbars
    private val seekbarsData = listOf(
        SeekbarData(0, 1, 5),
        SeekbarData(0, 1, 20, 10F)
    )

    // Live-data values
    private lateinit var liveData: List<MutableLiveData<Float>>

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]
        liveData = listOf(
            taskViewModel.maskToolCoreRadiusValue,
            taskViewModel.maskToolAmountValue
        )

        // Buttons
        buttons = listOf(
            binding.coreRadiusCall,
            binding.amountCall
        )

        // Transitions setting
        buttonTransitions = listOf(
            binding.coreRadiusCall.background as TransitionDrawable,
            binding.amountCall.background as TransitionDrawable)

        binding.affineApply.setOnClickListener {
            taskViewModel.maskToolNeedToUpdate.value = true
        }

        binding.coreRadiusCall.setOnClickListener {
            switchSeekbar(0)
        }

        binding.amountCall.setOnClickListener {
            switchSeekbar(1)
            switchSeekbar(1)
        }

        taskViewModel.maskToolCoreRadiusValue.observe(requireActivity()) {
            binding.coreRadiusCall.text = resources.getString(R.string.masking_core_radius, it.toString())
        }

        taskViewModel.maskToolAmountValue.observe(requireActivity()) {
            binding.amountCall.text = resources.getString(R.string.masking_amount, it.toString())
        }

        taskViewModel.maskToolCoreRadiusValue.value = seekbarsData[0].getFloatValue()
        taskViewModel.maskToolAmountValue.value = seekbarsData[1].getFloatValue()
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMaskingToolBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = MaskingToolFragment()
    }
}