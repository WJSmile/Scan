package com.palmpay.scan.bean;

public class CodeData {
    private int[] data;
    private int width;
    private int height;

    public CodeData(int[] data, int width, int height) {
        this.data = data;
        this.width = width;
        this.height = height;
    }

    public int[] getData() {
        return data;
    }

    public void setData(int[] data) {
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
