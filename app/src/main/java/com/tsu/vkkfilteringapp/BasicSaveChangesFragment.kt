package com.tsu.vkkfilteringapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.tsu.vkkfilteringapp.databinding.FragmentAffineSavingBinding
import com.tsu.vkkfilteringapp.databinding.FragmentBasicSaveChangesBinding

class BasicSaveChangesFragment : Fragment() {

    private lateinit var binding: FragmentBasicSaveChangesBinding
    private lateinit var taskViewModel: TaskViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]

        binding.affineSaveButton.setOnClickListener {
            taskViewModel.acceptBasicSaving.value = true
        }

        binding.affineCancelButton.setOnClickListener {
            taskViewModel.cancelBasicSaving.value = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBasicSaveChangesBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = BasicSaveChangesFragment()
    }
}