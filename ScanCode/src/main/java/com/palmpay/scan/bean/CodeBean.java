package com.palmpay.scan.bean;

import android.graphics.PointF;
import android.graphics.Rect;

public class CodeBean {
    private Rect rect;
    private byte[] code;
    private int type;
    public PointF topLeft;
    public PointF bottomLeft;
    public PointF bottomRight;
    public PointF topRight;
    public PointF center;

    public CodeBean(Rect rect, byte[] code, int type, PointF topLeft, PointF bottomLeft, PointF bottomRight, PointF topRight, PointF center) {
        this.rect = rect;
        this.code = code;
        this.type = type;
        this.topLeft = topLeft;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
        this.topRight = topRight;
        this.center = center;
    }

    public PointF getCenter() {
        return center;
    }

    public void setCenter(PointF center) {
        this.center = center;
    }

    public PointF getTopLeft() {
        return topLeft;
    }

    public void setTopLeft(PointF topLeft) {
        this.topLeft = topLeft;
    }

    public PointF getBottomLeft() {
        return bottomLeft;
    }

    public void setBottomLeft(PointF bottomLeft) {
        this.bottomLeft = bottomLeft;
    }

    public PointF getBottomRight() {
        return bottomRight;
    }

    public void setBottomRight(PointF bottomRight) {
        this.bottomRight = bottomRight;
    }

    public PointF getTopRight() {
        return topRight;
    }

    public void setTopRight(PointF topRight) {
        this.topRight = topRight;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public byte[] getCode() {
        return code;
    }

    public void setCode(byte[] code) {
        this.code = code;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCodeString() {
        if (code == null) {
            return "";
        }
        return new String(code);
    }
}
