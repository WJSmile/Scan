//
// Created by Zwj on 2022/7/12.
//

#include "JavaCallHelper.h"

jobject JavaCallHelper::codeBeanToJava(JNIEnv *env, std::vector<CodeBean> &qrCodes) {

    auto java_list_class = (jclass) env->FindClass("java/util/ArrayList");

    jmethodID list_mid = env->GetMethodID(java_list_class,
                                          "<init>", "()V");


    jmethodID list_add_mid = env->GetMethodID(java_list_class, "add", "(Ljava/lang/Object;)Z");

    jobject new_list = env->NewObject(java_list_class, list_mid);

    auto java_qrcode_class = (jclass) env->FindClass("com/palmpay/scan/bean/CodeBean");

    jmethodID qrcode_mid = env->GetMethodID(java_qrcode_class,
                                            "<init>",
                                            "(Landroid/graphics/Rect;[BILandroid/graphics/PointF;Landroid/graphics/PointF;Landroid/graphics/PointF;Landroid/graphics/PointF;Landroid/graphics/PointF;)V");

    auto java_rect_class = (jclass) env->FindClass("android/graphics/Rect");
    jmethodID rectMid = env->GetMethodID(java_rect_class,
                                         "<init>", "(IIII)V");

    auto java_PointF_class = env->FindClass("android/graphics/PointF");
    jmethodID pointFInitMid = env->GetMethodID(java_PointF_class,
                                               "<init>", "(FF)V");

    for (auto &qrCode: qrCodes) {


        jobject new_rect = env->NewObject(java_rect_class, rectMid, qrCode.rect.x, qrCode.rect.y,
                                          qrCode.rect.x + qrCode.rect.width,
                                          qrCode.rect.y + qrCode.rect.height);
        jobject topLeft = env->NewObject(java_PointF_class, pointFInitMid, qrCode.topLeft.x,
                                         qrCode.topLeft.y);
        jobject bottomLeft = env->NewObject(java_PointF_class, pointFInitMid, qrCode.bottomLeft.x,
                                            qrCode.bottomLeft.y);
        jobject bottomRight = env->NewObject(java_PointF_class, pointFInitMid, qrCode.bottomRight.x,
                                             qrCode.bottomRight.y);
        jobject topRight = env->NewObject(java_PointF_class, pointFInitMid, qrCode.topRight.x,
                                          qrCode.topRight.y);
        jobject center = env->NewObject(java_PointF_class, pointFInitMid, qrCode.center.x,
                                        qrCode.center.y);

        jbyteArray jarray = env->NewByteArray(qrCode.code.length());
        env->SetByteArrayRegion(jarray, 0, qrCode.code.length(), (jbyte *) qrCode.code.c_str());

        jobject new_qrcode = env->NewObject((jclass) java_qrcode_class, qrcode_mid, new_rect,
                                            jarray, qrCode.type,
                                            topLeft, bottomLeft, bottomRight, topRight, center);

        env->CallBooleanMethod(new_list, list_add_mid, new_qrcode);

        env->DeleteLocalRef(jarray);
    }
    qrCodes.clear();

    env->DeleteLocalRef(java_qrcode_class);
    env->DeleteLocalRef(java_list_class);
    env->DeleteLocalRef(java_rect_class);
    env->DeleteLocalRef(java_PointF_class);
    return new_list;
}




