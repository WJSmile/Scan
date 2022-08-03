//
// Created by Zwj on 2022/6/16.
//

#include "Distinguish.h"
#include "XLog.h"

void Distinguish::Main() {
    vector<CodeBean> qrCodes;
    while (!isExit) {
        mux.lock();
        if (data == nullptr || data->empty() || isPause) {
            mux.unlock();
            continue;
        }
        imageData = data->front();
        data->pop_front();
        mux.unlock();

        Mat src(imageData->height + imageData->height / 2,
                imageData->width, CV_8UC1,
                (uchar *) imageData->data);

        cvtColor(src, src, COLOR_YUV2GRAY_420);
        rotate(src, src, ROTATE_90_CLOCKWISE);


        getQrCode(src, qrCodes);

        getBarCode(src, qrCodes);

        if (javaCallHelper != nullptr) {

            javaCallHelper->callBackOnPoint(qrCodes);
        }

        qrCodes.clear();
        src.release();
    }
    signOut = true;
}


void Distinguish::setImageData(ImageData *image) {
    mux.lock();
    if (data != nullptr && data->empty() && !isExit) {
        data->push_back(image);
    }
    mux.unlock();
}

CodeBean Distinguish::scan(Mat &qrcode_mat) {
    int width = qrcode_mat.cols;
    int height = qrcode_mat.rows;
    auto *raw = (uchar *) qrcode_mat.data;
    CodeBean codeBean;
    Image imageZbar(width, height, "Y800", raw, width * height);
    imageScanner->scan(imageZbar); //扫描条码

    Image::SymbolIterator symbol = imageZbar.symbol_begin();

    for (; symbol != imageZbar.symbol_end(); ++symbol) {

        codeBean.code = symbol->get_data();
        codeBean.type = symbol->get_type();
    }
    imageZbar.set_data(nullptr, 0);
    return codeBean;
}

void Distinguish::getBarCode(Mat &src, vector<CodeBean> &codeBeans) {
    if (brcodeDetector == nullptr) {
        return;
    }
    Mat corners, result;
    if (brcodeDetector->detect(src, corners)) {
        for (int i = 0; i < corners.rows; ++i) {

            vector<Point2f> points(4);
            //topLeft
            points[0] = Point2f((float) corners.at<float>(i, 2),
                                (float) corners.at<float>(i, 3));

            //bottomLeft
            points[1] = Point2f((float) corners.at<float>(i, 0),
                                (float) corners.at<float>(i, 1));

            //bottomRight
            points[2] = Point2f((float) corners.at<float>(i, 6),
                                (float) corners.at<float>(i, 7));

            //topRight
            points[3] = Point2f((float) corners.at<float>(i, 4),
                                (float) corners.at<float>(i, 5));


            vector<Point2f> trans(4);
            trans[0] = Point2f(0, 0);
            trans[1] = Point2f(0, 200);
            trans[2] = Point2f(400, 200);
            trans[3] = Point2f(400, 0);
            Mat m = getPerspectiveTransform(points, trans);

            warpPerspective(src, result, m, Size(400, 200), INTER_LINEAR);

            CodeBean codeBean = scan(result);
            if (!codeBean.code.empty()) {
                RotatedRect rectPoint = minAreaRect(Mat(points));
                codeBean.center = rectPoint.center;
                codeBean.rect = rectPoint.boundingRect();
                codeBean.topLeft = points[0];
                codeBean.bottomLeft = points[1];
                codeBean.bottomRight = points[2];
                codeBean.topRight = points[3];
                codeBeans.push_back(codeBean);
            }
        }
    }
    result.release();
    corners.release();
}

void Distinguish::getQrCode(Mat &src, vector<CodeBean> &codeBeans) {
    if (detector == nullptr) {
        return;
    }
    vector<Mat> vPoints;
    vector<String> strDecoded;
    strDecoded = detector->detectAndDecode(src, vPoints);
    for (int i = 0; i < strDecoded.size(); ++i) {
        CodeBean codeBean;
        codeBean.topLeft = Point2f((float) vPoints[i].at<float>(0, 0),
                                   (float) vPoints[i].at<float>(0, 1));
        codeBean.bottomLeft = Point2f((float) vPoints[i].at<float>(3, 0),
                                      (float) vPoints[i].at<float>(3, 1));
        codeBean.bottomRight = Point2f((float) vPoints[i].at<float>(2, 0),
                                       (float) vPoints[i].at<float>(2, 1));
        codeBean.topRight = Point2f((float) vPoints[i].at<float>(1, 0),
                                    (float) vPoints[i].at<float>(1, 1));

        RotatedRect rectPoint = minAreaRect(Mat(vPoints[i]));
        codeBean.center = rectPoint.center;
        codeBean.rect = rectPoint.boundingRect();

        codeBean.code = strDecoded[i];
        codeBeans.push_back(codeBean);
    }
}

void Distinguish::release() {
    Stop();
    mux.lock();
    data->clear();
    mux.unlock();

    while (!signOut) {

    }
    delete data;
    data = nullptr;

    delete imageData;
    imageData = nullptr;

    delete imageScanner;
    imageScanner = nullptr;

    detector.release();
    detector = nullptr;

    brcodeDetector.release();
    detector = nullptr;

    javaCallHelper->release();
    delete javaCallHelper;
    javaCallHelper = nullptr;
}


Distinguish::Distinguish(const string &detect_prototxt, const string &detect_caffe_model,
                         const string &sr_prototxt,
                         const string &sr_caffe_model) {
    signOut = false;
    isPause = false;
    data = new list<ImageData *>();
    imageData = new ImageData();
    javaCallHelper = new JavaCallHelper();
    imageScanner = new ImageScanner();
    imageScanner->set_config(ZBAR_NONE, ZBAR_CFG_ENABLE, 1);

    detector = makePtr<wechat_qrcode::WeChatQRCode>(detect_prototxt, detect_caffe_model,
                                                    sr_prototxt, sr_caffe_model);
    brcodeDetector = makePtr<BarcodeDetector>(sr_prototxt, sr_caffe_model);
}

void Distinguish::pause(bool is_pause) {
    mux.lock();
    data->clear();
    isPause = is_pause;
    mux.unlock();
}







