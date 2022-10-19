//
// Created by Zwj on 2022/7/12.
//

#include "JavaCallHelper.h"
#include "XLog.h"

jobject JavaCallHelper::codeBeanToJava(JNIEnv *env, std::vector<CodeBean> &qrCodes) {

    auto java_list_class = (jclass) env->FindClass("java/util/ArrayList");

    jmethodID list_mid = env->GetMethodID(java_list_class,
                                          "<init>", "()V");


    jmethodID list_add_mid = env->GetMethodID(java_list_class, "add", "(Ljava/lang/Object;)Z");

    jobject new_list = env->NewObject(java_list_class, list_mid);

    auto java_qrcode_class = (jclass) env->FindClass("com/palmpay/scan/bean/CodeBean");

    jmethodID qrcode_mid = env->GetMethodID(java_qrcode_class,
                                            "<init>",
                                            "(Landroid/graphics/Rect;[BLjava/lang/String;Landroid/graphics/PointF;Landroid/graphics/PointF;Landroid/graphics/PointF;Landroid/graphics/PointF;Landroid/graphics/PointF;)V");

    auto java_rect_class = (jclass) env->FindClass("android/graphics/Rect");
    jmethodID rectMid = env->GetMethodID(java_rect_class,
                                         "<init>", "(IIII)V");

    auto java_PointF_class = env->FindClass("android/graphics/PointF");
    jmethodID pointFInitMid = env->GetMethodID(java_PointF_class,
                                               "<init>", "(FF)V");

    for (auto &qrCode: qrCodes) {


        jobject new_rect = env->NewObject(java_rect_class, rectMid, qrCode.rect.x * 2,
                                          qrCode.rect.y * 2,
                                          (qrCode.rect.x + qrCode.rect.width) * 2,
                                          (qrCode.rect.y + qrCode.rect.height) * 2);
        jobject topLeft = env->NewObject(java_PointF_class, pointFInitMid, qrCode.topLeft.x,
                                         qrCode.topLeft.y * 2);
        jobject bottomLeft = env->NewObject(java_PointF_class, pointFInitMid,
                                            qrCode.bottomLeft.x * 2,
                                            qrCode.bottomLeft.y * 2);
        jobject bottomRight = env->NewObject(java_PointF_class, pointFInitMid,
                                             qrCode.bottomRight.x * 2,
                                             qrCode.bottomRight.y * 2);
        jobject topRight = env->NewObject(java_PointF_class, pointFInitMid, qrCode.topRight.x * 2,
                                          qrCode.topRight.y * 2);
        jobject center = env->NewObject(java_PointF_class, pointFInitMid, qrCode.center.x * 2,
                                        qrCode.center.y * 2);

        jbyteArray jarray = env->NewByteArray(qrCode.code.length());
        env->SetByteArrayRegion(jarray, 0, qrCode.code.length(), (jbyte *) qrCode.code.c_str());

        jobject new_qrcode = env->NewObject((jclass) java_qrcode_class, qrcode_mid, new_rect,
                                            jarray, env->NewStringUTF(qrCode.type.c_str()),
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




