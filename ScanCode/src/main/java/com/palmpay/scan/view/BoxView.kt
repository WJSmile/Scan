package com.palmpay.scan.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.palmpay.scan.utils.toPx
import com.zwj.scancode.R

class BoxView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var boxPaint: Paint = Paint()

    private var boxWidth: Float = 200.toPx(context)

    private var boxTop: Float = 140.toPx(context)

    init {
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = 2.toPx(context)
        boxPaint.color = ContextCompat.getColor(context, R.color.width)
    }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val left = (width - boxWidth) / 2
        canvas?.drawRoundRect(
            RectF(left, boxTop, left + boxWidth, boxTop + boxWidth),
            10.toPx(context),
            10.toPx(context),
            boxPaint
        )
    }
}