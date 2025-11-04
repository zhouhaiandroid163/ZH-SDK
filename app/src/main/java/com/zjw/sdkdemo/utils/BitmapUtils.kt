package com.zjw.sdkdemo.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withSave

object BitmapUtils {
    /**
     * 合并图片
     * Merge images
     */
    @JvmStatic
    fun combineBitmap(background: Bitmap?, foreground: Bitmap?, x: Int, y: Int): Bitmap? {
        if (background == null || foreground == null) {
            return null
        }
        val bgWidth = background.width
        val bgHeight = background.height
        val newBitmap = createBitmap(bgWidth, bgHeight)
        val canvas = Canvas(newBitmap)
        canvas.drawBitmap(background, 0f, 0f, null)
        canvas.drawBitmap(foreground, x.toFloat(), y.toFloat(), null)
        canvas.withSave {
        }
        return newBitmap
    }
}