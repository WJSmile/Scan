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
#include <src/MultiFormatReader.h>
#include <src/BinaryBitmap.h>
#include <src/Result.h>
#include <src/DecodeHints.h>
#include <src/ReadBarcode.h>
#include <opencv2/barcode.hpp>
#include <opencv2/wechat_qrcode.hpp>

using namespace zbar;
using namespace cv;
using namespace std;
using namespace barcode;
using namespace ZXing;

class Distinguish : public XThread {
public:
    Distinguish(const string &detect_prototxt, const string &detect_caffe_model,
                const string &sr_prototxt, const string &sr_caffe_model);



    virtual void setImageData(ImageData *imageData);


    virtual CodeBean scan(Mat &qrcode_mat);

    virtual void zxingScan(Mat &qrcode_mat, vector<CodeBean> &codeBeans);

    virtual void getQrCode(Mat &src, vector<CodeBean> &codeBeans);

    virtual void getBarCode(Mat &src, vector<CodeBean> &codeBeans);

    void Main();

    virtual void pause(bool is_pause);

    bool isPause = false;
    list<ImageData *> *data;
    ImageData *imageData;
    Ptr<DecodeHints> hints;
    ImageScanner *imageScanner;
    Ptr<wechat_qrcode::WeChatQRCode> detector;
    Ptr<BarcodeDetector> brcodeDetector;
    std::mutex mux;
    JavaCallHelper *javaCallHelper;

    virtual void release(JNIEnv *env);

    bool signOut = false;


};


#endif //SCAN_DISTINGUISH_H
