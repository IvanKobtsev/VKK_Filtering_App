package com.tsu.vkkfilteringapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.tsu.vkkfilteringapp.SeekbarData
import com.tsu.vkkfilteringapp.TaskViewModel
import com.tsu.vkkfilteringapp.databinding.FragmentSeekBarBinding

class SeekbarFragment : Fragment() {

    private lateinit var binding: FragmentSeekBarBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var currentSeekbarData: SeekbarData
    private lateinit var currentLiveDataToEdit: MutableLiveData<Float>
    private var canChangeValue = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (canChangeValue) {
                    currentSeekbarData.currentValue = progress
                    currentLiveDataToEdit.value = currentSeekbarData.getFloatValue()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSeekBarBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun switchBar() {

    }

    fun showBar(textView: Button, stringID: Int, liveDataToEdit: MutableLiveData<Float>, seekbarDataList: MutableList<SeekbarData>, seekbarDataIndex: Int) {

        canChangeValue = false
        taskViewModel.textViewToWrite.value = textView
        taskViewModel.textViewStringID = stringID
        binding.seekBar.progress = seekbarDataList[seekbarDataIndex].currentValue
        binding.seekBar.max = seekbarDataList[seekbarDataIndex].getMax()
        binding.minValue.text = seekbarDataList[seekbarDataIndex].getTextMin()
        binding.maxValue.text = seekbarDataList[seekbarDataIndex].getTextMax()

        currentSeekbarData = seekbarDataList[seekbarDataIndex]
        currentLiveDataToEdit = liveDataToEdit
        canChangeValue = true
    }

    companion object {
        @JvmStatic
        fun newInstance() = SeekbarFragment()
    }
}