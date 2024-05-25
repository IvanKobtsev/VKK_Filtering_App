package com.tsu.vkkfilteringapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.ablanco.imageprovider.ImageProvider
import com.ablanco.imageprovider.ImageSource
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tsu.vkkfilteringapp.databinding.FragmentPhotoPickerBinding
import java.io.File

class PhotoPickerSheet : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentPhotoPickerBinding
    private lateinit var taskViewModel: TaskViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        taskViewModel = ViewModelProvider(activity)[TaskViewModel::class.java]

        binding.cameraPick.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED) {

                ImageProvider(activity).getImage(ImageSource.CAMERA){ bitmap ->
                    if (bitmap != null) {
                        taskViewModel.image.value = bitmap
                    }
                }
                dismiss()

            }
        }

        binding.galleryPick.setOnClickListener {
            ImageProvider(activity).getImage(ImageSource.GALLERY){ bitmap ->
                if (bitmap != null) {
                    taskViewModel.image.value = bitmap
                }

            }
            dismiss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPhotoPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

}