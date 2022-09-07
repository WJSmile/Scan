package com.palmpay.scan.bean;

public class CameraDataBean {
    private byte[] data;
    private int width;
    private int height;

    public CameraDataBean(byte[] data, int width, int height) {
        this.data = data;
        this.width = width;
        this.height = height;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
