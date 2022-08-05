//
// Created by Zwj on 2022/6/16.
//

#ifndef SCAN_DISTINGUISH_H
#define SCAN_DISTINGUISH_H

#include <list>
#include "ImageData.h"
#include "XThread.h"
#include "JavaCallHelper.h"
#include <zbar/zbar.h>
#include <opencv2/barcode.hpp>
#include <opencv2/wechat_qrcode.hpp>

using namespace zbar;
using namespace cv;
using namespace std;
using namespace barcode;

class Distinguish : public XThread {
public:
    Distinguish(const string &detect_prototxt, const string &detect_caffe_model,
                const string &sr_prototxt, const string &sr_caffe_model);

    virtual jbyte *yuvToNV21(jbyteArray yBuf, jbyteArray uBuf, jbyteArray vBuf,
                             int width, int height, int yRowStride, int yPixelStride,
                             int uRowStride,
                             int uPixelStride, int vRowStride, int vPixelStride, JNIEnv *env);

    virtual void setImageData(ImageData *imageData);


    virtual jbyteArray getByteArray(JNIEnv *env, jobject buffer);

    virtual CodeBean scan(Mat &qrcode_mat);

    virtual void getQrCode(Mat &src, vector<CodeBean> &codeBeans);

    virtual void getBarCode(Mat &src, vector<CodeBean> &codeBeans);

    void Main();

    virtual void pause(bool is_pause);

    bool isPause = false;
    list<ImageData *> *data;
    ImageData *imageData;
    ImageScanner *imageScanner;
    Ptr<wechat_qrcode::WeChatQRCode> detector;
    Ptr<BarcodeDetector> brcodeDetector;
    std::mutex mux;
    JavaCallHelper *javaCallHelper;

    virtual void release();

    bool signOut = false;


};


#endif //SCAN_DISTINGUISH_H
