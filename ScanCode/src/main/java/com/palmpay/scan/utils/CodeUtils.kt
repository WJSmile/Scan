package com.palmpay.scan.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.palmpay.scan.bean.CodeBean
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
                return Bitmap.createBitmap(
                    codeData.data,
                    codeData.width,
                    codeData.height,
                    Bitmap.Config.ARGB_8888
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return null
    }

    fun createFormLogo(
        text: String,
        width: Int,
        height: Int,
        logo: Bitmap,
        logoWidth: Int,
        logoHeight: Int,
        characterSet: String = "UTF-8",
        level: Int = -1,
        margin: Int = 0,
        black: Int = Color.BLACK,
        white: Int = Color.WHITE,
    ): Bitmap? {
        val bitmap =
            create(
                text,
                width,
                height,
                ScanCodeType.QRCode,
                characterSet,
                level,
                margin,
                black,
                white
            )
        if (bitmap != null) {
            BitmapUtil.addLogoInQRCode(bitmap, logo, logoWidth, logoHeight)
        }
        return null
    }

    fun findImageCode(context: Context, bitmap: Bitmap): List<CodeBean>? {
        val nativeLib = NativeLib()
        val paths = Utils.copyWeChatModel(context)
        if (paths.isNotEmpty()) {
            nativeLib.initScan(
                paths[0],
                paths[1],
                paths[2],
                paths[3]
            )
        }
        nativeLib.setSimpleMode(false)
        val copyBitMap = bitmap.copy(Bitmap.Config.RGB_565, false)
        val codeBeans = nativeLib.scanCodeFormBitMap(copyBitMap)
        copyBitMap.recycle()
        nativeLib.release()
        return codeBeans
    }
}