package com.palmpay.scan.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.OnLifecycleEvent
import com.palmpay.scan.callback.OnScanListener
import com.palmpay.scan.code.NativeLib
import com.palmpay.scan.utils.Utils

class ScanView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), LifecycleObserver {


    private var nativeLib: NativeLib? = NativeLib()
    private val cameraView: CameraView
    private var codePointView: CodePointView? = null
    private val boxView: BoxView

    private val lifecycle: Lifecycle
    private var onScanListener: OnScanListener? = null

    private var scanType: ScanType = ScanType.SCAN_BOX

    private var isPause = false

    init {
        lifecycle = (context as FragmentActivity).lifecycle

        lifecycle.addObserver(this)
        cameraView = CameraView(context)
        addView(cameraView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        codePointView = CodePointView(context)
        addView(codePointView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        boxView = BoxView(context)
        addView(boxView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))


        val paths = Utils.copyWeChatModel(context)
        if (paths.isNotEmpty()) {
            nativeLib?.initScan(
                paths[0],
                paths[1],
                paths[2],
                paths[3]
            )
        }

        cameraView.setOnAnalyzerListener {
            if (!isPause) {
                if (scanType == ScanType.SCAN_FULL_SCREEN) {
                    val codeBeans = nativeLib?.scanCode(
                        Utils.yuv420888ToNv21(it), it.width, it.height
                    )
                    if (!codeBeans.isNullOrEmpty()) {
                        if (lifecycle is LifecycleRegistry) {
                            context.runOnUiThread {
                                lifecycle.currentState = Lifecycle.State.CREATED
                            }
                        }
                        context.runOnUiThread {
                            codePointView?.setQrCodes(codeBeans)
                        }
                        isPause = onScanListener?.onResult(codeBeans) == true
                    }

                } else if (scanType == ScanType.SCAN_BOX) {

                    val codeBeans = nativeLib?.scanCodeCut(
                        Utils.yuv420888ToNv21(it), it.width, it.height,
                        boxView.boxSize.toInt(), boxView.boxRect.top.toInt()
                    )
                    if (!codeBeans.isNullOrEmpty()) {
                        isPause = onScanListener?.onResult(codeBeans) == true

                    }
                }
            }
            it.close()
        }

        codePointView?.setCancelButtonListener {
            isPause = false
            if (lifecycle is LifecycleRegistry) {
                lifecycle.currentState = Lifecycle.State.RESUMED
            }
            onScanListener?.onCancel()
        }

        codePointView?.setPointButtonListener {
            onScanListener?.onPointClick(it)
        }

        refreshStatus()
    }

    fun pause(){
        isPause = true
    }

    fun release() {
        cameraView.setOnAnalyzerListener(null)
        nativeLib?.release()
        nativeLib = null
        codePointView?.release()
        codePointView = null
        boxView.release()
    }


    fun setOnScanListener(onScanListener: OnScanListener?) {
        this.onScanListener = onScanListener
    }

    private fun refreshStatus() {
        if (scanType == ScanType.SCAN_FULL_SCREEN) {
            codePointView?.visibility = View.VISIBLE
            boxView.visibility = View.GONE
        } else {
            codePointView?.visibility = View.GONE
            boxView.visibility = View.VISIBLE
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        cameraView.unbindAll()
        lifecycle.removeObserver(this)
    }

    fun setScanType(scanType: ScanType) {
        this.scanType = scanType
        refreshStatus()
    }

    /**
     *
     * @param cancelText  扫码成功后取消按钮文字
     *
     */
    fun setCancelText(cancelText: String) {
        codePointView?.cancelText = cancelText
    }

    /**
     *
     * @param color  扫码成功后取消按钮文字颜色
     *
     */
    fun setCancelColor(@ColorRes color: Int) {
        codePointView?.cancelColor = color
    }

    /**
     *
     * @param cancelTextSize  扫码成功后取消按钮文字字体大小
     *
     */
    fun setCancelTextSize(cancelTextSize: Float) {
        codePointView?.cancelTextSize = cancelTextSize
    }


    /**
     *
     * @param cancelTop 扫码成功后取消按钮上边距
     *
     */
    fun setCancelTop(cancelTop: Int) {
        codePointView?.cancelTop = cancelTop
    }


    /**
     *
     * @param cancelLeft 扫码成功后取消按钮左边距
     *
     */
    fun setCancelLeft(cancelLeft: Int) {
        codePointView?.cancelLeft = cancelLeft
    }


    /**
     *
     * @param pointViewRes 二维码标记点背景资源
     *
     */
    fun setPointViewRes(@DrawableRes pointViewRes: Int) {
        codePointView?.pointViewRes = pointViewRes
    }


    /**
     *
     * @param size 二维码标记点大小
     *
     */
    fun setPointViewSize(size: Int) {
        codePointView?.pointViewSize = size
    }

    /**
     * @param successColorRes 识别到二维码显示的背景色
     */
    fun setSuccessColorRes(successColorRes: Int) {
        codePointView?.successColorRes = successColorRes
    }


    /**
     *@param size 扫描框大小
     *
     */
    fun setBoxSize(size: Float) {
        boxView.boxSize = size
        boxView.resetLineAnimator()
    }


    /**
     * @param round 扫码框圆角
     */
    fun setBoxRound(round: Float) {
        boxView.boxRound = round
    }

    /**
     *@param color 背景颜色
     */
    fun setMantleColor(color: Int) {
        boxView.mantleColor = color
    }

    /**
     * @param color 扫描框边框颜色
     */
    fun setBoxStrokeColor(color: Int) {
        boxView.boxStrokeColor = color
    }

    /**
     * @param strokeWidth 扫描框边框宽度
     */
    fun setBoxStrokeWidth(strokeWidth: Float) {
        boxView.boxStrokeWidth = strokeWidth
    }

    /**
     * @param lineWidth 扫描线宽度
     */
    fun setLineWidth(lineWidth: Float) {
        boxView.lineWidth = lineWidth
        codePointView?.scanLineView?.lineWidth = lineWidth
    }

    /**
     * @param lineHeight 扫描线高度
     */
    fun setLineHeight(lineHeight: Float) {
        boxView.lineHeight = lineHeight
        codePointView?.scanLineView?.lineHeight = lineHeight
    }

    /**
     * @param lineTop 扫描线距离顶部的距离
     */
    fun setLineTop(lineTop: Float) {
        boxView.boxTop = lineTop
        codePointView?.scanLineView?.lineTop = lineTop
    }

    /**
     * @param lineColor 扫描线颜色
     */
    fun setLineColor(lineColor: IntArray) {
        boxView.lineColor = lineColor
    }

    /**
     * @param duration 动画时长
     */
    fun setLineAnimatorDuration(duration: Long) {
        boxView.lineAnimatorDuration = duration
        boxView.resetLineAnimator()
    }

    /**
     * @param hornWidth 直角宽度
     */
    fun setHornWidth(hornWidth: Float) {
        boxView.hornWidth = hornWidth
    }

    /**
     * @param hornLength 直角长度
     */
    fun setHornLength(hornLength: Float) {
        boxView.hornLength = hornLength
    }

    /**
     * @param hornColor 直角颜色
     */
    fun setHornColor(hornColor: Int) {
        boxView.hornColor = hornColor
    }

    /**
     * @param boxType box样式
     */
    fun setBoxType(boxType: Int) {
        boxView.boxType = boxType
    }

    /**
     * @param animatorWidth 动画拖尾动画最大宽度
     */
    fun setAnimatorWidth(animatorWidth: Float) {
        codePointView?.scanLineView?.animatorWidth = animatorWidth
    }

    /**
     * @param animatorHeight 扫描线动画滚动距离
     */
    fun setAnimatorHeight(animatorHeight: Float) {
        codePointView?.scanLineView?.animatorHeight = animatorHeight
    }

    /**
     * @param time 扫描线动画时间
     */
    fun setAnimatorTime(time: Long) {
        codePointView?.scanLineView?.animatorTime = time
    }

    /**
     * @param id 扫描线资源id
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    fun setLineBitmap(@DrawableRes id: Int) {
        setLineBitmap(context.resources.getDrawable(id).toBitmap())
    }

    /**
     * @param bitmap 扫描线bitmap
     */
    fun setLineBitmap(bitmap: Bitmap) {
        boxView.lineBitmap = bitmap
        codePointView?.scanLineView?.lineBitmap = bitmap
    }

    /**
     * @param id 拖尾效果资源id
     */
    fun setTrailingBitmap(@DrawableRes id: Int) {
        codePointView?.scanLineView?.setTrailingBitmap(id)
    }

}