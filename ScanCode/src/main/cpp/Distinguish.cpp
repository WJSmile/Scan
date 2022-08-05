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
            //   javaCallHelper->callBackBitMap(src);
        }

        qrCodes.clear();
        src.release();
        delete imageData->data;
        delete imageData;
    }
    signOut = true;
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

jbyte *
Distinguish::yuvToNV21(jbyteArray yBuf, jbyteArray uBuf, jbyteArray vBuf,
                       int width, int height, int yRowStride, int yPixelStride, int uRowStride,
                       int uPixelStride, int vRowStride, int vPixelStride, JNIEnv *env) {

    /* Check that our frame has right format, as specified at android docs for
     * YUV_420_888 (https://developer.android.com/reference/android/graphics/ImageFormat?authuser=2#YUV_420_888):
     *      - Plane Y not overlaped with UV, and always with pixelStride = 1
     *      - Planes U and V have the same rowStride and pixelStride (overlaped or not)
     */
    if (yPixelStride != 1 || uPixelStride != vPixelStride || uRowStride != vRowStride) {
        jclass Exception = env->FindClass("java/lang/Exception");
        env->ThrowNew(Exception,
                      "Invalid YUV_420_888 byte structure. Not agree with https://developer.android.com/reference/android/graphics/ImageFormat?authuser=2#YUV_420_888");
    }
    jbyte *fullArrayNV21;
    int ySize = width * height;
    int uSize = env->GetArrayLength(uBuf);
    int vSize = env->GetArrayLength(vBuf);
    int newArrayPosition = 0; //Posicion por la que vamos rellenando el array NV21

    fullArrayNV21 = new jbyte[ySize + uSize + vSize];

    if (yRowStride == width) {
        //Best case. No padding, copy direct
        env->GetByteArrayRegion(yBuf, newArrayPosition, ySize, fullArrayNV21);
        newArrayPosition = ySize;
    } else {
        // Padding at plane Y. Copy Row by Row
        long yPlanePosition = 0;
        for (; newArrayPosition < ySize; newArrayPosition += width) {
            env->GetByteArrayRegion(yBuf, yPlanePosition, width, fullArrayNV21 + newArrayPosition);
            yPlanePosition += yRowStride;
        }
    }

    // Check UV channels in order to know if they are overlapped (best case)
    // If they are overlapped, U and B first bytes are consecutives and pixelStride = 2
    long uMemoryAdd = (long) &uBuf;
    long vMemoryAdd = (long) &vBuf;
    long diff = std::abs(uMemoryAdd - vMemoryAdd);
    if (vPixelStride == 2 && diff == 8) {
        if (width == vRowStride) {
            // Best Case: Valid NV21 representation (UV overlapped, no padding). Copy direct
            env->GetByteArrayRegion(uBuf, 0, uSize, fullArrayNV21 + ySize);
            env->GetByteArrayRegion(vBuf, 0, vSize, fullArrayNV21 + ySize + uSize);
        } else {
            // UV overlapped, but with padding. Copy row by row (too much performance improvement compared with copy byte-by-byte)
            int limit = height / 2 - 1;
            for (int row = 0; row < limit; row++) {
                env->GetByteArrayRegion(uBuf, row * vRowStride, width,
                                        fullArrayNV21 + ySize + (row * width));
            }
        }
    } else {
        //WORST: not overlapped UV. Copy byte by byte
        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width / 2; col++) {
                int vuPos = col * uPixelStride + row * uRowStride;
                env->GetByteArrayRegion(vBuf, vuPos, 1, fullArrayNV21 + newArrayPosition);
                newArrayPosition++;
                env->GetByteArrayRegion(uBuf, vuPos, 1, fullArrayNV21 + newArrayPosition);
                newArrayPosition++;
            }
        }
    }
    env->DeleteLocalRef(yBuf);
    env->DeleteLocalRef(uBuf);
    env->DeleteLocalRef(vBuf);
    return fullArrayNV21;
}


void Distinguish::setImageData(ImageData *image) {
    mux.lock();
    if (data != nullptr && data->empty() && !isExit) {
        data->push_back(image);
    } else {
        delete image->data;
        delete image;
    }
    mux.unlock();
}



jbyteArray Distinguish::getByteArray(JNIEnv *env, jobject buffer) {
    auto *pData = (jbyte *) env->GetDirectBufferAddress(buffer);   //获取buffer数据首地址
    jlong dwCapacity = env->GetDirectBufferCapacity(buffer);         //获取buffer的容量
    if (!pData) {
        XLOGE("GetDirectBufferAddress() return null");
        return nullptr;
    }
    jbyteArray data = env->NewByteArray(dwCapacity);                  //创建与buffer容量一样的byte[]
    env->SetByteArrayRegion(data, 0, dwCapacity, pData);              //数据拷贝到data中
    return data;
}











