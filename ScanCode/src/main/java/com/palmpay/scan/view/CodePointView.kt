package com.palmpay.scan.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.palmpay.scan.bean.CodeBean

class CodePointView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val circlePaint: Paint = Paint()
    private val codeBeans: MutableList<CodeBean> = mutableListOf()

    private val textPaint: Paint = Paint()

    init {
        circlePaint.color = Color.parseColor("#2A349A")
        circlePaint.style = Paint.Style.FILL
        circlePaint.isAntiAlias = true

        textPaint.textSize = 20f
        textPaint.color = Color.WHITE

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawText("取消", 50F, 20F, textPaint)
        for (qrCodeBean in codeBeans) {
            canvas?.drawCircle(qrCodeBean.center.x, qrCodeBean.center.y, 50F, circlePaint)
        }
    }

    fun setQrCodes(list: List<CodeBean>) {
        codeBeans.clear()
        codeBeans.addAll(list)
        postInvalidate()
    }
}