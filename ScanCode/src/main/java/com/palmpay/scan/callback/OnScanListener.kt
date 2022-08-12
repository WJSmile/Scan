package com.palmpay.scan.callback

import com.palmpay.scan.bean.CodeBean

interface OnScanListener {
    fun onCancel(){}

    fun onPointClick(codeBean: CodeBean){}

    fun onResult(result:List<CodeBean>)
}