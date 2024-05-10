package com.tsu.vkkfilteringapp

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView


class FastRenderingView(context: Context, attributeSet: AttributeSet) : SurfaceView(context, attributeSet), SurfaceHolder.Callback {
    private var drawThread: DrawThread? = null

    fun init() {
        holder.addCallback(this)
    }

    fun switchImages() {
        drawThread!!.blankShape = !drawThread!!.blankShape
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        drawThread = DrawThread(getHolder(), resources)
        drawThread!!.init()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        drawThread!!.running = false
        var joined = false
        while (!joined) {
            try {
                drawThread!!.join()
                joined = true
            } catch (e: InterruptedException) {
                // retry until joined
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return drawThread!!.onTouchEvent(event)
    }

}