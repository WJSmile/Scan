//
// Created by Zwj on 2022/7/12.
//

#ifndef SCAN_UTILS_H
#define SCAN_UTILS_H

#include <jni.h>
#include <opencv2/opencv.hpp>

#define DELETE(obj) if(obj){ delete obj; obj = 0; }

class Utils {
public:
   static  jobject mat_to_bitmap(JNIEnv *env, cv::Mat &src, bool needPremultiplyAlpha);
};


#endif //SCAN_UTILS_H
