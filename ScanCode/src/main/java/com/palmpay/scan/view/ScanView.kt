package com.palmpay.scan.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.OnLifecycleEvent
import com.palmpay.scan.bean.CodeBean
import com.palmpay.scan.callback.OnScanListener
import com.palmpay.scan.code.NativeLib
import com.palmpay.scan.utils.Utils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.Charset

class ScanView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val cameraView: CameraView
    private val nativeLib: NativeLib = NativeLib()
    private val codePointView: CodePointView

    private val lifecycle: Lifecycle
    private var onScanListener: OnScanListener? = null

    init {
        lifecycle = (context as FragmentActivity).lifecycle
        cameraView = CameraView(context)
        addView(cameraView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        codePointView = CodePointView(context)
        addView(codePointView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        val paths = Utils.copyWeChatModel(context)
        if (paths.isNotEmpty()) {
            nativeLib.initScan(
                paths[0],
                paths[1],
                paths[2],
                paths[3]
            )
        }

        cameraView.setOnAnalyzerListener {
            nativeLib.setImageByte(
                Utils.yuv420888ToNv21(it), it.width, it.height
            )
            it.close()
        }

        nativeLib.setPointCallBack {
            if (lifecycle is LifecycleRegistry) {
                context.runOnUiThread {
                    lifecycle.currentState = Lifecycle.State.CREATED
                }
            }
            nativeLib.pause(true)
            context.runOnUiThread {
                codePointView.setQrCodes(it)
            }
            onScanListener?.onResult(it)
        }

        codePointView.setCancelButtonListener {
            if (lifecycle is LifecycleRegistry) {
                lifecycle.currentState = Lifecycle.State.RESUMED
                nativeLib.pause(false)
            }
            onScanListener?.onCancel()
        }

        codePointView.setPointButtonListener {
            onScanListener?.onPointClick(it)
        }
    }


    fun release() {
        nativeLib.release()
        cameraView.release()
        codePointView.release()
    }


    fun setOnScanListener(onScanListener: OnScanListener?) {
        this.onScanListener = onScanListener
    }

    /**
     *
     * @param cancelText  扫码成功后取消按钮文字
     *
     */
    fun setCancelText(cancelText: String) {
        codePointView.cancelText = cancelText
    }

    /**
     *
     * @param color  扫码成功后取消按钮文字颜色
     *
     */
    fun setCancelColor(@ColorRes color: Int) {
        codePointView.cancelColor = color
    }

    /**
     *
     * @param cancelTextSize  扫码成功后取消按钮文字字体大小
     *
     */
    fun setCancelTextSize(cancelTextSize: Float) {
        codePointView.cancelTextSize = cancelTextSize
    }


    /**
     *
     * @param cancelTop 扫码成功后取消按钮上边距
     *
     */
    fun setCancelTop(cancelTop: Int) {
        codePointView.cancelTop = cancelTop
    }


    /**
     *
     * @param cancelLeft 扫码成功后取消按钮左边距
     *
     */
    fun setCancelLeft(cancelLeft: Int) {
        codePointView.cancelLeft = cancelLeft
    }


    /**
     *
     * @param pointViewRes 二维码标记点背景资源
     *
     */
    fun setPointViewRes(@DrawableRes pointViewRes: Int) {
        codePointView.pointViewRes = pointViewRes
    }


    /**
     *
     * @param size 二维码标记点大小
     *
     */
    fun setPointViewSize(size: Int) {
        codePointView.pointViewSize = size
    }

    /**
     * @param successColorRes 识别到二维码显示的背景色
     */
    fun setSuccessColorRes(successColorRes: Int) {
        codePointView.successColorRes = successColorRes
    }


}