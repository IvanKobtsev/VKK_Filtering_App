package com.tsu.vkkfilteringapp

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import com.tsu.vkkfilteringapp.shapesrender.DrawThread
import java.util.Timer
import java.util.TimerTask


class FastRenderingView(context: Context, attributeSet: AttributeSet) : SurfaceView(context, attributeSet), SurfaceHolder.Callback {
    private var drawThread: DrawThread? = null
    private var drawBlank = false

    fun init(showImages: Boolean) {
        holder.addCallback(this)
        drawBlank = showImages

        actionDown()

        shapeVelocityIncrease.start()
    }

    fun switchImages(isChecked: Boolean) {
        drawThread!!.showImages = isChecked
    }

    fun previousShape(button: Button) : Int {
        if (drawThread!!.chosenShape == 1) {
            button.setEnabled(false)
            button.alpha = 0.5F
        }
        swapShapes(drawThread!!.chosenShape - 1)

        return drawThread!!.chosenShape
    }

    fun nextShape(button: Button) : Int {
        if (drawThread!!.chosenShape == 4) {
            button.setEnabled(false)
            button.alpha = 0.5F
        }
        swapShapes(drawThread!!.chosenShape + 1)

        return drawThread!!.chosenShape
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

    private fun actionDown() {
        try {
            animationLooper.cancel()
            shapeRotationStarter.cancel()
            shapeVelocityIncrease.cancel()
            shapeLeftRotationAnim.cancel()
            shapeRightRotationAnim.cancel()
        }
        catch (e: Exception) {
            Log.e("Error", "Certain timer is not yet initialized")
        }

        animationLooper = Timer()

        shapeRotationStarter = object: CountDownTimer(7000, 7000) {

            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                increasingVel = true
                shapeVelocityIncrease.start()
            }
        }

        shapeVelocityIncrease = object: CountDownTimer(10000, 10) {

            override fun onTick(millisUntilFinished: Long) {
                shapeVelocityIncreaseTick(millisUntilFinished)
            }

            override fun onFinish() {
                shapeVelocityIncreaseFinish()
            }
        }

        shapeLeftRotationAnim = object: CountDownTimer(10000, 10) {

            override fun onTick(millisUntilFinished: Long) {

                if (millisUntilFinished > 9000) {
                    shapeRotationVY -= 0.0001F
                }

                drawThread!!.shapeRotation.x += shapeRotationVX
                drawThread!!.shapeRotation.y += shapeRotationVY

                if (millisUntilFinished < 1000) {
                    shapeRotationVY += 0.0001F
                }
            }

            override fun onFinish() {
                shapeRotationVY = 0F
                shapeRightRotationAnim.start()
            }
        }

        shapeRightRotationAnim = object: CountDownTimer(10000, 10) {

            override fun onTick(millisUntilFinished: Long) {
                shapeRightRotationAnimTick(millisUntilFinished)
            }

            override fun onFinish() {
                shapeRotationVY = 0F
            }
        }

        restartAnimation = object : TimerTask() {
            override fun run() {
                shapeLeftRotationAnim.start()
            }
        }

        shapeRotationVX = 0F
        shapeRotationVY = 0F
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                actionDown()
                true
            }

            MotionEvent.ACTION_MOVE -> {
                drawThread!!.actionMove(event)
                true
            }
            MotionEvent.ACTION_UP -> {
                drawThread!!.cancelTouchEvents(event)
                drawThread!!.lastTouchCount = 0
                shapeRotationStarter.start()
                true
            }
            else -> false
        }
    }

    private fun shapeVelocityIncreaseTick(millisUntilFinished: Long) {
        if (increasingVel) {
            shapeRotationVX += 0.0001F
            shapeRotationVY += 0.0001F
        }
        drawThread!!.shapeRotation.x += shapeRotationVX
        drawThread!!.shapeRotation.y += shapeRotationVY
        if (millisUntilFinished < 9000) {
            increasingVel = false
        }
        if (millisUntilFinished < 1000) {
            shapeRotationVY -= 0.0001F
        }
    }

    private fun shapeVelocityIncreaseFinish() {
        shapeRotationVY = 0F
        shapeLeftRotationAnim.start()
        animationLooper.schedule(restartAnimation, 20000)
    }

    private fun shapeRightRotationAnimTick(millisUntilFinished: Long) {
        if (millisUntilFinished > 9000) {
            shapeRotationVY += 0.0001F
        }

        drawThread!!.shapeRotation.x += shapeRotationVX
        drawThread!!.shapeRotation.y += shapeRotationVY

        if (millisUntilFinished < 1000) {
            shapeRotationVY -= 0.0001F
        }
    }

    private var increasingVel = true
    private var shapeRotationVX = 0F
    private var shapeRotationVY = 0F
    private var animationLooper = Timer()
    private lateinit var shapeVelocityIncrease: CountDownTimer
    private lateinit var shapeRightRotationAnim: CountDownTimer
    private lateinit var shapeLeftRotationAnim: CountDownTimer
    private lateinit var shapeRotationStarter: CountDownTimer
    private lateinit var restartAnimation: TimerTask
}