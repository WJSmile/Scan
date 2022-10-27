package com.palmpay.scan.code;

import java.nio.ByteBuffer;

public class YuvUtils {
    static {
        System.loadLibrary("scancode");
    }

    public native void rotation(byte[] srcYuvData,
                                byte[] dstYuvData,
                                int width,
                                int height, int type);

    public native void clipping(byte[] srcYuvData,
                                byte[] dstYuvData,
                                int width,
                                int height,
                                int cropX,
                                int cropY,
                                int cropWidth,
                                int cropHeight);

    public native void scale(byte[] srcYuvData,
                             byte[] dstYuvData,
                             int width,
                             int height, int dstWidth,
                             int dstHeight);

    public native void scaleAndclipping(byte[] srcYuvData,
                                        byte[] dstYuvData,
                                        int width,
                                        int height,
                                        int cropX,
                                        int cropY,
                                        int cropWidth,
                                        int cropHeight,
                                        int dstWidth,
                                        int dstHeight);


    public native void yuvI420ToNV21(byte[] srcYuvData,
                                     byte[] dstYuvData,
                                     int width,
                                     int height);

    public native void nV21ToI420(byte[] srcYuvData,
                                  byte[] dstYuvData,
                                  int width,
                                  int height);

    public native void Android420ToI420(ByteBuffer y, ByteBuffer u, ByteBuffer v,
                                        int yStride,
                                        int uStride,
                                        int vStride,
                                        int srcPixelStrideUv,
                                        byte[] dstYuvData,
                                        int width,
                                        int height);
}
