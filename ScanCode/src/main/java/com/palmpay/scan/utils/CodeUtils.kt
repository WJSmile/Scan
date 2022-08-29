package com.palmpay.scan.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.palmpay.scan.bean.ScanCodeType
import com.palmpay.scan.code.NativeLib

object CodeUtils {
    private val nativeLib = NativeLib()

    fun create(
        text: String,
        width: Int,
        height: Int,
        barcodeFormat: ScanCodeType = ScanCodeType.QRCode,
        characterSet: String = "UTF-8",
        level: Int = -1,
        margin: Int = 0,
        black: Int = Color.BLACK,
        white: Int = Color.WHITE
    ): Bitmap? {
        try {
            val codeData = nativeLib.getCodeBitMap(
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
            if (codeData != null) {
                return Bitmap.createBitmap(codeData.data, codeData.width, codeData.height, Bitmap.Config.ARGB_8888)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return null
    }


}