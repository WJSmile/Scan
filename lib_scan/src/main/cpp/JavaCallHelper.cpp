//
// Created by Zwj on 2022/7/12.
//

#include "JavaCallHelper.h"
#include "XLog.h"

jobject
JavaCallHelper::codeBeanToJava(JNIEnv *env, std::vector<CodeBean> &qrCodes, float scale, int top,
                               int left) {

    auto java_list_class = (jclass) env->FindClass("java/util/ArrayList");

    jmethodID list_mid = env->GetMethodID(java_list_class,
                                          "<init>", "()V");


    jmethodID list_add_mid = env->GetMethodID(java_list_class, "add", "(Ljava/lang/Object;)Z");

    jobject new_list = env->NewObject(java_list_class, list_mid);

    auto java_qrcode_class = (jclass) env->FindClass("com/palmpay/scan/bean/CodeBean");

    jmethodID qrcode_mid = env->GetMethodID(java_qrcode_class,
                                            "<init>",
                                            "(Landroid/graphics/RectF;[BLjava/lang/String;Landroid/graphics/PointF;Landroid/graphics/PointF;Landroid/graphics/PointF;Landroid/graphics/PointF;Landroid/graphics/PointF;)V");

    auto java_rect_class = (jclass) env->FindClass("android/graphics/RectF");
    jmethodID rectMid = env->GetMethodID(java_rect_class,
                                         "<init>", "(FFFF)V");

    auto java_PointF_class = env->FindClass("android/graphics/PointF");
    jmethodID pointFInitMid = env->GetMethodID(java_PointF_class,
                                               "<init>", "(FF)V");

    for (auto &qrCode: qrCodes) {


        jobject new_rect = env->NewObject(java_rect_class, rectMid,
                                          ((float) qrCode.rect.x * scale) + (float) top,
                                          ((float) qrCode.rect.y * scale) + (float) left,
                                          (((float) qrCode.rect.x + (float) qrCode.rect.width) *
                                           scale) + (float) left,
                                          (((float) qrCode.rect.y + (float) qrCode.rect.height) *
                                           scale) + (float) top);
        jobject topLeft = env->NewObject(java_PointF_class, pointFInitMid,
                                         (qrCode.topLeft.x * scale) + (float) left,
                                         (qrCode.topLeft.y * scale) + (float) top);
        jobject bottomLeft = env->NewObject(java_PointF_class, pointFInitMid,
                                            (qrCode.bottomLeft.x * scale) + (float) left,
                                            (qrCode.bottomLeft.y * scale) + (float) top);
        jobject bottomRight = env->NewObject(java_PointF_class, pointFInitMid,
                                             (qrCode.bottomRight.x * scale) + (float) left,
                                             (qrCode.bottomRight.y * scale) + (float) top);
        jobject topRight = env->NewObject(java_PointF_class, pointFInitMid,
                                          (qrCode.topRight.x * scale) + (float) left,
                                          (qrCode.topRight.y * scale) + (float) top);
        jobject center = env->NewObject(java_PointF_class, pointFInitMid,
                                        (qrCode.center.x * scale) + (float) left,
                                        (qrCode.center.y * scale) + (float) top);

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




