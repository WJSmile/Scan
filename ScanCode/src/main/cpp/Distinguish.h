//
// Created by Zwj on 2022/6/16.
//

#ifndef SCAN_DISTINGUISH_H
#define SCAN_DISTINGUISH_H

#include "JavaCallHelper.h"
#include <zbar.h>
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

class Distinguish {
public:
    Distinguish(const string &detect_prototxt, const string &detect_caffe_model,
                const string &sr_prototxt, const string &sr_caffe_model);

    virtual jobject decode(JNIEnv *env, Mat &src);

    virtual CodeBean zbarScan(Mat &qrcode_mat);

    virtual void zxingScan(Mat &qrcode_mat, vector<CodeBean> &codeBeans);

    virtual void getQrCode(Mat &src, vector<CodeBean> &codeBeans);

    virtual void getBarCode(Mat &src, vector<CodeBean> &codeBeans);

    virtual ZXing::ImageView ImageViewFromMat(const cv::Mat &image);

    virtual string getCodeTypeString(zbar_symbol_type_e zbarType,ZXing::BarcodeFormat barcodeFormat);

    virtual void release();

    bool simpleMode = true;


private:
    Ptr<ZXing::DecodeHints> hints;
    ImageScanner *imageScanner;
    Ptr<wechat_qrcode::WeChatQRCode> detector;
    Ptr<BarcodeDetector> brcodeDetector;
    vector<CodeBean> *qrCodes;
    JavaCallHelper *javaCallHelper;
    std::mutex mux;


};


#endif //SCAN_DISTINGUISH_H
