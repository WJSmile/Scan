//
// Created by Zwj on 2022/7/12.
//

#ifndef SCAN_JAVACALLHELPER_H
#define SCAN_JAVACALLHELPER_H

#include "opencv2/opencv.hpp"

#include <jni.h>

struct CodeBean {
    int type;
    cv::Point2f topLeft;
    cv::Point2f bottomLeft;
    cv::Point2f bottomRight;
    cv::Point2f topRight;
    cv::Point2f center;
    cv::Rect rect;
    std::string code;
};

class JavaCallHelper {
public:
    JavaCallHelper();
    virtual void callBackBitMap(cv::Mat &mat);

    virtual void callBackOnPoint(std::vector<CodeBean> &qrCodeBean);
    virtual jstring stringToUtf8(const char *   filename);

    virtual void release( JNIEnv *env);

    jmethodID javaCallbackId;
    jobject callback;
    JavaVM *java_vm;


    jmethodID javaCallbackOnPointId;
    jobject point_call_back;
    jobject java_qrcode_class;

private:
    JNIEnv *env;
};


#endif //SCAN_JAVACALLHELPER_H
