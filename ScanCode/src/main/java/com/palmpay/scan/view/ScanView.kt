package com.palmpay.scan.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.OnLifecycleEvent
import com.palmpay.scan.bean.CodeBean
import com.palmpay.scan.code.NativeLib
import com.palmpay.scan.utils.Utils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ScanView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val cameraView: CameraView
    private val nativeLib: NativeLib = NativeLib()
    private val codePointView: CodePointView

    private var onCodeResultListener: ((List<CodeBean>) -> Unit)? = null

    init {
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
            val lifecycle: Lifecycle = (context as FragmentActivity).lifecycle
            if (lifecycle is LifecycleRegistry) {
                context.runOnUiThread {
                    lifecycle.currentState = Lifecycle.State.CREATED
                }
            }
            nativeLib.pause(true)
            codePointView.setQrCodes(it)
            onCodeResultListener?.invoke(it)
        }
    }

    fun setOnCodeResultListener(onCodeResultListener: ((List<CodeBean>) -> Unit)?) {
        this.onCodeResultListener = onCodeResultListener
    }

    fun release() {
        nativeLib.release()
        cameraView.release()
    }

}