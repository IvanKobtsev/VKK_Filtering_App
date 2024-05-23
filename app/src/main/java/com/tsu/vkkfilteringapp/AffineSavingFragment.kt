package com.tsu.vkkfilteringapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.tsu.vkkfilteringapp.databinding.FragmentAffineSavingBinding
import com.tsu.vkkfilteringapp.databinding.FragmentFacesToolBinding

class AffineSavingFragment : Fragment() {

    private lateinit var binding: FragmentAffineSavingBinding
    private lateinit var taskViewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]
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