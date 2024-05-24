package com.tsu.vkkfilteringapp.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.SeekBar
import androidx.lifecycle.ViewModelProvider
import com.tsu.vkkfilteringapp.R
import com.tsu.vkkfilteringapp.TaskViewModel
import com.tsu.vkkfilteringapp.databinding.FragmentAffineToolBinding
import com.tsu.vkkfilteringapp.databinding.FragmentMaskingToolBinding

class MaskingToolFragment : Fragment() {

    private lateinit var binding: FragmentMaskingToolBinding
    private lateinit var taskViewModel: TaskViewModel

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]



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