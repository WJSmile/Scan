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

    private val imageView: ImageView

    init {
        cameraView = CameraView(context)
        addView(cameraView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        imageView = ImageView(context)

        addView(imageView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        codePointView = CodePointView(context)
        addView(codePointView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        initWeChatModel()

        cameraView.setOnAnalyzerListener {
            nativeLib.setImageYuvByte(
                it.planes[0].buffer,
                it.planes[1].buffer,
                it.planes[2].buffer,
                it.planes[0].rowStride,
                it.planes[0].pixelStride,
                it.planes[1].rowStride,
                it.planes[1].pixelStride,
                it.planes[2].rowStride,
                it.planes[2].pixelStride,
                it.width,
                it.height
            )
            it.close()
        }

        nativeLib.setBitMapCallBack {
            (context as FragmentActivity).runOnUiThread {
                imageView.setImageBitmap(it)
            }
        }

        nativeLib.setPointCallBack {
            if (it.isNotEmpty()) {
                val lifecycle: Lifecycle = (context as FragmentActivity).lifecycle
                if (lifecycle is LifecycleRegistry) {
                    context.runOnUiThread {
                        lifecycle.addObserver(CameraLifecycleObserver())
                        lifecycle.currentState = Lifecycle.State.CREATED
                    }
                }
            }
            codePointView.setQrCodes(it)
        }
    }

    inner class CameraLifecycleObserver : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onCreated() {
            nativeLib.pause(true)
        }
    }


    fun start() {
        nativeLib.start()
    }


    fun stop() {
        nativeLib.stop()
    }


    /**
     *将模型文件复制到sd卡中，并初始化低层解码库
     */
    private fun initWeChatModel() {
        try {
            val modelDir = "models"
            val models = context.assets.list(modelDir)
            val saveDirPath: String = getExternalFilesDir(context, modelDir)
            val saveDir = File(saveDirPath)
            if (!saveDir.exists()) {
                saveDir.mkdirs()
            }
            val detect = File(saveDirPath, "detect.prototxt")
            val detectModel = File(saveDirPath, "detect.caffemodel")
            val resolution = File(saveDirPath, "sr.prototxt")
            val resolutionModel = File(saveDirPath, "sr.caffemodel")
            if (!(detect.exists() && detectModel.exists() && resolution.exists() && resolutionModel.exists())) {
                //模型文件只要有一个不存在，则遍历拷贝
                for (model in models!!) {
                    val inputStream = context.assets.open("$modelDir/$model")
                    val saveFile = File(saveDir, model)
                    val outputStream = FileOutputStream(saveFile)
                    val buffer = ByteArray(1024)
                    var len: Int
                    while (inputStream.read(buffer).also { len = it } != -1) {
                        outputStream.write(buffer, 0, len)
                    }
                    outputStream.flush()
                    inputStream.close()
                    outputStream.close()

                }
            }
            nativeLib.initScan(
                detect.absolutePath,
                detectModel.absolutePath,
                resolution.absolutePath,
                resolutionModel.absolutePath
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getExternalFilesDir(context: Context, path: String): String {
        val files = context.getExternalFilesDirs(path)
        return if (files != null && files.isNotEmpty()) {
            files[0].absolutePath
        } else context.getExternalFilesDir(path)!!.absolutePath
    }

}