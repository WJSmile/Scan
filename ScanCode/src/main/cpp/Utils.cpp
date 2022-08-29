//
// Created by Zwj on 2022/7/12.
//
#include "Utils.h"
#include "XLog.h"
#include <android/bitmap.h>
#include <opencv2/imgproc/types_c.h>
#include <src/BitMatrix.h>
#include <src/MultiFormatWriter.h>

using namespace ZXing;

jobject Utils::mat_to_bitmap(JNIEnv *env, cv::Mat &src, bool needPremultiplyAlpha) {
    auto java_bitmap_class = (jclass) env->FindClass("android/graphics/Bitmap");
    jmethodID mid = env->GetStaticMethodID(java_bitmap_class,
                                           "createBitmap",
                                           "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jstring configName = env->NewStringUTF("ARGB_8888");
    jclass bitmapConfigClass = env->FindClass("android/graphics/Bitmap$Config");
    jmethodID valueOfBitmapConfigFunction = env->GetStaticMethodID(
            bitmapConfigClass, "valueOf",
            "(Ljava/lang/String;)Landroid/graphics/Bitmap$Config;");

    jobject bitmapConfig = env->CallStaticObjectMethod(bitmapConfigClass,
                                                       valueOfBitmapConfigFunction, configName);
    jobject bitmap = env->CallStaticObjectMethod(java_bitmap_class,
                                                 mid, src.size().width, src.size().height,
                                                 bitmapConfig);
    AndroidBitmapInfo info;
    void *pixels;

    try {
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4);
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
        CV_Assert(pixels);
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if (src.type() == CV_8UC1) {
                cvtColor(src, tmp, CV_GRAY2RGBA);
            } else if (src.type() == CV_8UC3) {
                cvtColor(src, tmp, CV_RGB2RGBA);
            } else if (src.type() == CV_8UC4) {
                if (needPremultiplyAlpha) {
                    cvtColor(src, tmp, cv::COLOR_RGBA2mRGBA);
                } else {
                    src.copyTo(tmp);
                }
            }
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
            if (src.type() == CV_8UC1) {
                cvtColor(src, tmp, CV_GRAY2BGR565);
            } else if (src.type() == CV_8UC3) {
                cvtColor(src, tmp, CV_RGB2BGR565);
            } else if (src.type() == CV_8UC4) {
                cvtColor(src, tmp, CV_RGBA2BGR565);
            }
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return bitmap;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
        return bitmap;
    }
}

jobject
Utils::writerCode(JNIEnv *env, const std::string &contents, int width, int height,
                  BarcodeFormat barcodeFormat,
                  CharacterSet characterSet,
                  int level, int margin, int black, int white) {


    if (barcodeFormat == BarcodeFormat::None ||
        characterSet == CharacterSet::Unknown) {
        return nullptr;
    }
    MultiFormatWriter writer(barcodeFormat);
    if (barcodeFormat == BarcodeFormat::Aztec || barcodeFormat == BarcodeFormat::PDF417 ||
        barcodeFormat == BarcodeFormat::QRCode) {
        writer.setEccLevel(level);
        writer.setEncoding(characterSet);
    }

    writer.setMargin(margin);
    BitMatrix matrix = writer.encode(contents, width, height);
    if (matrix.empty()) {
        return nullptr;
    }
    jintArray pixels = env->NewIntArray(matrix.height() * matrix.width());

    int index = 0;
    for (int i = 0; i < matrix.height(); ++i) {
        for (int j = 0; j < matrix.width(); ++j) {
            int pix = matrix.get(j, i) ? black : white;
            env->SetIntArrayRegion(pixels, index, 1, &pix);
            index++;
        }
    }
    auto java_code_class = env->FindClass("com/palmpay/scan/bean/CodeData");

    jmethodID code_mid = env->GetMethodID(java_code_class,
                                          "<init>", "([III)V");
    return env->NewObject(java_code_class, code_mid, pixels, matrix.width(), matrix.height());
}



