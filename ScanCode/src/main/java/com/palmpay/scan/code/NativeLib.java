package com.palmpay.scan.code;


public class NativeLib {

    static {
        System.loadLibrary("scancode");
    }

    private long distinguishId = -1;

    public native void initScan(String detectProtoTxt, String detectCaffeModel, String srProtoTxt, String srCaffeModel);

    public native void setImageByte(byte[] bytes, int width, int height);

    public native void setImageByteForBox(byte[] bytes, int width, int height,int boxWidth,int boxTop);

    public native void release();

    public native void pause(boolean isPause);

    public native void setBitMapCallBack(BitMapCallBack bitMapCallBack);

    public native void setPointCallBack(PointCallBack pointCallBack);
}
