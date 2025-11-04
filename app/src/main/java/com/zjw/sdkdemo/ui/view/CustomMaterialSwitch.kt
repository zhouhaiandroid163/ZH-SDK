package com.zjw.sdkdemo.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlin.math.abs

class CustomSwitchMaterial @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = com.google.android.material.R.attr.switchStyle) :
    SwitchMaterial(context, attrs, defStyleAttr) {

    private var touchX = 0f
    private var touchY = 0f
    private var isMovedBeyondThreshold = false

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchX = ev.x
                touchY = ev.y
                isMovedBeyondThreshold = false
                return super.onTouchEvent(ev)
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = ev.x - touchX
                val deltaY = ev.y - touchY

                if (abs(deltaX) > 50 || abs(deltaY) > 50) {
                    isMovedBeyondThreshold = true
                }
                return super.onTouchEvent(ev)
            }

            MotionEvent.ACTION_UP -> {
                val handled = super.onTouchEvent(ev)
                if (isMovedBeyondThreshold && handled) {
                    performClick()
                }
                return handled
            }

            MotionEvent.ACTION_CANCEL -> {
                isMovedBeyondThreshold = false
                return super.onTouchEvent(ev)
            }

            else -> return super.onTouchEvent(ev)
        }
    }

    override fun performClick(): Boolean {
        val handled = super.performClick()
        return handled
    }
}
