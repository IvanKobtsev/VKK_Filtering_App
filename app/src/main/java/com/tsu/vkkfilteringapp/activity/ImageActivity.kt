package com.tsu.vkkfilteringapp

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.tsu.vkkfilteringapp.databinding.ActivityImageBinding
import com.tsu.vkkfilteringapp.filters.AffineTransformation
import com.tsu.vkkfilteringapp.filters.UnsharpMasking
import com.tsu.vkkfilteringapp.fragments.AffineToolFragment
import com.tsu.vkkfilteringapp.fragments.FacesToolFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private lateinit var affineSavingFragment: AffineSavingFragment

    private lateinit var animFadeIn: Animation
    private lateinit var animFadeOut: Animation
    private lateinit var animAppear: Animation
    private lateinit var animDisappear: Animation

    // Motion events related
    private var lastX = 0F
    private var draggingCount = 0F
    private var imageViewBasePosition = 0F
    private var imageViewOriginalPosition = 0F
    private var askedAlready = false

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

        fragments = listOf(
            FacesToolFragment.newInstance(),
            FacesToolFragment.newInstance(),
            FacesToolFragment.newInstance(),
            FacesToolFragment.newInstance(),
            FacesToolFragment.newInstance(),
            FacesToolFragment.newInstance(),
            affineFragment)

        affineSavingFragment = AffineSavingFragment.newInstance()

        // Loading animation
        animFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        animFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        animAppear = AnimationUtils.loadAnimation(this, R.anim.appear)
        animDisappear = AnimationUtils.loadAnimation(this, R.anim.disappear)

        binding.progressBarOverlay.startAnimation(animDisappear)

        binding.toolProps.y += 500

        PhotoPickerSheet().show(supportFragmentManager, "photoPicker")

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

                binding.progressBarOverlay.startAnimation(animFadeIn)

                GlobalScope.launch(Dispatchers.Main) {

                    processImage()
                    supportFragmentManager.beginTransaction().replace(binding.toolProps.id, affineSavingFragment).commit()
                }
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

                    imageViewOriginalPosition = binding.imageToEdit.x
                }

                MotionEvent.ACTION_MOVE -> {

                    if (taskViewModel.selectedTool == -1 && taskViewModel.picturePicked) {

                        if (lastX > it.x) {
                            draggingCount += lastX - it.x
                            binding.imageToEdit.x -= lastX - it.x
                        }

                        lastX = it.x

                        if (draggingCount > 200) {
                            if (taskViewModel.hasUnsavedChanges.value == false) {
                                deleteImage()
                            }
                            else if (!askedAlready) {
                                askedAlready = true
                                askToAcceptDialog()
                            }
                        }


                    }
                }

                MotionEvent.ACTION_UP -> {
                    binding.imageToEdit.x = imageViewOriginalPosition
                }
            }

        }

    }

    private suspend fun processImage(){
        val value = GlobalScope.async {

            val result = withContext(Dispatchers.Default) {

                when (taskViewModel.selectedTool) {
                    0 -> {
                        // Rotation
                    }
                    1 -> {
                        // Filters
                    }
                    2 -> {
                        // Scaling
                    }
                    3 -> {
                        // Faces
                    }
                    4 -> {
                        // Retouch
                    }
                    5 -> {
                        // Masking
                    }
                    6 -> {
                        newBitmap = affineTransformation.transformBitmapByTriangles(editedImage,
                            taskViewModel.affineToolOrigTriangle,
                            taskViewModel.affineToolTransTriangle)
                    }
                    else -> {
                        newBitmap = editedImage
                    }
                }

            }
        }
        value.await()

        if (newBitmap.width == editedImage.width &&
            newBitmap.height == editedImage.height) {
            binding.imageToEdit.alpha = 1F
            Toast.makeText(this.baseContext, R.string.error_too_large_image, Toast.LENGTH_LONG).show()
        }
        else {
            binding.imageToEdit.alpha = 1F
            binding.imageToEdit.setImageBitmap(newBitmap)
        }

        binding.progressBarOverlay.startAnimation(animFadeOut)
    }

    override fun onResume() {
        super.onResume()

        if (imageViewBasePosition == 0F) {
            imageViewBasePosition = binding.imageToEdit.x
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
                supportFragmentManager.beginTransaction().replace(binding.toolProps.id, fragments[newActiveFragment]).commit()
                taskViewModel.selectedTool = newActiveFragment
            }
            else {
                binding.toolProps.y -= 500
                buttonTransitions[newActiveFragment].startTransition(transitionDuration)
                supportFragmentManager.beginTransaction().replace(binding.toolProps.id, fragments[newActiveFragment]).commit()
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
//                imageName = editText.text.toString()
                saveMediaToStorage()
                Toast.makeText(this.context, "Теперь ваша картинка в галерее!", Toast.LENGTH_SHORT).show()
            }
            setNegativeButton("Отменить") { _, _ ->
                // Do nothing
            }
//            setView(dialogLayout)
            show()
        }

    }

    private fun callExitDialog() {
        val builder = AlertDialog.Builder(this)
        with (builder) {
            setMessage(R.string.there_is_no_autosave)
            setTitle(R.string.accept_exit)
            setPositiveButton(R.string.exit) { _, _ ->

                finish()
            }
            setNegativeButton(R.string.cancel) { _, _ ->
                // Do nothing
            }
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun askToAcceptDialog() {

        val builder = AlertDialog.Builder(this)
        with (builder) {
            setMessage(R.string.unsaved_changes)
            setTitle(R.string.accept_deletion)
            setPositiveButton(R.string.delete) { _, _ ->
                deleteImage()
            }
            setNegativeButton(R.string.leave) { _, _ ->
                askedAlready = false
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

    private fun deleteImage() {

        taskViewModel.hasUnsavedChanges.value = false
        taskViewModel.picturePicked = false
        askedAlready = false

        binding.imageToEdit.setImageResource(R.drawable.pick_photo_warning_screen)
        binding.imageToEdit.scaleType = ImageView.ScaleType.CENTER_CROP

        imageViewOriginalPosition = imageViewBasePosition
    }
}
