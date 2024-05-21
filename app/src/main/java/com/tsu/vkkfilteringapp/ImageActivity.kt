package com.tsu.vkkfilteringapp

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.tsu.vkkfilteringapp.databinding.ActivityImageBinding
import com.tsu.vkkfilteringapp.filters.AffineTransformation
import com.tsu.vkkfilteringapp.filters.UnsharpMasking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date


class ImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var editedImage : Bitmap
    private lateinit var newBitmap: Bitmap
    private lateinit var imageName: String

    private lateinit var affineFragment: AffineToolFragment
    private lateinit var buttonTransitions: List<TransitionDrawable>
    private lateinit var fragments: List<Fragment>

    // Companions
    private var affineTransformation = AffineTransformation.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        binding.imageToEdit.init(this, assets)

        // Transitions setting
        buttonTransitions = listOf(
            binding.rotationTool.background as TransitionDrawable,
            binding.filtersTool.background as TransitionDrawable,
            binding.scalingTool.background as TransitionDrawable,
            binding.faceRecognitionTool.background as TransitionDrawable,
            binding.retouchTool.background as TransitionDrawable,
            binding.maskingTool.background as TransitionDrawable,
            binding.affineTransformTool.background as TransitionDrawable)
        // Fragments setting
        affineFragment = AffineToolFragment.newInstance()

        fragments = listOf(FacesToolFragment.newInstance(),
        FacesToolFragment.newInstance(),
        FacesToolFragment.newInstance(),
        FacesToolFragment.newInstance(),
        FacesToolFragment.newInstance(),
        FacesToolFragment.newInstance(),
        affineFragment)


        binding.toolProps.y += 500

        PhotoPickerSheet().show(supportFragmentManager, "photoPicker")

//        binding.camera.setOnClickListener {
//            takePhoto(view)
//        }

        binding.maskingTool.setOnClickListener {
            val blur = UnsharpMasking(editedImage, 1.0, 1)
            binding.imageToEdit.setImageBitmap(blur.newImg)
            editedImage = blur.newImg
        }

        binding.exitButton.setOnClickListener {
            callExitDialog()
        }

        binding.saveButton.setOnClickListener {
            if (this::editedImage.isInitialized) {
                callSaveDialog()
            }
            else {
                Toast.makeText(this, "Вы не загрузили картинку!", Toast.LENGTH_SHORT).show()
            }
        }

        // Tool buttons' listeners

        binding.rotationTool.setOnClickListener {
            showToolProps(0)
        }

        binding.filtersTool.setOnClickListener {
            showToolProps(1)
        }

        binding.scalingTool.setOnClickListener {
            showToolProps(2)
        }

        binding.faceRecognitionTool.setOnClickListener {
            showToolProps(3)
        }

        binding.retouchTool.setOnClickListener {
            showToolProps(4)
        }

        binding.maskingTool.setOnClickListener {
            showToolProps(5)
        }

        binding.affineTransformTool.setOnClickListener {
            showToolProps(6)
        }

        // Observers

        taskViewModel.image.observe(this) {
            editedImage = it
            binding.imageToEdit.setImageBitmap(editedImage)
            binding.imageToEdit.setAdjustViewBounds(true)
            binding.imageToEdit.setScaleType(ImageView.ScaleType.FIT_XY)
            taskViewModel.picturePicked = true
        }

        taskViewModel.affineToolSelectedPoint.observe(this) {

        }

        taskViewModel.affineToolNeedToUpdate.observe(this) {
            if (it) {

                // Show loading screen...

//                val mainJob = CoroutineScope(Dispatchers.IO).launch {

                    newBitmap = affineTransformation.transformBitmapByTriangles(editedImage,
                        taskViewModel.affineToolOrigTriangle,
                        taskViewModel.affineToolTransTriangle)
//                }

                binding.imageToEdit.setImageBitmap(newBitmap)

            }
        }

        taskViewModel.motionEvent.observe(this) {

            when (it.action) {
                MotionEvent.ACTION_DOWN -> {

                    if (!taskViewModel.picturePicked) {
                        PhotoPickerSheet().show(supportFragmentManager, "photoPicker")
                    }
                    else {
                        if (taskViewModel.selectedTool == 6) {
                            affineFragment.setPoint(it.x, it.y)
                            checkForNullPoints()
                        }

                        binding.imageToEdit.invalidate()
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    Log.i("smth", it.x.toString() + "  " + it.y.toString())
                }

                MotionEvent.ACTION_UP -> {

                }
            }

        }

    }

    private val transitionDuration = 200
    private fun showToolProps(newActiveFragment: Int) {
        if (taskViewModel.picturePicked) {
            if (newActiveFragment == taskViewModel.selectedTool) {
                binding.toolProps.y += 500
                buttonTransitions[taskViewModel.selectedTool].reverseTransition(transitionDuration)
                taskViewModel.selectedTool = -1
            }
            else if (taskViewModel.selectedTool != -1) {
                buttonTransitions[taskViewModel.selectedTool].reverseTransition(transitionDuration)
                buttonTransitions[newActiveFragment].startTransition(transitionDuration)
                supportFragmentManager.beginTransaction().replace(R.id.toolProps, fragments[newActiveFragment]).commit()
                taskViewModel.selectedTool = newActiveFragment
            }
            else {
                binding.toolProps.y -= 500
                buttonTransitions[newActiveFragment].startTransition(transitionDuration)
                supportFragmentManager.beginTransaction().replace(R.id.toolProps, fragments[newActiveFragment]).commit()
                taskViewModel.selectedTool = newActiveFragment
            }
            binding.imageToEdit.invalidate()
        }
    }

    private fun checkForNullPoints() {
        for (vi in 0..2) {
            if (taskViewModel.affineToolOrigTriangle.vertices[vi].x == 0F && taskViewModel.affineToolOrigTriangle.vertices[vi].y == 0F) {
                return
            }
        }

        for (vi in 0..2) {
            if (taskViewModel.affineToolTransTriangle.vertices[vi].x == 0F && taskViewModel.affineToolTransTriangle.vertices[vi].y == 0F) {
                return
            }
        }

        taskViewModel.affineToolCanApply.value = true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            callExitDialog()
            return true
        }
        else {
            return super.onKeyDown(keyCode, event)
        }
    }

    private fun callSaveDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogLayout = layoutInflater.inflate(R.layout.file_name_dialog, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.fileNameEditor)

        with (builder) {
            setTitle("Введите имя вашего файла")
            setPositiveButton("Готово") { _, _ ->
                imageName = editText.text.toString()
                saveMediaToStorage()
                Toast.makeText(this.context, "Теперь ваша картинка в галерее!", Toast.LENGTH_SHORT).show()
            }
            setNegativeButton("Отменить") { _, _ ->
                // Do nothing
            }
            setView(dialogLayout)
            show()
        }

    }

    private fun callExitDialog() {
        val builder = AlertDialog.Builder(this)
        with (builder) {
            setMessage("Ваши изменения не сохраняются автоматически!")
            setTitle("Вы точно хотите выйти?")
            setPositiveButton("Выйти") { _, _ ->

                finish()
            }
            setNegativeButton("Останусь") { _, _ ->
                // Do nothing
            }
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun saveMediaToStorage() {

        val filename: String

        if (this::imageName.isInitialized && imageName != "") {
            filename = "${imageName}.jpg"
        }
        else {
            val currentDate = Date()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
            val formattedDate = dateFormat.format(currentDate)
            filename = "${formattedDate}.jpg"
        }

        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.contentResolver?.also { resolver ->

                val contentValues = ContentValues().apply {

                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            editedImage.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }
}
