package com.tsu.vkkfilteringapp

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView

class ImageViewMotionOverride(context: Context, attributeSet: AttributeSet) : androidx.appcompat.widget.AppCompatImageView(context, attributeSet) {

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                true
            }

            MotionEvent.ACTION_MOVE -> {
                true
            }

            MotionEvent.ACTION_UP -> {
                true
            }
            else -> false
        }
    }

}