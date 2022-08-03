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
    private val paint: Paint = Paint()
    private val codeBeans: MutableList<CodeBean> = mutableListOf()

    init {
        paint.color = Color.RED
        paint.textSize = 50f
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 5f
        paint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        for (qrCodeBean in codeBeans) {
            canvas?.drawCircle(qrCodeBean.center.x, qrCodeBean.center.y, 50F, paint)
        }

    }

    fun setQrCodes(list: List<CodeBean>) {
        codeBeans.clear()
        codeBeans.addAll(list)
        postInvalidate()
    }
}