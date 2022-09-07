package com.palmpay.scan.view

import android.content.Context
import android.hardware.Camera
import android.os.Looper
import android.util.AttributeSet
import android.view.SurfaceView
import android.widget.FrameLayout
import com.palmpay.scan.bean.CameraDataBean

class CameraView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) , Camera.PreviewCallback {

    private val surfaceView = SurfaceView(context)

    private var camera: Camera? = null


    private var analyzerListener: ((CameraDataBean) -> Unit)? = null

    init {
        addView(surfaceView)

        post {
            Thread{
                Looper.prepare()
                initCamera()
                Looper.loop()
            }.start()
        }

    }

    private fun initCamera() {
        camera = Camera.open()
        val parameters = camera?.parameters
        parameters?.setPictureSize(height, width)
        parameters?.setPreviewSize(height, width)

        parameters?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        camera?.parameters = parameters
        camera?.setDisplayOrientation(90)
        camera?.setPreviewCallback(this)
        camera?.setPreviewDisplay(surfaceView.holder)
        camera?.startPreview()
    }


    fun setOnAnalyzerListener(action: ((CameraDataBean) -> Unit)?) {
        this.analyzerListener = action
    }

    fun stop(){
        camera?.stopPreview()
    }

    fun release() {
        analyzerListener = null
        camera?.setPreviewCallback(null)
        camera?.release()
        camera = null
        surfaceView.destroyDrawingCache()
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        analyzerListener?.invoke(
            CameraDataBean(
                data, this.camera?.parameters?.pictureSize?.width ?: 0,
                this.camera?.parameters?.pictureSize?.height ?: 0
            )
        )
    }

}