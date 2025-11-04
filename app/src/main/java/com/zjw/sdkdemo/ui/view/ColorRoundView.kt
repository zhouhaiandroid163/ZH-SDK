package com.zjw.sdkdemo.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.zjw.sdkdemo.R

class ColorRoundView : View {
    private var bgColor = 0
    private var ringColor = 0
    private var paintBg: Paint? = null
    private var paintRing: Paint? = null
    private var paintClick: Paint? = null
    private var click = false
    private val padding = 8f

    private val rectRing = RectF()
    private val rectF = RectF()
    private var radius = 0f

    constructor(context: Context?) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        val tArray = context.obtainStyledAttributes(attrs, R.styleable.ColorRoundView)
        try {
            bgColor = tArray.getColor(R.styleable.ColorRoundView_bgColor, DEFAULT__COLOR)
            ringColor = tArray.getColor(R.styleable.ColorRoundView_ringColor, DEFAULT__COLOR)
        } finally {
            tArray.recycle()
        }
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private fun init() {
        paintBg = Paint()
        paintBg!!.isAntiAlias = true
        paintBg!!.strokeCap = Paint.Cap.ROUND
        paintBg!!.style = Paint.Style.FILL
        paintBg!!.color = bgColor

        paintRing = Paint()
        paintRing!!.isAntiAlias = true
        paintRing!!.strokeCap = Paint.Cap.ROUND
        paintRing!!.style = Paint.Style.STROKE
        paintRing!!.color = ringColor
        paintRing!!.strokeWidth = 2f

        paintClick = Paint()
        paintClick!!.isAntiAlias = true
        paintClick!!.strokeCap = Paint.Cap.ROUND
        paintClick!!.style = Paint.Style.STROKE
        paintClick!!.color = Color.WHITE
        paintClick!!.strokeWidth = 4f
    }



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val mWidth = width.toFloat()
        val mHeight = height.toFloat()

        radius = if (mHeight > mWidth) mWidth / 2 else mHeight / 2
        canvas.drawCircle(width / 2f, height / 2f, radius - padding, paintBg!!)

        val ringRadius = radius - padding
        rectRing.set(
            width / 2f - ringRadius,
            height / 2f - ringRadius,
            width / 2f + ringRadius,
            height / 2f + ringRadius
        )
        canvas.drawArc(rectRing, 0f, 360f, false, paintRing!!)

        if (click) {
            val clickStroke = paintClick!!.strokeWidth
            rectF.set(
                width / 2f - radius + clickStroke,
                height / 2f - radius + clickStroke,
                width / 2f + radius - clickStroke,
                height / 2f + radius - clickStroke
            )
            canvas.drawArc(rectF, 0f, 360f, false, paintClick!!)
        }
    }

    fun setBgColor(bgColor: Int, ringColor: Int) {
        paintBg!!.color = ContextCompat.getColor(context, bgColor)
        paintRing!!.color = ContextCompat.getColor(context, ringColor)
        this.bgColor = ContextCompat.getColor(context, bgColor)
    }

    fun getColor(): Int {
        return bgColor
    }

    companion object {
        private val DEFAULT__COLOR = Color.argb(235, 0, 0, 0)
    }
}