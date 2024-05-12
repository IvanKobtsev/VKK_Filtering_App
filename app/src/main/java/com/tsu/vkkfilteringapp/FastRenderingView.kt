package com.tsu.vkkfilteringapp

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button


class FastRenderingView(context: Context, attributeSet: AttributeSet) : SurfaceView(context, attributeSet), SurfaceHolder.Callback {
    private var drawThread: DrawThread? = null
    var drawBlank = false

    fun init(blankShape: Boolean) {
        holder.addCallback(this)
        drawBlank = blankShape
    }

    fun switchImages(isChecked: Boolean) {
        drawThread!!.blankShape = isChecked
    }

    fun previousShape(button: Button) {
        if (drawThread!!.chosenShape == 1) {
            button.setEnabled(false)
            button.alpha = 0.5F
        }
        swapShapes(drawThread!!.chosenShape - 1)
    }

    fun nextShape(button: Button) {
        if (drawThread!!.chosenShape == 4) {
            button.setEnabled(false)
            button.alpha = 0.5F
        }
        swapShapes(drawThread!!.chosenShape + 1)
    }

    private fun swapShapes(newChosenShape: Int) {
        // Do some fade in animation.................
        drawThread!!.chosenShape = newChosenShape
        drawThread!!.loadShapeToShow()
        // Do some fade out animation...............
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        drawThread = DrawThread(getHolder(), resources)
        drawThread!!.init(drawBlank)
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