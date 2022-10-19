package com.palmpay.scan.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Size
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class CameraXView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val previewView: PreviewView

    private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private var imageAnalysis: ImageAnalysis? = null

    private val preview: Preview = Preview.Builder().build()

    private val cameraSelector: CameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()

    private val executorService: ExecutorService = Executors.newFixedThreadPool(2)

    private var camera: Camera? = null


    private var analyzerListener: ((ImageProxy) -> Unit)? = null

    init {
        previewView = PreviewView(context)
        addView(previewView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        post {
            imageAnalysis = ImageAnalysis.Builder()
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .setTargetResolution(Size(width, height))
                .build()

            imageAnalysis?.setAnalyzer(executorService) { imageProxy ->
                analyzerListener?.invoke(imageProxy)
            }
            initCamera()
        }
    }

    private fun initCamera() {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            preview.setSurfaceProvider(previewView.surfaceProvider)
            val useCaseGroup = previewView.viewPort?.let {viewPort->
                imageAnalysis?.let {
                    UseCaseGroup.Builder()
                        .addUseCase(preview)
                        .addUseCase(it)
                        .setViewPort(viewPort)
                        .build()
                }
            }

            camera = useCaseGroup?.let {
                cameraProvider.bindToLifecycle(
                    (context as FragmentActivity),
                    cameraSelector,
                    it
                )
            }
        }, ContextCompat.getMainExecutor(context))
    }


    fun setOnAnalyzerListener(action: ((ImageProxy) -> Unit)?) {
        this.analyzerListener = action
    }

    fun unbindAll() {
        cameraProviderFuture.get().unbindAll()
    }

}