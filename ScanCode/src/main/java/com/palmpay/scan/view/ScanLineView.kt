package com.palmpay.scan.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.DrawableRes
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.palmpay.scan.utils.toPx
import com.zwj.scancode.R


@SuppressLint("UseCompatLoadingForDrawables")
class ScanLineView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint: Paint = Paint()

    private val rectF: RectF = RectF()

    private var position: Float = 0F

    private val bitMapPaint: Paint = Paint()

    private lateinit var trailingBitmap: Bitmap

    private lateinit var rotateBitMap: Bitmap

    private var bitMapRect: Rect = Rect()

    private var bitMapWidth = 0F

    private var animatorDown: AnimatorSet? = null

    private var animatorUp: AnimatorSet? = null

    private var isDown: Boolean = true

    var lineBitmap: Bitmap? = null

    var lineWidth = 360.toPx(context)

    var lineHeight = 2.toPx(context)

    var lineTop = 0F

    var animatorTime = 2000L

    var animatorWidth = 200.toPx(context)

    var animatorHeight = 300.toPx(context)

    var lineColor: IntArray = intArrayOf(
        ContextCompat.getColor(context, R.color.scan_line_1),
        ContextCompat.getColor(context, R.color.scan_line_2),
        ContextCompat.getColor(context, R.color.scan_line_3)
    )

    init {
        paint.style = Paint.Style.FILL
        setTrailingBitmap(R.drawable.scankit_scan_tail)
        bitMapPaint.isAntiAlias = true
        resetLineAnimator()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        drawLine(canvas)

        drawTrailing(canvas)
    }


    var lineLeft = 0F
    private fun drawLine(canvas: Canvas?) {
        if (lineWidth < width) {
            lineLeft = (width - lineWidth) / 2
        } else {
            lineWidth = width.toFloat()
        }
        if (lineTop == 0F) {
            rectF.top = (height - animatorHeight) / 2 + position
        } else {
            rectF.top = lineTop + position
        }
        rectF.left = lineLeft
        rectF.right = lineLeft + lineWidth
        rectF.bottom = rectF.top + lineHeight

        if (lineBitmap == null) {
            val linearGradient = LinearGradient(
                rectF.left,
                rectF.top,
                rectF.left + lineWidth,
                rectF.top,
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

            paint.shader = linearGradient

            canvas?.drawRect(rectF, paint)
        } else {
            lineBitmap?.let {
                canvas?.drawBitmap(it, null, rectF, paint)
            }
        }
    }

    private fun drawTrailing(canvas: Canvas?) {
        if (isDown) {
            bitMapRect.top = (rectF.bottom - bitMapWidth).toInt()
            bitMapRect.left = rectF.left.toInt()
            bitMapRect.right = rectF.right.toInt()
            bitMapRect.bottom = rectF.bottom.toInt()

            canvas?.drawBitmap(rotateBitMap, null, bitMapRect, bitMapPaint)
        } else {
            bitMapRect.top = (rectF.top).toInt()
            bitMapRect.left = rectF.left.toInt()
            bitMapRect.right = rectF.right.toInt()
            bitMapRect.bottom = (rectF.top + bitMapWidth).toInt()

            canvas?.drawBitmap(trailingBitmap, null, bitMapRect, bitMapPaint)
        }
    }

    fun setTrailingBitmap(@DrawableRes id: Int) {
        trailingBitmap = context.resources.getDrawable(id).toBitmap()
        val matrix = Matrix()
        matrix.setRotate(
            180F,
            (trailingBitmap.width / 2).toFloat(),
            (trailingBitmap.height / 2).toFloat()
        )
        rotateBitMap = Bitmap.createBitmap(
            trailingBitmap,
            0,
            0,
            trailingBitmap.width,
            trailingBitmap.height,
            matrix,
            true
        )
    }

    fun resetLineAnimator() {
        if (animatorDown != null || animatorUp != null || animatorDown?.isRunning == true || animatorUp?.isRunning == true) {
            release()
        }
        animatorDown = AnimatorSet()
        animatorUp = AnimatorSet()
        val lineAnimator = ValueAnimator.ofFloat(0F, animatorHeight)
        val bitMapHeightAnimator = ValueAnimator.ofFloat(0F, animatorWidth, 0F)

        val lineAnimatorUp = ValueAnimator.ofFloat(animatorHeight, 0F)
        val bitMapHeightAnimatorUp = ValueAnimator.ofFloat(0F, animatorWidth, 0F)

        bitMapHeightAnimator.interpolator = LinearInterpolator()
        bitMapHeightAnimator.addUpdateListener {
            bitMapWidth = it.animatedValue as Float
            position = lineAnimator.animatedValue as Float
            postInvalidateOnAnimation()
        }

        bitMapHeightAnimatorUp.interpolator = LinearInterpolator()
        bitMapHeightAnimatorUp.addUpdateListener {
            bitMapWidth = it.animatedValue as Float
            position = lineAnimatorUp.animatedValue as Float
            postInvalidateOnAnimation()
        }

        animatorDown?.duration = animatorTime
        animatorDown?.play(lineAnimator)?.with(bitMapHeightAnimator)

        animatorUp?.duration = animatorTime
        animatorUp?.play(lineAnimatorUp)?.with(bitMapHeightAnimatorUp)

        animatorDown?.doOnEnd {
            isDown = false
            animatorUp?.start()
        }
        animatorUp?.doOnEnd {
            isDown = true
            animatorDown?.start()
        }
        animatorDown?.start()

    }

    fun pause() {
        if (isDown){
            animatorDown?.pause()
        }else{
            animatorUp?.pause()
        }
    }

    fun start(){
        if (isDown){
            animatorDown?.start()
        }else{
            animatorUp?.start()
        }
    }

    fun release() {
        animatorDown?.removeAllListeners()
        animatorUp?.removeAllListeners()
        animatorDown?.cancel()
        animatorDown?.clone()
        animatorUp?.cancel()
        animatorUp?.clone()
        animatorDown = null
        animatorUp = null
    }
}