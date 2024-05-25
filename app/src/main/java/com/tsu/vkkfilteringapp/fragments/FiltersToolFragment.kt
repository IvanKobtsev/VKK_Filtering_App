package com.tsu.vkkfilteringapp.fragments

import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.tsu.vkkfilteringapp.R
import com.tsu.vkkfilteringapp.TaskViewModel
import com.tsu.vkkfilteringapp.databinding.FragmentAffineToolBinding
import com.tsu.vkkfilteringapp.databinding.FragmentFiltersToolBinding

class FiltersToolFragment : Fragment() {

    private lateinit var binding: FragmentFiltersToolBinding
    private lateinit var taskViewModel: TaskViewModel

    private lateinit var buttonTransitions: List<TransitionDrawable>
    private lateinit var buttons: List<Button>
    private var currentFilter = -1
    private val transitionDuration = 300

    private val stringIDs = listOf(
        R.string.filters_contrast_plus50,
        R.string.filters_contrast_minus50,
        R.string.filters_saturation_plus50,
        R.string.filters_saturation_minus50,
        R.string.filters_inversion,
        R.string.filters_common_blur_light,
        R.string.filters_common_blur_medium,
        R.string.filters_common_blur_strong,
        R.string.filters_gaussian_blur_light,
        R.string.filters_gaussian_blur_medium,
        R.string.filters_gaussian_blur_strong,
        R.string.filters_mosaic_tiny,
        R.string.filters_mosaic_medium,
        R.string.filters_mosaic_big
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]

        buttons = listOf(
            binding.filter0Call,
            binding.filter1Call,
            binding.filter2Call,
            binding.filter3Call,
            binding.filter4Call,
            binding.filter5Call,
            binding.filter6Call,
            binding.filter7Call,
            binding.filter8Call,
            binding.filter9Call,
            binding.filter10Call,
            binding.filter11Call,
            binding.filter12Call,
            binding.filter13Call
        )

        buttonTransitions = listOf(
            binding.filter0Call.background as TransitionDrawable,
            binding.filter1Call.background as TransitionDrawable,
            binding.filter2Call.background as TransitionDrawable,
            binding.filter3Call.background as TransitionDrawable,
            binding.filter4Call.background as TransitionDrawable,
            binding.filter5Call.background as TransitionDrawable,
            binding.filter6Call.background as TransitionDrawable,
            binding.filter7Call.background as TransitionDrawable,
            binding.filter8Call.background as TransitionDrawable,
            binding.filter9Call.background as TransitionDrawable,
            binding.filter10Call.background as TransitionDrawable,
            binding.filter11Call.background as TransitionDrawable,
            binding.filter12Call.background as TransitionDrawable,
            binding.filter13Call.background as TransitionDrawable
        )

        binding.filter0Call.setOnClickListener {
            switchFilter(0)
        }

        binding.filter1Call.setOnClickListener {
            switchFilter(1)
        }

        binding.filter2Call.setOnClickListener {
            switchFilter(2)
        }

        binding.filter3Call.setOnClickListener {
            switchFilter(3)
        }

        binding.filter4Call.setOnClickListener {
            switchFilter(4)
        }

        binding.filter5Call.setOnClickListener {
            switchFilter(5)
        }

        binding.filter6Call.setOnClickListener {
            switchFilter(6)
        }

        binding.filter7Call.setOnClickListener {
            switchFilter(7)
        }

        binding.filter8Call.setOnClickListener {
            switchFilter(8)
        }

        binding.filter9Call.setOnClickListener {
            switchFilter(9)
        }

        binding.filter10Call.setOnClickListener {
            switchFilter(10)
        }

        binding.filter11Call.setOnClickListener {
            switchFilter(11)
        }

        binding.filter12Call.setOnClickListener {
            switchFilter(12)
        }

        binding.filter13Call.setOnClickListener {
            switchFilter(13)
        }
    }

    private fun switchFilter(filterToApply: Int) {

        if (currentFilter != -1) {
            buttonTransitions[currentFilter].reverseTransition(transitionDuration)
        }

        buttonTransitions[filterToApply].startTransition(transitionDuration)
        currentFilter = filterToApply
        taskViewModel.filtersToolSelected = filterToApply
        taskViewModel.filtersToolNeedToUpdate.value = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFiltersToolBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = FiltersToolFragment()
    }
}