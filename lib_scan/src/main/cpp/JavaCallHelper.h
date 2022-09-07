//
// Created by Zwj on 2022/7/12.
//

#ifndef SCAN_JAVACALLHELPER_H
#define SCAN_JAVACALLHELPER_H

#include "opencv2/opencv.hpp"

#include <jni.h>

struct CodeBean {
    std::string type;
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
    virtual jobject codeBeanToJava(JNIEnv *env,std::vector<CodeBean> &qrCodeBean);

};


#endif //SCAN_JAVACALLHELPER_H
