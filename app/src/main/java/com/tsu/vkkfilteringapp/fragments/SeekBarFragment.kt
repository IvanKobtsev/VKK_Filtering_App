package com.tsu.vkkfilteringapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.tsu.vkkfilteringapp.TaskViewModel
import com.tsu.vkkfilteringapp.databinding.FragmentSeekBarBinding

class SeekBarFragment : Fragment() {

    private lateinit var binding: FragmentSeekBarBinding
    private lateinit var taskViewModel: TaskViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSeekBarBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun switchBar() {

    }

    fun showBar(textView: TextView, currentValue: Int, minValue: Int, maxValue: Int) {

        taskViewModel.textViewToWrite.value = textView


    }

    companion object {
        @JvmStatic
        fun newInstance() = SeekBarFragment()
    }
}