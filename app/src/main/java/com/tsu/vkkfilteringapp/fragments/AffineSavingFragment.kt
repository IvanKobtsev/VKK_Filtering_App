package com.tsu.vkkfilteringapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.tsu.vkkfilteringapp.TaskViewModel
import com.tsu.vkkfilteringapp.databinding.FragmentAffineSavingBinding
import com.tsu.vkkfilteringapp.graphics2d.Triangle2D

class AffineSavingFragment : Fragment() {

    private lateinit var binding: FragmentAffineSavingBinding
    private lateinit var taskViewModel: TaskViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]

        binding.affineCancelButton.setOnClickListener {
            taskViewModel.affineToolCancelPressed.value = true
        }

        binding.affineSaveButton.setOnClickListener {
            taskViewModel.hasUnsavedChanges.value = true
        }

        binding.cropChoice.setOnClickListener {
            taskViewModel.affineToolShowCropped.value = true
        }

        binding.fullChoice.setOnClickListener {
            taskViewModel.affineToolShowCropped.value = false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAffineSavingBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = AffineSavingFragment()
    }
}