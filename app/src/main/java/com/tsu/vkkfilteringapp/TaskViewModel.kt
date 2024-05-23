package com.tsu.vkkfilteringapp

import android.graphics.Bitmap
import android.view.MotionEvent
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

    // Affine-related
    var affineToolNeedToUpdate = MutableLiveData(false)
    var affineToolSelectedPoint = MutableLiveData(0)
    var affineToolOrigTriangle = Triangle2D()
    var affineToolTransTriangle = Triangle2D()
    var affineToolCanApply = MutableLiveData(false)
    var affineToolShowCropped = MutableLiveData(false)
    var affineToolCancelPressed = MutableLiveData(false)
    var affineToolCroppedImageBorders = listOf(0F, 0F, 0F, 0F)

}