package com.tsu.vkkfilteringapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.tsu.vkkfilteringapp.R
import com.tsu.vkkfilteringapp.TaskViewModel
import com.tsu.vkkfilteringapp.databinding.FragmentAffineToolBinding
import com.tsu.vkkfilteringapp.databinding.FragmentRetouchToolBinding

class RetouchToolFragment : Fragment() {

    private lateinit var binding: FragmentRetouchToolBinding
    private lateinit var taskViewModel: TaskViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]
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