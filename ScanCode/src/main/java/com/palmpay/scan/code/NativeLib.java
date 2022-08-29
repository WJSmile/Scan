package com.palmpay.scan.code;


import com.palmpay.scan.bean.CodeBean;
import com.palmpay.scan.bean.CodeData;

import java.util.ArrayList;

public class NativeLib {

    static {
        System.loadLibrary("scancode");
    }

    private long distinguishId = -1;

    public native void initScan(String detectProtoTxt, String detectCaffeModel, String srProtoTxt, String srCaffeModel);

    public native void setSimpleMode(boolean isSimple);

    public native ArrayList<CodeBean> scanCode(byte[] bytes, int width, int height);

    public native ArrayList<CodeBean> scanCodeCut(byte[] bytes, int width, int height, int boxWidth, int boxTop);

    public native CodeData getCodeBitMap(String contents,
                                         int width,
                                         int height,
                                         String barcodeFormatName,
                                         String characterSetName,
                                         int level,
                                         int margin,
                                         int black,
                                         int white);

    public native void release();
}
