package com.tsu.vkkfilteringapp

import android.graphics.Bitmap
import android.view.MotionEvent
import android.widget.Button
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tsu.vkkfilteringapp.fragments.SeekbarFragment
import com.tsu.vkkfilteringapp.graphics2d.Triangle2D

class TaskViewModel : ViewModel() {

    // Base
    var image = MutableLiveData<Bitmap>()
    var motionEvent = MutableLiveData<MotionEvent>()
    var hasUnsavedChanges = MutableLiveData(false)
    var picturePicked = false
    var canDrawOnImageView = true
    var isApplyingChanges = false
    var selectedTool = -1
    var tooLargeImage = MutableLiveData(false)
    lateinit var seekbarFragment: SeekbarFragment
    var isSeekbarWrapperActuallyHidden = false
    var seekbarWrapperHide = MutableLiveData(false)
    var acceptBasicSaving = MutableLiveData(false)
    var cancelBasicSaving = MutableLiveData(false)

    // Rotation-related
    var rotationToolNeedToUpdate = MutableLiveData(false)
    var rotationToolAngle = MutableLiveData(90F)

    // Filters-related
    var filtersToolNeedToUpdate = MutableLiveData(false)
    var filtersToolSelected = -1

    // Scaling-related
    var scalingToolNeedToUpdate = MutableLiveData(false)
    var scalingToolScale = MutableLiveData(0.5F)

    // Faces-related
    var facesToolNeedToUpdate = MutableLiveData(false)

    // Retouch-related
    var retouchToolNeedToUpdate = MutableLiveData(false)
    var retouchToolBrushRadius = MutableLiveData<Float>()
    var retouchToolIntensity = MutableLiveData<Float>()

    // Masking-related
    var maskToolNeedToUpdate = MutableLiveData(false)
    var maskToolAmountValue = MutableLiveData<Float>()
    var maskToolCoreRadiusValue = MutableLiveData<Float>()

    // Affine-related
    var affineToolNeedToUpdate = MutableLiveData(false)
    var affineToolSelectedPoint = MutableLiveData(0)
    var affineToolOrigTriangle = Triangle2D()
    var affineToolTransTriangle = Triangle2D()
    var affineToolCanApply = MutableLiveData(false)
    var affineToolShowCropped = MutableLiveData(false)
    var affineToolCancelPressed = MutableLiveData(false)
    var affineToolCroppedImageBorders = listOf(0F, 0F, 0F, 0F)

    // Seekbar-related
    var textViewToWrite = MutableLiveData<Button>()
    var textViewStringID = 0

}