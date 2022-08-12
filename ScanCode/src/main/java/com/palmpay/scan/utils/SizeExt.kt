package com.palmpay.scan.utils

import android.content.Context

fun Int.toPx(context: Context): Float {
    val scale = context.resources.displayMetrics.density
    return  (this * scale + 0.5f)
}