package com.palmpay.scan.code;


import com.palmpay.scan.bean.CodeBean;

import java.util.ArrayList;

public interface PointCallBack {
    void onPoint(ArrayList<CodeBean> qrCodeBean);
}
