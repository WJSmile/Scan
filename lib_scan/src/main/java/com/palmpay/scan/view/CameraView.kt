package com.palmpay.scan.view

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.hardware.Camera
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import android.widget.FrameLayout
import android.widget.ImageView
import com.palmpay.scan.bean.CameraDataBean
import com.palmpay.scan.code.YuvUtils
import com.palmpay.scan.utils.Utils
import kotlin.math.abs

class CameraView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), Camera.PreviewCallback {

    private val surfaceView = SurfaceView(context)

    private var camera: Camera? = null

    private var surfaceMatrix: Matrix? = null

    private var analyzerListener: ((CameraDataBean) -> Unit)? = null

    private val yuvUtils = YuvUtils()

    init {
        addView(surfaceView)
        post {
            Thread {
                Looper.prepare()
                initCamera()
                Looper.loop()
            }.start()
        }

    }

    private fun initCamera() {
        camera = Camera.open()
        val parameters = camera?.parameters
        setCameraSize(camera, width.toFloat(), height.toFloat())?.let {
            parameters?.setPictureSize(it.width, it.height)
            parameters?.setPreviewSize(it.width, it.height)
        }

        parameters?.supportedPreviewFormats?.forEach {
            if (ImageFormat.NV21 == it) {
                parameters.previewFormat = ImageFormat.NV21
            }
        }

        parameters?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        camera?.parameters = parameters
        setCameraDisplayOrientation(camera)
        camera?.setPreviewCallback(this)
        camera?.setPreviewDisplay(surfaceView.holder)
        surfaceMatrix = calculateSurfaceHolderTransform()
        (context as Activity).runOnUiThread {
            val values = FloatArray(9)
            surfaceMatrix?.getValues(values)
            surfaceView.scaleX = values[Matrix.MSCALE_X]
            surfaceView.scaleY = values[Matrix.MSCALE_Y]
            surfaceView.invalidate()
        }
        camera?.startPreview()
    }


    private fun setCameraDisplayOrientation(camera: Camera?) {
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info)
        val rotation = (context as Activity).windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360
        } else {
            result = (info.orientation - degrees + 360) % 360
        }
        camera?.setDisplayOrientation(result)
    }

    fun setOnAnalyzerListener(action: ((CameraDataBean) -> Unit)?) {
        this.analyzerListener = action
    }

    fun stop() {
        camera?.stopPreview()
    }

    fun start() {
        camera?.startPreview()
        camera?.setPreviewCallback(this)
    }

    fun release() {
        analyzerListener = null
        camera?.setPreviewCallback(null)
        camera?.release()
        camera = null
        surfaceView.destroyDrawingCache()
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        data?.let {
            analyzerListener?.invoke(
                CameraDataBean(
                    getClippingBiteMap(data), height,
                    width
                )
            )
        }
    }

    private fun getClippingBiteMap(data: ByteArray): ByteArray {

        val values = FloatArray(9)
        surfaceMatrix?.getValues(values)

        val w = (width * values[Matrix.MSCALE_X]).toInt()
        val h = (height * values[Matrix.MSCALE_Y]).toInt()

        val byteW = this.camera?.parameters?.pictureSize?.width ?: 0
        val byteH = this.camera?.parameters?.pictureSize?.height ?: 0

        val scaleYuvByte = ByteArray(w * h * 3 / 2)


        val clippingYuvByte = ByteArray(width * height * 3 / 2)

        yuvUtils.scale(data, scaleYuvByte, byteW, byteH, h, w)


        yuvUtils.clipping(
            scaleYuvByte,
            clippingYuvByte,
            h, w,
            abs(values[Matrix.MTRANS_X]).toInt(),
            abs(values[Matrix.MTRANS_Y]).toInt(),
            height,
            width
        )
        return clippingYuvByte
    }

    private fun setCameraSize(camera: Camera?, needW: Float, needH: Float): Camera.Size? {

        if (camera == null) {
            return null
        }
        val parameters = camera.parameters
        val list = parameters.supportedPreviewSizes

        val needRatio = needW / needH

        val map: LinkedHashMap<Float, Camera.Size> = LinkedHashMap()
        var bestRatio = 0f
        for (size in list) {

            if (!map.containsKey(size.height.toFloat() / size.width)) {
                map[size.height.toFloat() / size.width] = size
            }
            if (bestRatio == 0f || abs(needRatio - size.height.toFloat() / size.width) < abs(
                    needRatio - bestRatio
                )
            ) {
                bestRatio = size.height.toFloat() / size.width
            }
        }
        return map[bestRatio]
    }


    private fun calculateSurfaceHolderTransform(): Matrix {
        // 预览 View 的大小，比如 SurfaceView
        val viewHeight: Int = height
        val viewWidth: Int = width
        // 相机选择的预览尺寸
        val cameraHeight: Int = camera?.parameters?.previewSize?.width ?: 0
        val cameraWidth: Int = camera?.parameters?.previewSize?.height ?: 0
        // 计算出将相机的尺寸 => View 的尺寸需要的缩放倍数
        val ratioPreview = cameraWidth.toFloat() / cameraHeight
        val ratioView = viewWidth.toFloat() / viewHeight
        val scaleX: Float
        val scaleY: Float
        if (ratioView < ratioPreview) {
            scaleX = ratioPreview / ratioView
            scaleY = 1f
        } else {
            scaleX = 1f
            scaleY = ratioView / ratioPreview
        }
        // 计算出 View 的偏移量
        val scaledWidth = viewWidth * scaleX
        val scaledHeight = viewHeight * scaleY
        val dx = (viewWidth - scaledWidth) / 2
        val dy = (viewHeight - scaledHeight) / 2
        val matrix = Matrix()
        matrix.postScale(scaleX, scaleY)
        matrix.postTranslate(dx, dy)
        return matrix
    }
}