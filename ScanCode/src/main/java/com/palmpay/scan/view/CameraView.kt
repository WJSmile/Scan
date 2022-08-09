package com.palmpay.scan.view

import android.content.Context
import android.util.AttributeSet
import android.util.Size
import android.widget.FrameLayout
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val previewView: PreviewView

    private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private val imageAnalysis: ImageAnalysis

    private val preview: Preview = Preview.Builder().build()

    private val cameraSelector: CameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()

    private val executorService: ExecutorService = Executors.newFixedThreadPool(2)

    private var camera: Camera? = null

    private var screenWidth = 0
    private var screenHeight = 0


    private var analyzerListener: ((ImageProxy) -> Unit)? = null

    init {
        screenWidth = context.resources.displayMetrics.widthPixels
        screenHeight = context.resources.displayMetrics.heightPixels
        previewView = PreviewView(context)
        addView(previewView, LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT))

        cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        imageAnalysis = ImageAnalysis.Builder()
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .setTargetResolution(Size(screenWidth, screenHeight))
            .build()

        imageAnalysis.setAnalyzer(executorService) { imageProxy ->
            analyzerListener?.invoke(imageProxy)
        }
        initCamera()
    }

    private fun initCamera() {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            preview.setSurfaceProvider(previewView.surfaceProvider)


            camera = cameraProvider.bindToLifecycle(
                (context as FragmentActivity),
                cameraSelector,
                imageAnalysis,
                preview
            )

        }, ContextCompat.getMainExecutor(context))
    }


    fun setOnAnalyzerListener(action: (ImageProxy) -> Unit) {
        this.analyzerListener = action
    }

    fun release(){
        cameraProviderFuture.get().unbindAll()
    }

}