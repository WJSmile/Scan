package com.palmpay.scan.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.palmpay.scan.bean.ScanCodeType
import com.palmpay.scan.code.NativeLib

object CodeUtils {
    private val nativeLib = NativeLib()

    fun create(
        text: String,
        width: Int,
        height: Int,
        barcodeFormat: ScanCodeType = ScanCodeType.QRCODE,
        characterSet: String = "UTF-8",
        level: Int = -1,
        margin: Int = 0,
        black: Int = Color.BLACK,
        white: Int = Color.WHITE
    ): Bitmap? {
        try {
            val array = nativeLib.getCodeBitMap(
                text,
                width,
                height,
                barcodeFormat.name,
                characterSet,
                level,
                margin,
                black,
                white
            )
            if (array != null) {
                return Bitmap.createBitmap(array, width, height, Bitmap.Config.ARGB_8888)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return null
    }


}