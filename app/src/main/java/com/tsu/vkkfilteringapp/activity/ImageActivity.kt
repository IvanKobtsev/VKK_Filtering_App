package com.tsu.vkkfilteringapp.activity

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.tsu.vkkfilteringapp.BasicSaveChangesFragment
import com.tsu.vkkfilteringapp.FaceDetection
import com.tsu.vkkfilteringapp.PhotoPickerSheet
import com.tsu.vkkfilteringapp.R
import com.tsu.vkkfilteringapp.RetouchingBrush
import com.tsu.vkkfilteringapp.TaskViewModel
import com.tsu.vkkfilteringapp.databinding.ActivityImageBinding
import com.tsu.vkkfilteringapp.filters.AffineTransformation
import com.tsu.vkkfilteringapp.filters.LightweightFilters
import com.tsu.vkkfilteringapp.filters.RotationFilter
import com.tsu.vkkfilteringapp.filters.UnsharpMasking
import com.tsu.vkkfilteringapp.fragments.AffineSavingFragment
import com.tsu.vkkfilteringapp.fragments.AffineToolFragment
import com.tsu.vkkfilteringapp.fragments.FacesToolFragment
import com.tsu.vkkfilteringapp.fragments.FiltersToolFragment
import com.tsu.vkkfilteringapp.fragments.MaskingToolFragment
import com.tsu.vkkfilteringapp.fragments.RetouchToolFragment
import com.tsu.vkkfilteringapp.fragments.RotationToolFragment
import com.tsu.vkkfilteringapp.fragments.ScalingToolFragment
import com.tsu.vkkfilteringapp.fragments.SeekbarFragment
import com.tsu.vkkfilteringapp.graphics2d.Triangle2D
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.core.Point
import pl.droidsonroids.gif.InputSource.AssetSource
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.PI


class ImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var editedImage : Bitmap
    private lateinit var newBitmap: Bitmap
    private lateinit var imageName: String

    // Filters-related
    private lateinit var filters: LightweightFilters

    // Retouch-related
    private lateinit var retouchFragment: RetouchToolFragment

    // Masking-related
    private lateinit var maskingTool: UnsharpMasking
    private lateinit var retouchTool: RetouchingBrush

    // Affine-related
    private lateinit var affineFragment: AffineToolFragment
    private lateinit var buttonTransitions: List<TransitionDrawable>
    private lateinit var fragments: List<Fragment>
    private lateinit var affineSavingFragment: AffineSavingFragment

    private lateinit var basicSavingFragment: BasicSaveChangesFragment

    private lateinit var seekBarFragment: SeekbarFragment

    private lateinit var animFadeIn: Animation
    private lateinit var animFadeOut: Animation
    private lateinit var animAppear: Animation
    private lateinit var animDisappear: Animation

    // Toast cool-downs
    private var canShowNoImageToast = true
    private var noImageCoolDownTimer = object: CountDownTimer(2000, 2000) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() { canShowNoImageToast = true }
    }
    private var canShowNeedToApplyToast = true
    private var needToApplyCoolDownTimer = object: CountDownTimer(2000, 2000) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() { canShowNeedToApplyToast = true }
    }
    private var canShowTooLargeImageToast = true
    private var tooLargeImageCoolDownTimer = object: CountDownTimer(3500, 3500) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() { canShowTooLargeImageToast = true }
    }

    // Motion events related
    private var lastX = 0F
    private var draggingCount = 0F
    private var imageViewBasePosition = 0F
    private var imageViewOriginalPosition = 0F
    private var askedAlready = false

    private val faceDetection = FaceDetection.newInstance()
    private val affineTransformation = AffineTransformation.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        binding.imageToEdit.init(this, assets)

        // Setting filters
        filters = LightweightFilters()
        maskingTool = UnsharpMasking()
        retouchTool = RetouchingBrush()

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
        retouchFragment = RetouchToolFragment.newInstance()

        fragments = listOf(
            RotationToolFragment.newInstance(),
            FiltersToolFragment.newInstance(),
            ScalingToolFragment.newInstance(),
            FacesToolFragment.newInstance(),
            retouchFragment,
            MaskingToolFragment.newInstance(),
            affineFragment)

        seekBarFragment = SeekbarFragment.newInstance()
        taskViewModel.seekbarFragment = seekBarFragment

        affineSavingFragment = AffineSavingFragment.newInstance()
        basicSavingFragment = BasicSaveChangesFragment.newInstance()

        // Loading animation
        animFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        animFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        animAppear = AnimationUtils.loadAnimation(this, R.anim.appear)
        animDisappear = AnimationUtils.loadAnimation(this, R.anim.disappear)

        binding.progressBarOverlay.startAnimation(animDisappear)

        binding.toolProps.y += 500

        PhotoPickerSheet().show(supportFragmentManager, "photoPicker")

        binding.exitButton.setOnClickListener {
            callExitDialog()
        }

        binding.saveButton.setOnClickListener {
            if (taskViewModel.picturePicked && !taskViewModel.isApplyingChanges) {
                callSaveDialog()
            }
            else if (taskViewModel.isApplyingChanges) {
                needToApplyWarning()
            }
            else {
                noImageWarning()
            }
        }

        // Tool buttons' listeners

        binding.rotationTool.setOnClickListener {
            switchToolProps(0)
        }

        binding.filtersTool.setOnClickListener {
            switchToolProps(1)
        }

        binding.scalingTool.setOnClickListener {
            switchToolProps(2)
        }

        binding.faceRecognitionTool.setOnClickListener {
            switchToolProps(3)
        }

        binding.retouchTool.setOnClickListener {
            switchToolProps(4)
        }

        binding.maskingTool.setOnClickListener {
            switchToolProps(5)
        }

        binding.affineTransformTool.setOnClickListener {
            switchToolProps(6)
        }

        // Observers

        taskViewModel.image.observe(this) {
            editedImage = it
            newBitmap = it
            binding.imageToEdit.setImageBitmap(editedImage)
            binding.imageToEdit.setAdjustViewBounds(true)
            binding.imageToEdit.setScaleType(ImageView.ScaleType.FIT_XY)
            taskViewModel.picturePicked = true
        }

        taskViewModel.seekbarWrapperHide.observe(this) {
            if (it && !taskViewModel.isSeekbarWrapperActuallyHidden) {
                binding.seekBarFragmentWrapper.y += 500
                taskViewModel.isSeekbarWrapperActuallyHidden = true
            }
            else if (!it && taskViewModel.isSeekbarWrapperActuallyHidden) {
                binding.seekBarFragmentWrapper.y -= 500
                taskViewModel.isSeekbarWrapperActuallyHidden = false
            }
        }

        taskViewModel.acceptBasicSaving.observe(this) {
            if (it) {
                editedImage = newBitmap
                unlockToolScrollAndSave()
                switchToolProps(-1)
            }
        }

        taskViewModel.cancelBasicSaving.observe(this) {
            if (it) {
                binding.imageToEdit.setImageBitmap(editedImage)
                unlockToolScrollAndSave()
                switchToolProps(-1)
            }
        }

        // Rotation observers
        taskViewModel.rotationToolNeedToUpdate.observe(this) {
            if (it) {

                lockUI()
                binding.progressBarOverlay.startAnimation(animFadeIn)

                GlobalScope.launch(Dispatchers.Main) {

                    processImage()

                    binding.imageToEdit.setImageBitmap(newBitmap)

                    lockToolScrollAndSave()
                    supportFragmentManager.beginTransaction().replace(binding.toolProps.id, affineSavingFragment).commit()
                }
            }
        }

        // Filters observers
        taskViewModel.filtersToolNeedToUpdate.observe(this) {
            if (it) {

                lockUI()
                binding.progressBarOverlay.startAnimation(animFadeIn)

                GlobalScope.launch(Dispatchers.Main) {

                    processImage()

                    binding.imageToEdit.setImageBitmap(newBitmap)

                    lockToolScrollAndSave()
                    supportFragmentManager.beginTransaction().replace(binding.toolProps.id, basicSavingFragment).commit()
                }
            }
        }

        // Scaling observers
        taskViewModel.scalingToolNeedToUpdate.observe(this) {
            if (it) {

                lockUI()
                binding.progressBarOverlay.startAnimation(animFadeIn)

                GlobalScope.launch(Dispatchers.Main) {

                    processImage()

                    binding.imageToEdit.setImageBitmap(newBitmap)

                    lockToolScrollAndSave()
                    supportFragmentManager.beginTransaction().replace(binding.toolProps.id, basicSavingFragment).commit()
                }
            }
        }

        // Faces observers
        taskViewModel.facesToolNeedToUpdate.observe(this) {
            if (it) {

                lockUI()
                binding.progressBarOverlay.startAnimation(animFadeIn)

                GlobalScope.launch(Dispatchers.Main) {

                    processImage()

                    binding.imageToEdit.setImageBitmap(newBitmap)

                    lockToolScrollAndSave()
                    supportFragmentManager.beginTransaction().replace(binding.toolProps.id, basicSavingFragment).commit()
                }
            }
        }

        // Retouch observers
        taskViewModel.retouchToolNeedToUpdate.observe(this) {
            if (it) {
                editedImage = newBitmap
            }
        }

        // Masking observers
        taskViewModel.maskToolNeedToUpdate.observe(this) {
            if (it) {

                lockUI()
                binding.progressBarOverlay.startAnimation(animFadeIn)

                GlobalScope.launch(Dispatchers.Main) {

                    processImage()

                    binding.imageToEdit.setImageBitmap(newBitmap)

                    lockToolScrollAndSave()
                    supportFragmentManager.beginTransaction().replace(binding.toolProps.id, basicSavingFragment).commit()
                }
            }
        }

        // Affine Observers
        taskViewModel.affineToolCancelPressed.observe(this) {
            if (it) {
                unlockToolScrollAndSave()
                revertChanges()
            }
        }

        taskViewModel.tooLargeImage.observe(this) {
            if (it) {
                tooLargeImageWarning()
            }
        }

        taskViewModel.affineToolNeedToUpdate.observe(this) {
            if (it) {

                lockUI()
                binding.progressBarOverlay.startAnimation(animFadeIn)

                GlobalScope.launch(Dispatchers.Main) {

                    processImage()

                    if (newBitmap.width == editedImage.width &&
                        newBitmap.height == editedImage.height) {
                        binding.imageToEdit.alpha = 1F
                        taskViewModel.tooLargeImage.value = true
                    }
                    else {
                        binding.imageToEdit.alpha = 1F
                        binding.imageToEdit.setImageBitmap(newBitmap)

                        lockToolScrollAndSave()
                        taskViewModel.canDrawOnImageView = false
                        binding.imageToEdit.invalidate()

                        if (taskViewModel.affineToolShowCropped.value!!) {
                            binding.imageToEdit.setImageBitmap(affineTransformation.getCropDemo(newBitmap, taskViewModel))
                        }

                        supportFragmentManager.beginTransaction().replace(binding.toolProps.id, affineSavingFragment).commit()
                    }
                }
            }
        }

        // Base observers

        taskViewModel.motionEvent.observe(this) {

            when (it.action) {
                MotionEvent.ACTION_DOWN -> {

                    if (!taskViewModel.picturePicked) {
                        PhotoPickerSheet().show(supportFragmentManager, "photoPicker")
                    }
                    else {
                        when (taskViewModel.selectedTool) {
                            4 -> {
                                retouchFragment.enableSaving()
                            }
                            6 -> {
                                affineFragment.setPoint(it.x, it.y)
                                checkForNullPoints()
                            }
                        }

                        binding.imageToEdit.invalidate()
                    }

                    imageViewOriginalPosition = binding.imageToEdit.x
                }

                MotionEvent.ACTION_MOVE -> {
                    if (taskViewModel.picturePicked) {
                        when (taskViewModel.selectedTool) {
                            -1 -> {
                                if (lastX > it.x) {
                                    draggingCount += lastX - it.x
                                    binding.imageToEdit.x -= lastX - it.x
                                }

                                lastX = it.x

                                if (draggingCount > 200) {
                                    if (taskViewModel.hasUnsavedChanges.value == false) {
                                        deleteImage()
                                        draggingCount = 0F
                                    }
                                    else if (!askedAlready) {
                                        askedAlready = true
                                        askToAcceptDialog()
                                    }
                                }
                            }
                            4 -> {
                                newBitmap = RetouchingBrush.retouching(it.x.toInt(), it.y.toInt(),
                                    taskViewModel.retouchToolBrushRadius.value!!.toInt(),
                                    newBitmap,
                                    taskViewModel.retouchToolIntensity.value!!.toDouble())
                                binding.imageToEdit.setImageBitmap(newBitmap)
                            }
                        }
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    binding.imageToEdit.x = imageViewOriginalPosition
                }
            }
        }

        taskViewModel.hasUnsavedChanges.observe(this) {
            if (it) {
                when (taskViewModel.selectedTool) {
                    0 -> {
                        editedImage = if (taskViewModel.affineToolShowCropped.value!!) {
                            affineTransformation.getCroppedImage(newBitmap, taskViewModel)
                        } else {
                            newBitmap
                        }

                        binding.imageToEdit.setImageBitmap(editedImage)
                        unlockToolScrollAndSave()
                        switchToolProps(-1)
                    }
                    6 -> {
                        editedImage = if (taskViewModel.affineToolShowCropped.value!!) {
                            affineTransformation.getCroppedImage(newBitmap, taskViewModel)
                        } else {
                            newBitmap
                        }

                        binding.imageToEdit.setImageBitmap(editedImage)
                        taskViewModel.canDrawOnImageView = true
                        taskViewModel.affineToolOrigTriangle = Triangle2D()
                        taskViewModel.affineToolTransTriangle = Triangle2D()
                        taskViewModel.affineToolCanApply.value = false

                        unlockToolScrollAndSave()

                        switchToolProps(-1)
                    }
                }
            }
        }

        taskViewModel.affineToolShowCropped.observe(this) {
            if (taskViewModel.picturePicked) {
                if (it) {
                    binding.imageToEdit.setImageBitmap(affineTransformation.getCropDemo(newBitmap, taskViewModel))
                }
                else {
                    binding.imageToEdit.setImageBitmap(newBitmap)
                }
            }
        }

        // Seekbar wrapper setting
        taskViewModel.seekbarWrapperHide.value = true
        supportFragmentManager.beginTransaction().replace(binding.seekBarFragmentWrapper.id, seekBarFragment).commit()

    }

    private suspend fun processImage(){
        val value = GlobalScope.async {

            val result = withContext(Dispatchers.Default) {

                when (taskViewModel.selectedTool) {
                    0 -> {
                        newBitmap = affineTransformation.rotateImage(
                            editedImage,
                            taskViewModel.rotationToolAngle.value!! * (PI.toFloat() / 180),
                            taskViewModel)
                    }
                    1 -> {
                        when (taskViewModel.filtersToolSelected) {
                            0 -> {
                                newBitmap = filters.contrast(editedImage, 125)
                            }
                            1 -> {
                                newBitmap = filters.contrast(editedImage, -125)
                            }
                            2 -> {
                                newBitmap = filters.saturation(editedImage, 125)
                            }
                            3 -> {
                                newBitmap = filters.saturation(editedImage, -125)
                            }
                            4 -> {
                                newBitmap = filters.inversion(editedImage)
                            }
                            5 -> {
                                newBitmap = filters.commonBlur(editedImage, 5, 0.5)
                            }
                            6 -> {
                                newBitmap = filters.commonBlur(editedImage, 15, 0.75)
                            }
                            7 -> {
                                newBitmap = filters.commonBlur(editedImage, 25, 1.0)
                            }
                            8 -> {
                                newBitmap = filters.blurGaussian(editedImage, 1, 1.0, 0.0)
                            }
                            9 -> {
                                newBitmap = filters.blurGaussian(editedImage, 5, 1.0, 0.5)
                            }
                            10 -> {
                                newBitmap = filters.blurGaussian(editedImage, 10, 1.0, 1.0)
                            }
                            11 -> {
                                newBitmap = filters.mosaicFilter(editedImage, 5, 1.0)
                            }
                            12 -> {
                                newBitmap = filters.mosaicFilter(editedImage, 10, 1.0)
                            }
                            13 -> {
                                newBitmap = filters.mosaicFilter(editedImage, 25, 1.0)
                            }
                        }
                    }
                    2 -> {
                        newBitmap = affineTransformation.scaleImage(
                            editedImage,
                            taskViewModel.scalingToolScale.value!!,
                            taskViewModel)
                    }
                    3 -> {
                        newBitmap = faceDetection.detectFaces(editedImage, resources, baseContext)
                    }
                    4 -> {
                        // Retouch
                    }
                    5 -> {
                        newBitmap = maskingTool.processImage(
                            editedImage,
                            taskViewModel.maskToolAmountValue.value!!.toDouble(),
                            taskViewModel.maskToolCoreRadiusValue.value!!.toInt())
                    }
                    6 -> {
                        newBitmap = affineTransformation.transformBitmapByTriangles(
                            editedImage,
                            taskViewModel)
                    }
                    else -> {
                        newBitmap = editedImage
                    }
                }

            }
        }
        value.await()

        binding.progressBarOverlay.startAnimation(animFadeOut)
        unlockUI()
    }

    override fun onResume() {
        super.onResume()

        if (imageViewBasePosition == 0F) {
            imageViewBasePosition = binding.imageToEdit.x
        }
    }

    private val transitionDuration = 200
    private fun switchToolProps(newActiveFragment: Int) {
        if (taskViewModel.picturePicked && !taskViewModel.isApplyingChanges) {
            if (newActiveFragment == taskViewModel.selectedTool) {
                binding.toolProps.y += 500
                buttonTransitions[taskViewModel.selectedTool].reverseTransition(transitionDuration)
                taskViewModel.selectedTool = -1
                binding.imageToEdit.setImageBitmap(editedImage)
            }
            else if (newActiveFragment == -1) {
                supportFragmentManager.beginTransaction().replace(binding.toolProps.id, fragments[taskViewModel.selectedTool]).commit()
            }
            else if (taskViewModel.selectedTool != -1) {
                buttonTransitions[taskViewModel.selectedTool].reverseTransition(transitionDuration)
                buttonTransitions[newActiveFragment].startTransition(transitionDuration)
                supportFragmentManager.beginTransaction().replace(binding.toolProps.id, fragments[newActiveFragment]).commit()
                taskViewModel.selectedTool = newActiveFragment
                binding.imageToEdit.setImageBitmap(editedImage)
                newBitmap = editedImage
            }
            else {
                buttonTransitions[newActiveFragment].startTransition(transitionDuration)
                supportFragmentManager.beginTransaction().replace(binding.toolProps.id, fragments[newActiveFragment]).commit()
                taskViewModel.selectedTool = newActiveFragment
                binding.toolProps.y -= 500
                binding.imageToEdit.setImageBitmap(newBitmap)
            }
            binding.imageToEdit.invalidate()
            taskViewModel.seekbarWrapperHide.value = true
        }
        else if (taskViewModel.isApplyingChanges) {
            needToApplyWarning()
        }
        else {
            noImageWarning()
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
            setTitle(R.string.enter_file_name)
            setPositiveButton(R.string.done) { _, _ ->
                imageName = editText.text.toString()
                saveMediaToStorage()
                Toast.makeText(this.context, R.string.now_image_is_in_gallery, Toast.LENGTH_SHORT).show()
            }
            setNegativeButton(R.string.cancel) { _, _ ->
                // Do nothing
            }
            setView(dialogLayout)
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
            filename = "${imageName}.png"
        }
        else {
            val currentDate = Date()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
            val formattedDate = dateFormat.format(currentDate)
            filename = "${formattedDate}.png"
        }

        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.contentResolver?.also { resolver ->

                val contentValues = ContentValues().apply {

                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
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
            editedImage.compress(Bitmap.CompressFormat.PNG, 100, it)
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

    private fun lockUI() {
        taskViewModel.seekbarWrapperHide.value = true
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun unlockUI() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun lockToolScrollAndSave() {
        binding.tools.foreground = ResourcesCompat.getDrawable(resources, R.color.shadowed, theme)
        taskViewModel.isApplyingChanges = true
    }

    private fun unlockToolScrollAndSave() {
        binding.tools.foreground = ResourcesCompat.getDrawable(resources, R.color.transparent, theme)
        taskViewModel.isApplyingChanges = false
    }

    private fun revertChanges() {
        switchToolProps(-1)
        binding.imageToEdit.setImageBitmap(editedImage)
        taskViewModel.canDrawOnImageView = true
    }

    private fun noImageWarning() {
        if (canShowNoImageToast) {
            Toast.makeText(this, R.string.didnt_upload_image, Toast.LENGTH_SHORT).show()
            canShowNoImageToast = false
            noImageCoolDownTimer.start()
        }
    }

    private fun needToApplyWarning() {
        if (canShowNeedToApplyToast) {
            Toast.makeText(this, R.string.apply_changes_first, Toast.LENGTH_SHORT).show()
            canShowNeedToApplyToast = false
            needToApplyCoolDownTimer.start()
        }
    }

    private fun tooLargeImageWarning() {
        if (canShowTooLargeImageToast) {
            Toast.makeText(this, R.string.error_too_large_image, Toast.LENGTH_LONG).show()
            canShowTooLargeImageToast = false
            tooLargeImageCoolDownTimer.start()
        }
    }
}
