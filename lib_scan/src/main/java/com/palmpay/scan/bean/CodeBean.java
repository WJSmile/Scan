package com.palmpay.scan.bean;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

public class CodeBean {
    private RectF rect;
    private byte[] code;
    private ScanCodeType scanCodeType;
    public PointF topLeft;
    public PointF bottomLeft;
    public PointF bottomRight;
    public PointF topRight;
    public PointF center;

    public CodeBean(RectF rect, byte[] code, String type, PointF topLeft, PointF bottomLeft, PointF bottomRight, PointF topRight, PointF center) {
        this.rect = rect;
        this.code = code;
        this.scanCodeType = ScanCodeType.valueOf(type);
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

    public RectF getRect() {
        return rect;
    }

    public void setRect(RectF rect) {
        this.rect = rect;
    }

    public byte[] getCode() {
        return code;
    }

    public void setCode(byte[] code) {
        this.code = code;
    }

    public ScanCodeType getScanCodeType() {
        return scanCodeType;
    }

    public void setScanCodeType(ScanCodeType scanCodeType) {
        this.scanCodeType = scanCodeType;
    }

    public String getCodeString() {
        if (code == null) {
            return "";
        }
        return new String(code);
    }
}
