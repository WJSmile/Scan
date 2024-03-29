#include <jni.h>
#include <libyuv/rotate.h>
#include <libyuv/video_common.h>
#include <cstring>

#include "libyuv/include/libyuv.h"


void clipping(jbyte *src_yuv_data, jbyte *dst_yuv_data, jint width, jint height,
              jint crop_x, jint crop_y, jint crop_width,
              jint crop_height) {
    jint dst_i420_y_size = crop_width * crop_height;
    jint dst_i420_u_size = (crop_width >> 1) * (crop_height >> 1);

    jbyte *dst_i420_y_data = dst_yuv_data;
    jbyte *dst_i420_u_data = dst_yuv_data + dst_i420_y_size;
    jbyte *dst_i420_v_data = dst_yuv_data + dst_i420_y_size + dst_i420_u_size;

    libyuv::ConvertToI420((uint8_t *) src_yuv_data, width * height * 3 / 2,
                          (uint8_t *) dst_i420_y_data, crop_width,
                          (uint8_t *) dst_i420_u_data, crop_width >> 1,
                          (uint8_t *) dst_i420_v_data, crop_width >> 1,
                          crop_x, crop_y,
                          width, height,
                          crop_width, crop_height,
                          libyuv::kRotate0, libyuv::FOURCC_NV21);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_palmpay_scan_code_YuvUtils_clipping(JNIEnv *env, jobject thiz, jbyteArray src_yuv_data,
                                             jbyteArray dst_yuv_data, jint width, jint height,
                                             jint crop_x, jint crop_y, jint crop_width,
                                             jint crop_height) {

    jbyte *src_yuv_data_ = env->GetByteArrayElements(src_yuv_data, nullptr);

    jbyte *dst_yuv_data_ = env->GetByteArrayElements(dst_yuv_data, nullptr);

    clipping(src_yuv_data_, dst_yuv_data_, width, height, crop_x, crop_y, crop_width, crop_height);
    env->ReleaseByteArrayElements(src_yuv_data, src_yuv_data_, 0);
    env->ReleaseByteArrayElements(dst_yuv_data, dst_yuv_data_, 0);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_palmpay_scan_code_YuvUtils_scale(JNIEnv *env, jobject thiz, jbyteArray src_yuv_data,
                                          jbyteArray dst_yuv_data, jint width, jint height,
                                          jint dst_width, jint dst_height) {
    jbyte *src_yuv_data_ = env->GetByteArrayElements(src_yuv_data, nullptr);

    jbyte *dst_yuv_data_ = env->GetByteArrayElements(dst_yuv_data, nullptr);

    jint src_i420_y_size = width * height;
    jint src_i420_u_size = (width >> 1) * (height >> 1);
    jbyte *src_i420_y_data = src_yuv_data_;
    jbyte *src_i420_u_data = src_yuv_data_ + src_i420_y_size;
    jbyte *src_i420_v_data = src_yuv_data_ + src_i420_y_size + src_i420_u_size;

    jint dst_i420_y_size = dst_width * dst_height;
    jint dst_i420_u_size = (dst_width >> 1) * (dst_height >> 1);
    jbyte *dst_i420_y_data = dst_yuv_data_;
    jbyte *dst_i420_u_data = dst_yuv_data_ + dst_i420_y_size;
    jbyte *dst_i420_v_data = dst_yuv_data_ + dst_i420_y_size + dst_i420_u_size;

    libyuv::I420Scale((uint8_t *) src_i420_y_data, width,
                      (uint8_t *) src_i420_u_data, width >> 1,
                      (uint8_t *) src_i420_v_data, width >> 1,
                      width, height,
                      (uint8_t *) dst_i420_y_data, dst_width,
                      (uint8_t *) dst_i420_u_data, dst_width >> 1,
                      (uint8_t *) dst_i420_v_data, dst_width >> 1,
                      dst_width, dst_height,
                      libyuv::kFilterNone);

    env->ReleaseByteArrayElements(src_yuv_data, src_yuv_data_, 0);
    env->ReleaseByteArrayElements(dst_yuv_data, dst_yuv_data_, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_palmpay_scan_code_YuvUtils_rotation(JNIEnv *env, jobject thiz, jbyteArray src_yuv_data,
                                             jbyteArray dst_yuv_data, jint width, jint height,
                                             jint type) {

    jbyte *src_yuv_data_ = env->GetByteArrayElements(src_yuv_data, nullptr);

    jbyte *dst_yuv_data_ = env->GetByteArrayElements(dst_yuv_data, nullptr);
    jint src_i420_y_size = width * height;
    jint src_i420_u_size = (width >> 1) * (height >> 1);

    jbyte *src_i420_y_data = src_yuv_data_;
    jbyte *src_i420_u_data = src_yuv_data_ + src_i420_y_size;
    jbyte *src_i420_v_data = src_yuv_data_ + src_i420_y_size + src_i420_u_size;

    jbyte *dst_i420_y_data = dst_yuv_data_;
    jbyte *dst_i420_u_data = dst_yuv_data_ + src_i420_y_size;
    jbyte *dst_i420_v_data = dst_yuv_data_ + src_i420_y_size + src_i420_u_size;

    //要注意这里的width和height在旋转之后是相反的
    if (type == libyuv::kRotate90 || type == libyuv::kRotate270) {
        libyuv::I420Rotate((uint8_t *) src_i420_y_data, width,
                           (uint8_t *) src_i420_u_data, width >> 1,
                           (uint8_t *) src_i420_v_data, width >> 1,
                           (uint8_t *) dst_i420_y_data, height,
                           (uint8_t *) dst_i420_u_data, height >> 1,
                           (uint8_t *) dst_i420_v_data, height >> 1,
                           width, height,
                           (libyuv::RotationMode) type);
    } else {
        libyuv::I420Rotate((uint8_t *) src_i420_y_data, width,
                           (uint8_t *) src_i420_u_data, width >> 1,
                           (uint8_t *) src_i420_v_data, width >> 1,
                           (uint8_t *) dst_i420_y_data, width,
                           (uint8_t *) dst_i420_u_data, width >> 1,
                           (uint8_t *) dst_i420_v_data, width >> 1,
                           width, height,
                           (libyuv::RotationMode) type);
    }


    env->ReleaseByteArrayElements(src_yuv_data, src_yuv_data_, 0);
    env->ReleaseByteArrayElements(dst_yuv_data, dst_yuv_data_, 0);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_palmpay_scan_code_YuvUtils_yuvI420ToNV21(JNIEnv *env, jobject thiz,
                                                  jbyteArray src_yuv_data, jbyteArray dst_yuv_data,
                                                  jint width, jint height) {
    jbyte *src_yuv_data_ = env->GetByteArrayElements(src_yuv_data, nullptr);

    jbyte *dst_yuv_data_ = env->GetByteArrayElements(dst_yuv_data, nullptr);
    memcpy(dst_yuv_data_, src_yuv_data_, width * height);//y分量

    jint src_y_size = width * height;
    jint src_u_size = (width >> 1) * (height >> 1);

    jbyte *src_nv21_y_data = dst_yuv_data_;
    jbyte *src_nv21_uv_data = dst_yuv_data_ + src_y_size;

    jbyte *src_i420_y_data = src_yuv_data_;
    jbyte *src_i420_u_data = src_yuv_data_ + src_y_size;
    jbyte *src_i420_v_data = src_yuv_data_ + src_y_size + src_u_size;


    libyuv::I420ToNV21(
            (uint8_t *) src_i420_y_data, width,
            (uint8_t *) src_i420_u_data, width >> 1,
            (uint8_t *) src_i420_v_data, width >> 1,
            (uint8_t *) src_nv21_y_data, width,
            (uint8_t *) src_nv21_uv_data, width,
            width, height);
    env->ReleaseByteArrayElements(src_yuv_data, src_yuv_data_, 0);
    env->ReleaseByteArrayElements(dst_yuv_data, dst_yuv_data_, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_palmpay_scan_code_YuvUtils_nV21ToI420(JNIEnv *env, jobject thiz, jbyteArray src_yuv_data,
                                               jbyteArray dst_yuv_data, jint width, jint height) {

    jbyte *src_yuv_data_ = env->GetByteArrayElements(src_yuv_data, nullptr);

    jbyte *dst_yuv_data_ = env->GetByteArrayElements(dst_yuv_data, nullptr);
    jint src_y_size = width * height;
    jint src_u_size = (width >> 1) * (height >> 1);

    jbyte *src_nv21_y_data = src_yuv_data_;
    jbyte *src_nv21_vu_data = src_yuv_data_ + src_y_size;

    jbyte *src_i420_y_data = dst_yuv_data_;
    jbyte *src_i420_u_data = dst_yuv_data_ + src_y_size;
    jbyte *src_i420_v_data = dst_yuv_data_ + src_y_size + src_u_size;

    libyuv::NV21ToI420((uint8_t *) src_nv21_y_data, width,
                       (uint8_t *) src_nv21_vu_data, width,
                       (uint8_t *) src_i420_y_data, width,
                       (uint8_t *) src_i420_u_data, width >> 1,
                       (uint8_t *) src_i420_v_data, width >> 1,
                       width, height);

    env->ReleaseByteArrayElements(src_yuv_data, src_yuv_data_, 0);
    env->ReleaseByteArrayElements(dst_yuv_data, dst_yuv_data_, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_palmpay_scan_code_YuvUtils_scaleAndclipping(JNIEnv *env, jobject thiz,
                                                     jbyteArray src_yuv_data,
                                                     jbyteArray dst_yuv_data, jint width,
                                                     jint height, jint crop_x, jint crop_y,
                                                     jint crop_width, jint crop_height,
                                                     jint dst_width, jint dst_height) {
    jbyte *src_yuv_data_ = env->GetByteArrayElements(src_yuv_data, nullptr);

    jbyte *dst_yuv_data_ = env->GetByteArrayElements(dst_yuv_data, nullptr);

    jbyteArray scale_yuv_data = env->NewByteArray(dst_width * dst_height * 3 / 2);
    jbyte *scale_yuv_data_ = env->GetByteArrayElements(scale_yuv_data, nullptr);

    jint src_i420_y_size = width * height;
    jint src_i420_u_size = (width >> 1) * (height >> 1);
    jbyte *src_i420_y_data = src_yuv_data_;
    jbyte *src_i420_u_data = src_yuv_data_ + src_i420_y_size;
    jbyte *src_i420_v_data = src_yuv_data_ + src_i420_y_size + src_i420_u_size;

    jint dst_i420_y_size = dst_width * dst_height;
    jint dst_i420_u_size = (dst_width >> 1) * (dst_height >> 1);
    jbyte *dst_i420_y_data = scale_yuv_data_;
    jbyte *dst_i420_u_data = scale_yuv_data_ + dst_i420_y_size;
    jbyte *dst_i420_v_data = scale_yuv_data_ + dst_i420_y_size + dst_i420_u_size;

    libyuv::I420Scale((uint8_t *) src_i420_y_data, width,
                      (uint8_t *) src_i420_u_data, width >> 1,
                      (uint8_t *) src_i420_v_data, width >> 1,
                      width, height,
                      (uint8_t *) dst_i420_y_data, dst_width,
                      (uint8_t *) dst_i420_u_data, dst_width >> 1,
                      (uint8_t *) dst_i420_v_data, dst_width >> 1,
                      dst_width, dst_height,
                      libyuv::kFilterNone);


    clipping(scale_yuv_data_, dst_yuv_data_, dst_width, dst_height, crop_x, crop_y, crop_width,
             crop_height);

    env->ReleaseByteArrayElements(src_yuv_data, src_yuv_data_, 0);
    env->ReleaseByteArrayElements(dst_yuv_data, dst_yuv_data_, 0);
    env->ReleaseByteArrayElements(scale_yuv_data, scale_yuv_data_, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_palmpay_scan_code_YuvUtils_Android420ToI420(JNIEnv *env, jobject thiz, jobject y,
                                                             jobject u, jobject v, jint y_stride,
                                                             jint u_stride, jint v_stride,
                                                             jint src_pixel_stride_uv,
                                                             jbyteArray dst_yuv_data, jint width,
                                                             jint height) {
    auto *src_y_data_ = (uint8_t *) env->GetDirectBufferAddress(y);
    auto *src_u_data_ = (uint8_t *) env->GetDirectBufferAddress(u);
    auto *src_v_data_ = (uint8_t *) env->GetDirectBufferAddress(v);

    jbyte *dst_yuv_data_ = env->GetByteArrayElements(dst_yuv_data, nullptr);

    jint src_y_size = width * height;
    jint src_u_size = (width >> 1) * (height >> 1);

    jbyte *dst_i420_y_data = dst_yuv_data_;
    jbyte *dst_i420_u_data = dst_yuv_data_ + src_y_size;
    jbyte *dst_i420_v_data = dst_yuv_data_ + src_y_size + src_u_size;

    libyuv::Android420ToI420(src_y_data_, y_stride, src_u_data_, u_stride,
                             src_v_data_, v_stride,
                             src_pixel_stride_uv, (uint8_t *) dst_i420_y_data, width,
                             (uint8_t *) dst_i420_u_data,
                             width >> 1, (uint8_t *) dst_i420_v_data, width >> 1, width, height);


    env->ReleaseByteArrayElements(dst_yuv_data, dst_yuv_data_, 0);

}