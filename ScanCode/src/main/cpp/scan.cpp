#include <jni.h>
#include "Distinguish.h"
#include "XLog.h"
#include "Utils.h"

Distinguish *GetDistinguishFromObj(JNIEnv *env, jobject obj) {
    auto objClazz = (jclass) env->GetObjectClass(obj);
    if (objClazz == nullptr) {
        XLOGE("env->GetObjectClass return null.");
        return nullptr;
    }
    jfieldID fid = env->GetFieldID(objClazz, "distinguishId", "J");
    if (fid == nullptr) {
        XLOGE("env->GetFieldID error.");
        return nullptr;
    }
    auto p = (jlong) env->GetLongField(obj, fid);
    if (p == -1) {
        return nullptr;
    }
    return (Distinguish *) p;
}

void SetDistinguishFromObj(JNIEnv *env, jobject obj, jlong jlong1) {
    auto objClazz = (jclass) env->GetObjectClass(obj);
    if (objClazz == nullptr) {
        XLOGE("env->GetObjectClass return null.");
        return;
    }
    jfieldID fid = env->GetFieldID(objClazz, "distinguishId", "J");
    if (fid == nullptr) {
        XLOGE("env->GetFieldID error.");
        return;
    }
    env->SetLongField(obj, fid, jlong1);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_palmpay_scan_code_NativeLib_initScan(JNIEnv *env, jobject thiz, jstring detect_proto_txt,
                                              jstring detect_caffe_model, jstring sr_proto_txt,
                                              jstring sr_caffe_model) {
    const char *_detect_proto_txt = env->GetStringUTFChars(detect_proto_txt, nullptr);


    const char *_detect_caffe_model = env->GetStringUTFChars(detect_caffe_model, nullptr);

    const char *_sr_proto_txt = env->GetStringUTFChars(sr_proto_txt, nullptr);
    const char *_sr_caffe_model = env->GetStringUTFChars(sr_caffe_model, nullptr);


    auto distinguish = new Distinguish(_detect_proto_txt,
                                       _detect_caffe_model,
                                       _sr_proto_txt,
                                       _sr_caffe_model);

    SetDistinguishFromObj(env, thiz, reinterpret_cast<jlong>(distinguish));

    env->ReleaseStringUTFChars(detect_proto_txt, _detect_proto_txt);

    env->ReleaseStringUTFChars(detect_caffe_model, _detect_caffe_model);

    env->ReleaseStringUTFChars(sr_proto_txt, _sr_proto_txt);

    env->ReleaseStringUTFChars(sr_caffe_model, _sr_caffe_model);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_palmpay_scan_code_NativeLib_release(JNIEnv *env, jobject thiz) {

    Distinguish *distinguish = GetDistinguishFromObj(env, thiz);
    if (distinguish != nullptr) {
        SetDistinguishFromObj(env, thiz, -1);
        distinguish->release();
        delete distinguish;
    }

}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_palmpay_scan_code_NativeLib_scanCode(JNIEnv *env, jobject thiz, jbyteArray bytes,
                                              jint width, jint height) {
    Distinguish *distinguish = GetDistinguishFromObj(env, thiz);
    jobject obj = nullptr;
    if (distinguish != nullptr) {
        jbyte *bytes_ = env->GetByteArrayElements(bytes, nullptr);

        Mat src(height + height / 2,
                width, CV_8UC1,
                (uchar *) bytes_);

        cvtColor(src, src, COLOR_YUV2GRAY_420);
        rotate(src, src, ROTATE_90_CLOCKWISE);
        obj = distinguish->decode(env, src);
        src.release();
        env->ReleaseByteArrayElements(bytes, bytes_, 0);
    }
    return obj;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_palmpay_scan_code_NativeLib_scanCodeFormBitMap(JNIEnv *env, jobject thiz, jobject bitmap) {
    Distinguish *distinguish = GetDistinguishFromObj(env, thiz);
    jobject obj = nullptr;
    if (distinguish != nullptr) {
        Mat src;
        Utils::BitmapToMat(env,bitmap,src);
        cvtColor(src, src, COLOR_RGBA2GRAY);
        obj = distinguish->decode(env, src);
        src.release();
    }
    return  obj;
}


extern "C"
JNIEXPORT jobject JNICALL
Java_com_palmpay_scan_code_NativeLib_scanCodeCut(JNIEnv *env, jobject thiz, jbyteArray bytes,
                                                 jint width, jint height, jint box_width,
                                                 jint box_top) {
    Distinguish *distinguish = GetDistinguishFromObj(env, thiz);
    jobject obj = nullptr;
    if (distinguish != nullptr) {
        jbyte *bytes_ = env->GetByteArrayElements(bytes, nullptr);
        Mat src(height + height / 2,
                width, CV_8UC1,
                (uchar *) bytes_);

        cvtColor(src, src, COLOR_YUV2GRAY_420);
        rotate(src, src, ROTATE_90_CLOCKWISE);

        if (box_width != 0 && box_top != 0) {
            Rect rect = Rect(Point((src.cols - box_width) / 2,
                                   box_top),
                             Point((src.cols - box_width) / 2 +
                                           box_width, box_top + box_width));
            src = src(rect);
        }
        obj = distinguish->decode(env, src);
        src.release();
        env->ReleaseByteArrayElements(bytes, bytes_, 0);
    }
    return obj;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_palmpay_scan_code_NativeLib_setSimpleMode(JNIEnv *env, jobject thiz, jboolean is_simple) {
    Distinguish *distinguish = GetDistinguishFromObj(env, thiz);
    if (distinguish != nullptr) {
        distinguish->simpleMode = is_simple;
    }
}

void ThrowJavaException(JNIEnv *env, const char *message) {
    static jclass jcls = env->FindClass("java/lang/RuntimeException");
    env->ThrowNew(jcls, message);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_palmpay_scan_code_NativeLib_getCodeBitMap(JNIEnv *env, jobject thiz, jstring contents,
                                                   jint width, jint height,
                                                   jstring barcode_format_name,
                                                   jstring character_set_name, jint level,
                                                   jint margin, jint black, jint white) {
    const char *contents_ = env->GetStringUTFChars(contents, nullptr);
    const char *barcode_format_name_ = env->GetStringUTFChars(barcode_format_name, nullptr);
    const char *character_set_name_ = env->GetStringUTFChars(character_set_name, nullptr);
    jobject obj;
    try {
        obj = Utils::writerCode(env,
                                contents_,
                                width,
                                height,
                                ZXing::BarcodeFormatFromString(barcode_format_name_),
                                ZXing::CharacterSetFromString(character_set_name_),
                                level, margin, black, white);
        env->ReleaseStringUTFChars(contents, contents_);
        env->ReleaseStringUTFChars(contents, barcode_format_name_);
        env->ReleaseStringUTFChars(contents, character_set_name_);
    } catch (const std::exception &e) {
        ThrowJavaException(env, e.what());
    }
    return obj;
}
