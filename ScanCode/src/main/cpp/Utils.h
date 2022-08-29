//
// Created by Zwj on 2022/7/12.
//

#ifndef SCAN_UTILS_H
#define SCAN_UTILS_H

#include <jni.h>
#include <opencv2/opencv.hpp>
#include <src/BarcodeFormat.h>
#include <src/CharacterSet.h>
#include <string>


class Utils {
public:
    static jobject mat_to_bitmap(JNIEnv *env, cv::Mat &src, bool needPremultiplyAlpha);


    static jobject writerCode(JNIEnv *env ,const std::string &contents, int width, int height,
               ZXing::BarcodeFormat barcodeFormat,
               ZXing::CharacterSet characterSet = ZXing::CharacterSet::UTF8, int level = -1,
               int margin = -1,
               int black = 0xFF000000, int white = 0xFFFFFFFF);

};


#endif //SCAN_UTILS_H
