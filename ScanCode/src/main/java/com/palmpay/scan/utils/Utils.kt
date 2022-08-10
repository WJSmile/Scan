package com.palmpay.scan.utils

import android.content.Context
import androidx.camera.core.ImageProxy
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ReadOnlyBufferException
import kotlin.experimental.inv

object Utils {

    fun yuv420888ToNv21(image: ImageProxy): ByteArray? {
        val width = image.width
        val height = image.height
        val ySize = width * height
        val uvSize = width * height / 4
        val nv21 = ByteArray(ySize + uvSize * 2)
        val yBuffer = image.planes[0].buffer // Y
        val uBuffer = image.planes[1].buffer // U
        val vBuffer = image.planes[2].buffer // V
        var rowStride = image.planes[0].rowStride
        assert(image.planes[0].pixelStride == 1)
        var pos = 0
        if (rowStride == width) { // likely
            yBuffer[nv21, 0, ySize]
            pos += ySize
        } else {
            var yBufferPos = -rowStride // not an actual position
            while (pos < ySize) {
                yBufferPos += rowStride
                yBuffer.position(yBufferPos)
                yBuffer[nv21, pos, width]
                pos += width
            }
        }
        rowStride = image.planes[2].rowStride
        val pixelStride = image.planes[2].pixelStride
        assert(rowStride == image.planes[1].rowStride)
        assert(pixelStride == image.planes[1].pixelStride)
        if (pixelStride == 2 && rowStride == width && uBuffer[0] == vBuffer[1]) {
            // maybe V an U planes overlap as per NV21, which means vBuffer[1] is alias of uBuffer[0]
            val savePixel = vBuffer[1]
            try {
                vBuffer.put(1, savePixel.inv())
                if (uBuffer[0] == savePixel.inv()) {
                    vBuffer.put(1, savePixel)
                    vBuffer.position(0)
                    uBuffer.position(0)
                    vBuffer[nv21, ySize, 1]
                    uBuffer[nv21, ySize + 1, uBuffer.remaining()]
                    return nv21 // shortcut
                }
            } catch (ex: ReadOnlyBufferException) {
                ex.printStackTrace()
            }
            vBuffer.put(1, savePixel)
        }


        for (row in 0 until height / 2) {
            for (col in 0 until width / 2) {
                val vuPos = col * pixelStride + row * rowStride
                nv21[pos++] = vBuffer[vuPos]
                nv21[pos++] = uBuffer[vuPos]
            }
        }
        return nv21
    }

    private fun getExternalFilesDir(context: Context, path: String): String {
        val files = context.getExternalFilesDirs(path)
        return if (files != null && files.isNotEmpty()) {
            files[0].absolutePath
        } else context.getExternalFilesDir(path)!!.absolutePath
    }

    fun copyWeChatModel(context: Context): List<String> {
        val pathList: MutableList<String> = mutableListOf()
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
            pathList.add(detect.absolutePath)
            pathList.add(detectModel.absolutePath)
            pathList.add(resolution.absolutePath)
            pathList.add(resolutionModel.absolutePath)
            return pathList
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return pathList
    }
}