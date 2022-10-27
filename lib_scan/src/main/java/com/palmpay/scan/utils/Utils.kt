package com.palmpay.scan.utils

import android.content.Context
import android.graphics.*
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageProxy
import com.palmpay.scan.code.YuvUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ReadOnlyBufferException
import kotlin.experimental.inv


object Utils {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun android420ToI420(image: ImageProxy): ByteArray {

        val nv21 = ByteArray(image.width * image.height * 3 / 2)

        YuvUtils().Android420ToI420(
            image.planes[0].buffer,
            image.planes[1].buffer,
            image.planes[2].buffer,
            image.planes[0].rowStride,
            image.planes[1].rowStride,
            image.planes[2].rowStride,
            image.planes[2].pixelStride,
            nv21,
            image.width,
            image.height
        )

        return nv21
    }
    fun bitMapToNv21(scaled: Bitmap): ByteArray {
        val argb = IntArray(scaled.width * scaled.height)

        scaled.getPixels(argb, 0, scaled.width, 0, 0, scaled.width, scaled.height)

        val yuv = ByteArray(scaled.width * scaled.height * 3 / 2)
        encodeYUV420SP(yuv, argb, scaled.width, scaled.height)

        scaled.recycle()
        return yuv
    }

    private fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        var yIndex = 0
        var uvIndex = width * height
        var R: Int
        var G: Int
        var B: Int
        var Y: Int
        var U: Int
        var V: Int
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                R = argb[index] and -0x1000000 ushr 24
                G = argb[index] and 0xff0000 shr 16
                B = argb[index] and 0xff00 shr 8

                // well known RGB to YUV algorithm
                Y = (66 * R + 129 * G + 25 * B + 128 shr 8) + 16
                U = (-38 * R - 74 * G + 112 * B + 128 shr 8) + 128
                V = (112 * R - 94 * G - 18 * B + 128 shr 8) + 128

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (if (Y < 0) 0 else if (Y > 255) 255 else Y).toByte()
                if (j % 2 == 0 && index % 2 == 0 && uvIndex < yuv420sp.size - 2) {
                    yuv420sp[uvIndex++] = (if (V < 0) 0 else if (V > 255) 255 else V).toByte()
                    yuv420sp[uvIndex++] = (if (U < 0) 0 else if (U > 255) 255 else U).toByte()
                }
                index++
            }
        }
    }


    fun copyWeChatModel(context: Context): List<String> {
        val pathList: MutableList<String> = mutableListOf()
        try {
            val modelDir = "models"
            val models = context.assets.list(modelDir)
            val saveDirPath: String = context.cacheDir.absolutePath + "/scan"
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

    fun byteArrayToBitMap(data: ByteArray?, width: Int, height: Int): Bitmap {
        val newOpts = BitmapFactory.Options()
        newOpts.inJustDecodeBounds = true
        val yuvImage = YuvImage(
            data,
            ImageFormat.NV21,
            width,
            height,
            null
        )
        val bao = ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            Rect(0, 0, width, height),
            100,
            bao
        )
        val rawImage: ByteArray = bao.toByteArray()
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        return BitmapFactory.decodeByteArray(rawImage, 0, rawImage.size, options)
    }


}