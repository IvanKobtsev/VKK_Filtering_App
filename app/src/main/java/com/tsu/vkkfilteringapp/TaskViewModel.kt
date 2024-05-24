package com.tsu.vkkfilteringapp

import android.graphics.Bitmap
import android.view.MotionEvent
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tsu.vkkfilteringapp.graphics2d.Triangle2D
import com.tsu.vkkfilteringapp.graphics3d.Triangle3D

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

    // Rotation-related
    var rotationToolNeedToUpdate = MutableLiveData(false)
    var rotationToolCancelPressed = MutableLiveData(false)

    // Filters-related
    var filtersToolNeedToUpdate = MutableLiveData(false)
    var filtersToolCancelPressed = MutableLiveData(false)

    // Scaling-related
    var scalingToolNeedToUpdate = MutableLiveData(false)
    var scalingToolCancelPressed = MutableLiveData(false)

    // Faces-related
    var facesToolNeedToUpdate = MutableLiveData(false)
    var facesToolCancelPressed = MutableLiveData(false)

    // Retouch-related
    var retouchToolNeedToUpdate = MutableLiveData(false)
    var retouchToolCancelPressed = MutableLiveData(false)

    // Masking-related
    var maskToolNeedToUpdate = MutableLiveData(false)
    var maskToolCancelPressed = MutableLiveData(false)

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
    var textViewToWrite = MutableLiveData<TextView>()

}