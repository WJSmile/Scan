//
// Created by Zwj on 2022/6/16.
//

#ifndef SCAN_IMAGEDATA_H
#define SCAN_IMAGEDATA_H
#include <jni.h>

struct ImageData {
    jbyte *data = 0;
    long length = 0;
    int width = 0;
    int height = 0;
};


#endif //SCAN_IMAGEDATA_H
