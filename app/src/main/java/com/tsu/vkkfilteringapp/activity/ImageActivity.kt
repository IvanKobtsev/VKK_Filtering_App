package com.tsu.vkkfilteringapp.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.tsu.vkkfilteringapp.AffineToolFragment
import com.tsu.vkkfilteringapp.FacesToolFragment
import com.tsu.vkkfilteringapp.PhotoPickerSheet
import com.tsu.vkkfilteringapp.R
import com.tsu.vkkfilteringapp.TaskViewModel
import com.tsu.vkkfilteringapp.databinding.ActivityImageBinding
import com.tsu.vkkfilteringapp.filters.UnsharpMasking


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

        binding.maskingTool.setOnClickListener {
            val blur = UnsharpMasking(editedImage, 1.0, 1)
            binding.imageToEdit.setImageBitmap(blur.newImg)
            editedImage = blur.newImg
        }

        binding.saveButton.setOnClickListener {

        }

        binding.faceRecognitionTool.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(
                R.id.toolProps,
                FacesToolFragment.newInstance()
            ).commit()
        }

        binding.affineTransformTool.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(
                R.id.toolProps,
                AffineToolFragment.newInstance()
            ).commit()
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
