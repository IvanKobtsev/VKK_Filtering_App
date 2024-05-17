package com.tsu.vkkfilteringapp

import android.R.attr.bitmap
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.tsu.vkkfilteringapp.databinding.ActivityImageBinding
import java.io.File
import java.io.FileOutputStream


class ImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var editedImage : Bitmap
    private var hasUnsavedChanges = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        PhotoPickerSheet().show(supportFragmentManager, "photoPicker")

//        binding.camera.setOnClickListener {
//            takePhoto(view)
//        }



        binding.saveButton.setOnClickListener {

        }

        binding.affineTransformTool.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.toolProps, AffineToolFragment.newInstance()).commit()
        }

        // Observers

        taskViewModel.image.observe(this) {
            editedImage = it
            binding.imageToEdit.setImageBitmap(editedImage)
            binding.imageToEdit.setAdjustViewBounds(true);
            binding.imageToEdit.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        taskViewModel.affineToolSelectedPoint.observe(this) {

        }

        taskViewModel.affineToolNeedToUpdate.observe(this) {

        }

    }
}
