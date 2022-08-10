#include <jni.h>
#include <string>
#include "Distinguish.h"
#include "Utils.h"
#include "XLog.h"

Distinguish *distinguish;
JavaVM *java_vm;

extern "C"
JNIEXPORT
jint JNI_OnLoad(JavaVM *vm, void *res) {
    JNIEnv *env;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    java_vm = vm;
    return JNI_VERSION_1_6;
}

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
    distinguish = GetDistinguishFromObj(env, thiz);
    if (distinguish == nullptr) {
        distinguish = new Distinguish(_detect_proto_txt,
                                      _detect_caffe_model,
                                      _sr_proto_txt,
                                      _sr_caffe_model);
        SetDistinguishFromObj(env, thiz, reinterpret_cast<jlong>(distinguish));
    }

    distinguish->javaCallHelper->java_vm = java_vm;

    distinguish->Start();

    env->ReleaseStringUTFChars(detect_proto_txt, _detect_proto_txt);

    env->ReleaseStringUTFChars(detect_caffe_model, _detect_caffe_model);

    env->ReleaseStringUTFChars(sr_proto_txt, _sr_proto_txt);

    env->ReleaseStringUTFChars(sr_caffe_model, _sr_caffe_model);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_palmpay_scan_code_NativeLib_setImageByte(JNIEnv *env, jobject thiz, jbyteArray bytes,
                                                  jint width, jint height) {
    if (distinguish != nullptr && !distinguish->signOut) {
        auto *imageData = new ImageData();
        jbyte *bytes_ = env->GetByteArrayElements(bytes, nullptr);
        imageData->data = bytes_;
        imageData->height = height;
        imageData->width = width;
        distinguish->setImageData(imageData);
        env->ReleaseByteArrayElements(bytes, bytes_, 0);
    }
}



extern "C"
JNIEXPORT void JNICALL
Java_com_palmpay_scan_code_NativeLib_release(JNIEnv *env, jobject thiz) {
    distinguish->release(env);
    SetDistinguishFromObj(env, thiz, -1);
    delete distinguish;
    distinguish = nullptr;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_palmpay_scan_code_NativeLib_pause(JNIEnv *env, jobject thiz, jboolean is_pause) {
    if (distinguish == nullptr) {
        return;
    }
    distinguish->pause(is_pause);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_palmpay_scan_code_NativeLib_setBitMapCallBack(JNIEnv *env, jobject thiz,
                                                       jobject callback) {
    if (distinguish == nullptr) {
        return;
    }
    //通过传进来的对象找到该类
    jobject g_callback = env->NewGlobalRef(callback);// 生成全局引用


    jclass javaClass = env->GetObjectClass(g_callback);
    //获取要回调的方法ID,回调java方法
    jmethodID javaCallbackId = env->GetMethodID(javaClass, "onBitMap",
                                                "(Landroid/graphics/Bitmap;)V");
    distinguish->javaCallHelper->javaCallbackId = javaCallbackId;
    distinguish->javaCallHelper->callback = g_callback;

    env->DeleteLocalRef(javaClass);


}

extern "C"
JNIEXPORT void JNICALL
Java_com_palmpay_scan_code_NativeLib_setPointCallBack(JNIEnv *env, jobject thiz,
                                                      jobject point_call_back) {
    if (distinguish == nullptr) {
        return;
    }
    jobject g_callback = env->NewGlobalRef(point_call_back);// 生成全局引用

    jclass javaClass = env->GetObjectClass(g_callback);

    auto java_code_class = (jclass) env->FindClass("com/palmpay/scan/bean/CodeBean");

    jmethodID javaCallbackOnPointId = env->GetMethodID(javaClass, "onPoint",
                                                       "(Ljava/util/ArrayList;)V");


    distinguish->javaCallHelper->javaCallbackOnPointId = javaCallbackOnPointId;
    distinguish->javaCallHelper->point_call_back = g_callback;
    distinguish->javaCallHelper->java_qrcode_class = env->NewGlobalRef(java_code_class);

    env->DeleteLocalRef(java_code_class);
    env->DeleteLocalRef(javaClass);
}
