package com.palmpay.scan.code;

import java.nio.ByteBuffer;

public class NativeLib {

    static {
        System.loadLibrary("scancode");
    }

    private long distinguishId = -1;

    public native void initScan(String detectProtoTxt, String detectCaffeModel, String srProtoTxt, String srCaffeModel);

    public native void setImageByte(byte[] bytes, int width, int height);

    public native void setImageYuvByte(ByteBuffer yBytes, ByteBuffer uBytes, ByteBuffer vBytes,
                                       int yRowStride, int yPixelStride,
                                       int uRowStride, int uPixelStride,
                                       int vRowStride, int vPixelStride,
                                       int width, int height);

    public native void start();

    public native void stop();

    public native void pause(boolean isPause);

    public native void setBitMapCallBack(BitMapCallBack bitMapCallBack);

    public native void setPointCallBack(PointCallBack pointCallBack);
}
