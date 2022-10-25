package com.palmpay.scan.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.palmpay.scan.bean.CodeBean
import com.palmpay.scan.utils.toPx
import com.palmpay.scan.R

class CodePointView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val animators: MutableList<AnimatorSet> = mutableListOf()
    private var cancelAction: (() -> Unit)? = null
    private var pointAction: ((codeBean: CodeBean) -> Unit)? = null

    private var cancelText: String = "cancel"

    @ColorRes
    private var cancelColor: Int = R.color.width

    private var cancelTextSize: Float = 18.toPx(context)

    var cancelTop = 55.toPx(context).toInt()

    var cancelLeft = 20.toPx(context).toInt()

    @DrawableRes
    var pointViewRes: Int = R.drawable.scan_code_point

    var pointViewSize: Int = 50.toPx(context).toInt()

    @ColorRes
    var successColorRes: Int = R.color.half_transparent

    private val cancelTextView: TextView

    val scanLineView: ScanLineView


    init {
        cancelTextView = TextView(context)
        cancelTextView.text = cancelText
        cancelTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, cancelTextSize)
        cancelTextView.setTextColor(context.resources.getColor(cancelColor))



        addView(cancelTextView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        cancelTextView.visibility = View.GONE

        scanLineView = ScanLineView(context)
        addView(scanLineView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        cancelTextView.setOnClickListener {
            cancelAction?.invoke()
            cancelAnimator()
            scanLineView.visibility = View.VISIBLE
            scanLineView.start()

            removeAllViews()
            addView(cancelTextView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
            addView(scanLineView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            cancelTextView.visibility = View.GONE
            setBackgroundResource(R.color.transparent)
        }
    }


    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.tag == null) {

                if (i == 0) {
                    measureChild(child, MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
                    child.layout(
                        cancelLeft,
                        cancelTop,
                        cancelLeft + child.measuredWidth,
                        cancelTop + child.measuredHeight
                    )
                } else {
                    measureChild(child, MeasureSpec.AT_MOST, MeasureSpec.AT_MOST)
                    child.layout(
                        0,
                        0,
                        width,
                        height
                    )
                }

            } else {
                val codeBean: CodeBean = child.tag as CodeBean
                child.layout(
                    codeBean.center.x.toInt() - child.layoutParams.width / 2,
                    codeBean.center.y.toInt() - child.layoutParams.width / 2,
                    codeBean.center.x.toInt() + child.layoutParams.width / 2,
                    codeBean.center.y.toInt() + child.layoutParams.width / 2
                )
            }
        }
    }


    fun setQrCodes(list: List<CodeBean>) {
        cancelTextView.visibility = View.VISIBLE
        setBackgroundResource(successColorRes)
        scanLineView.pause()
        scanLineView.visibility = View.GONE
        for (codeBean in list) {
            val view = View(context)
            view.setBackgroundResource(pointViewRes)
            view.tag = codeBean
            view.setOnClickListener {
                pointAction?.invoke(codeBean)
            }
            addView(view, LayoutParams(pointViewSize, pointViewSize))
            setAnimator(view)
        }
        requestLayout()
    }


    private fun setAnimator(view: View) {
        val animatorSet = AnimatorSet()
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.7f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.7f, 1f)
        scaleX.repeatCount = ValueAnimator.INFINITE
        scaleY.repeatCount = ValueAnimator.INFINITE
        animatorSet.playTogether(scaleX, scaleY)
        animatorSet.duration = 2000
        animatorSet.startDelay = 500
        animatorSet.start()

        animators.add(animatorSet)
    }

    private fun cancelAnimator() {
        for (animator in animators) {
            animator.cancel()
        }
        animators.clear()
    }

    fun release() {
        cancelAnimator()
        scanLineView.release()
    }

    fun setCancelButtonListener(cancelAction: (() -> Unit)?) {
        this.cancelAction = cancelAction
    }

    fun setPointButtonListener(pointAction: ((codeBean: CodeBean) -> Unit)?) {
        this.pointAction = pointAction
    }

    fun setCancelText(cancelText:String){
        this.cancelText =cancelText
        cancelTextView.text = cancelText
    }

    fun setCancelTextColor(cancelColor:Int){
      cancelTextView.setTextColor(cancelColor)
    }

    fun setCancelTextSize(cancelTextSize:Float) {
        cancelTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,cancelTextSize)
    }
}