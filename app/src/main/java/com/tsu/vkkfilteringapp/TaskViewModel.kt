package com.tsu.vkkfilteringapp

import android.graphics.Bitmap
import android.view.MotionEvent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TaskViewModel : ViewModel() {
    var image = MutableLiveData<Bitmap>()
    var affineToolSelectedPoint = MutableLiveData<Int>()
    var affineToolNeedToUpdate = MutableLiveData<Boolean>()
    var motionEvent = MutableLiveData<MotionEvent>()
}