//
// Created by Zwj on 2022/7/12.
//

#include "JavaCallHelper.h"
#include "Utils.h"
#include <iostream>

void JavaCallHelper::callBackBitMap(cv::Mat &mat) {
    java_vm->AttachCurrentThread(&env, nullptr);
    env->CallVoidMethod(callback, javaCallbackId, Utils::mat_to_bitmap(env, mat, false));
    java_vm->DetachCurrentThread();
}

void JavaCallHelper::callBackOnPoint(std::vector<CodeBean> &qrCodes) {
    if (java_vm == nullptr || point_call_back == nullptr || java_qrcode_class == nullptr) {
        return;
    }
    java_vm->AttachCurrentThread(&env, nullptr);
    auto java_list_class = (jclass) env->FindClass("java/util/ArrayList");
    jmethodID list_mid = env->GetMethodID(java_list_class,
                                          "<init>", "()V");


    jmethodID list_add_mid = env->GetMethodID(java_list_class, "add", "(Ljava/lang/Object;)Z");

    jobject new_list = env->NewObject(java_list_class, list_mid);

    if (qrCodes.empty()) {
        env->CallVoidMethod(point_call_back, javaCallbackOnPointId, new_list);
        java_vm->DetachCurrentThread();
        return;
    }


    jmethodID qrcode_mid = env->GetMethodID((jclass) java_qrcode_class,
                                            "<init>",
                                            "(Landroid/graphics/Rect;Ljava/lang/String;ILandroid/graphics/PointF;Landroid/graphics/PointF;Landroid/graphics/PointF;Landroid/graphics/PointF;Landroid/graphics/PointF;)V");

    auto java_rect_class = (jclass) env->FindClass("android/graphics/Rect");
    jmethodID rectMid = env->GetMethodID(java_rect_class,
                                         "<init>", "(IIII)V");

    auto java_PointF_class = env->FindClass("android/graphics/PointF");
    jmethodID PointFInitMid = env->GetMethodID(java_PointF_class,
                                               "<init>", "(FF)V");

    for (auto &qrCode: qrCodes) {


        jobject new_rect = env->NewObject(java_rect_class, rectMid, qrCode.rect.x, qrCode.rect.y,
                                          qrCode.rect.x + qrCode.rect.width,
                                          qrCode.rect.y + qrCode.rect.height);
        jobject topLeft = env->NewObject(java_PointF_class, PointFInitMid, qrCode.topLeft.x,
                                         qrCode.topLeft.y);
        jobject bottomLeft = env->NewObject(java_PointF_class, PointFInitMid, qrCode.bottomLeft.x,
                                            qrCode.bottomLeft.y);
        jobject bottomRight = env->NewObject(java_PointF_class, PointFInitMid, qrCode.bottomRight.x,
                                             qrCode.bottomRight.y);
        jobject topRight = env->NewObject(java_PointF_class, PointFInitMid, qrCode.topRight.x,
                                          qrCode.topRight.y);
        jobject center = env->NewObject(java_PointF_class, PointFInitMid, qrCode.center.x,
                                        qrCode.center.y);


        jobject new_qrcode = env->NewObject((jclass) java_qrcode_class, qrcode_mid, new_rect,
                                            stringToUtf8(qrCode.code.data()), qrCode.type,
                                            topLeft, bottomLeft, bottomRight, topRight, center);

        env->CallBooleanMethod(new_list, list_add_mid, new_qrcode);
    }
    qrCodes.clear();

    env->CallVoidMethod(point_call_back, javaCallbackOnPointId, new_list);

    java_vm->DetachCurrentThread();
}


JavaCallHelper::JavaCallHelper() {
    point_call_back = nullptr;
    java_qrcode_class = nullptr;
    callback = nullptr;
}

void JavaCallHelper::release(JNIEnv *env) {
    javaCallbackId = nullptr;
    javaCallbackOnPointId = nullptr;
    if (java_qrcode_class != nullptr) {
        env->DeleteGlobalRef(java_qrcode_class);
    }
    java_qrcode_class = nullptr;

    if (point_call_back != nullptr) {
        env->DeleteGlobalRef(point_call_back);
    }
    point_call_back = nullptr;
    if (callback != nullptr) {
        env->DeleteGlobalRef(callback);
    }
    callback = nullptr;
    java_vm = nullptr;
}

jstring JavaCallHelper::stringToUtf8(const char *filename) {
    jobject bb = env->NewDirectByteBuffer((void *) filename, strlen(filename));

    jclass cls_Charset = env->FindClass("java/nio/charset/Charset");
    jmethodID mid_Charset_forName = env->GetStaticMethodID(cls_Charset, "forName",
                                                           "(Ljava/lang/String;)Ljava/nio/charset/Charset;");
    jobject charset = env->CallStaticObjectMethod(cls_Charset, mid_Charset_forName,
                                                  env->NewStringUTF("UTF-8"));

    jmethodID mid_Charset_decode = env->GetMethodID(cls_Charset, "decode",
                                                    "(Ljava/nio/ByteBuffer;)Ljava/nio/CharBuffer;");
    jobject cb = env->CallObjectMethod(charset, mid_Charset_decode, bb);
    env->DeleteLocalRef(bb);

    jclass cls_CharBuffer = env->FindClass("java/nio/CharBuffer");
    jmethodID mid_CharBuffer_toString = env->GetMethodID(cls_CharBuffer, "toString",
                                                         "()Ljava/lang/String;");
   return static_cast<jstring>(env->CallObjectMethod(cb, mid_CharBuffer_toString));
}


