package com.palmpay.scan.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.palmpay.scan.utils.toPx
import com.palmpay.scan.R

class BoxView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var boxPaint: Paint = Paint()

    var boxSize: Float = 300.toPx(context)

    var boxTop: Float = 0F

    var boxRound: Float = 10.toPx(context)

    var boxRect: RectF = RectF()

    var boxStrokeWidth = 2.toPx(context)

    var mantleColor = ContextCompat.getColor(context, R.color.half_transparent)

    var boxStrokeColor = ContextCompat.getColor(context, R.color.width)

    private val porterDuffXfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)

    private val linePaint: Paint = Paint()

    var lineWidth: Float = 300.toPx(context)

    var lineHeight: Float = 2.toPx(context)

    var lineAnimatorDuration: Long = 2500

    private var lineMoveDistance = 0F

    private var lineAnimator: ValueAnimator? = null

    var lineColor: IntArray = intArrayOf(
        ContextCompat.getColor(context, R.color.scan_line_1),
        ContextCompat.getColor(context, R.color.scan_line_2),
        ContextCompat.getColor(context, R.color.scan_line_3)
    )

    var lineBitmap: Bitmap? = null

    var hornWidth: Float = 6.toPx(context)

    var hornLength: Float = 20.toPx(context)

    var hornColor: Int = ContextCompat.getColor(context, R.color.width)

    var boxType: Int = 0

    init {
        resetLineAnimator()
        boxPaint.isAntiAlias = true
        linePaint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (boxType == 0) {
            drawRoundBox(canvas)
        } else {
            drawBox(canvas)
        }
        drawLine(canvas)
    }


    private fun drawLine(canvas: Canvas?) {
        var left = boxRect.left
        if (boxSize >= lineWidth) {
            left = (boxSize - lineWidth) / 2 + boxRect.left
        }
        if (lineBitmap == null) {
            val linearGradient =
                LinearGradient(
                    left,
                    boxRect.top,
                    left + lineWidth,
                    boxRect.top,
                    intArrayOf(
                        lineColor[0],
                        lineColor[1],
                        lineColor[2],
                        lineColor[1],
                        lineColor[0]
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )

            linePaint.shader = linearGradient

            canvas?.drawRect(
                left,
                boxRect.top + lineMoveDistance,
                left + lineWidth,
                boxRect.top + lineHeight + lineMoveDistance,
                linePaint
            )
        } else {
            canvas?.drawBitmap(
                lineBitmap!!, null, RectF(
                    left,
                    boxRect.top + lineMoveDistance,
                    left + lineWidth,
                    boxRect.top + lineHeight + lineMoveDistance
                ), linePaint
            )
        }

    }

    fun resetLineAnimator() {
        if (lineAnimator != null || lineAnimator?.isRunning == true) {
            lineAnimator?.cancel()
            lineAnimator = null
        }

        lineAnimator = ValueAnimator.ofFloat(0F, boxSize)

        lineAnimator?.addUpdateListener {
            lineMoveDistance = it.animatedValue as Float
            postInvalidateOnAnimation()
        }

        lineAnimator?.duration = lineAnimatorDuration
        lineAnimator?.interpolator = LinearInterpolator()
        lineAnimator?.repeatCount = ValueAnimator.INFINITE
        lineAnimator?.start()

    }

    fun release() {
        if (lineAnimator != null) {
            lineAnimator?.removeAllListeners()
            lineAnimator?.cancel()
        }
    }


    private fun drawRoundBox(canvas: Canvas?) {

        boxPaint.style = Paint.Style.FILL
        val left = (width - boxSize) / 2
        boxRect = if (boxTop == 0F) {
            val top = (height - boxSize) / 2
            RectF(left, top, left + boxSize, top + boxSize)
        } else {
            RectF(left, boxTop, left + boxSize, boxTop + boxSize)
        }
        canvas?.drawRoundRect(
            boxRect,
            boxRound,
            boxRound,
            boxPaint
        )


        boxPaint.color = mantleColor
        boxPaint.xfermode = porterDuffXfermode
        canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), boxPaint)
        boxPaint.xfermode = null


        boxPaint.style = Paint.Style.STROKE
        boxPaint.color = boxStrokeColor
        boxPaint.strokeWidth = boxStrokeWidth
        canvas?.drawRoundRect(
            boxRect,
            boxRound,
            boxRound,
            boxPaint
        )
    }

    private fun drawBox(canvas: Canvas?) {

        boxPaint.style = Paint.Style.FILL
        val left = (width - boxSize) / 2
        boxRect = if (boxTop == 0F) {
            val top = (height - boxSize) / 2
            RectF(left, top, left + boxSize, top + boxSize)
        } else {
            RectF(left, boxTop, left + boxSize, boxTop + boxSize)
        }

        canvas?.drawRect(
            boxRect,
            boxPaint
        )

        boxPaint.color = mantleColor
        boxPaint.xfermode = porterDuffXfermode
        canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), boxPaint)
        boxPaint.xfermode = null


        boxPaint.style = Paint.Style.STROKE
        boxPaint.color = boxStrokeColor
        boxPaint.strokeWidth = boxStrokeWidth
        canvas?.drawRect(
            boxRect,
            boxPaint
        )

        boxPaint.style = Paint.Style.FILL
        boxPaint.strokeWidth = hornWidth
        boxPaint.color = hornColor
        canvas?.drawLine(
            boxRect.left - (boxStrokeWidth / 2) - (hornWidth - boxStrokeWidth) / 2,
            boxRect.top,
            boxRect.left + hornLength,
            boxRect.top,
            boxPaint
        )

        canvas?.drawLine(
            boxRect.left,
            boxRect.top - (boxStrokeWidth / 2) - (hornWidth - boxStrokeWidth) / 2,
            boxRect.left,
            boxRect.top + hornLength,
            boxPaint
        )

        canvas?.drawLine(
            boxRect.right - hornLength,
            boxRect.top,
            boxRect.right + (boxStrokeWidth / 2) + (hornWidth - boxStrokeWidth) / 2,
            boxRect.top,
            boxPaint
        )

        canvas?.drawLine(
            boxRect.right,
            boxRect.top + (boxStrokeWidth / 2) + (hornWidth - boxStrokeWidth) / 2,
            boxRect.right,
            boxRect.top + hornLength,
            boxPaint
        )

        canvas?.drawLine(
            boxRect.right - hornLength,
            boxRect.bottom,
            boxRect.right + (boxStrokeWidth / 2) + (hornWidth - boxStrokeWidth) / 2,
            boxRect.bottom,
            boxPaint
        )

        canvas?.drawLine(
            boxRect.right,
            boxRect.bottom - hornLength,
            boxRect.right,
            boxRect.bottom + (boxStrokeWidth / 2) + (hornWidth - boxStrokeWidth) / 2,
            boxPaint
        )

        canvas?.drawLine(
            boxRect.left + hornLength,
            boxRect.bottom,
            boxRect.left - (boxStrokeWidth / 2) - (hornWidth - boxStrokeWidth) / 2,
            boxRect.bottom,
            boxPaint
        )

        canvas?.drawLine(
            boxRect.left,
            boxRect.bottom - hornLength,
            boxRect.left,
            boxRect.bottom + (boxStrokeWidth / 2) + (hornWidth - boxStrokeWidth) / 2,
            boxPaint
        )
    }
}