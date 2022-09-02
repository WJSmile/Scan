NDK_PATH="/Users/zwj/Library/Android/sdk/ndk/21.4.7075529"
CMAKE_TOOLCHAIN_PATH="${NDK_PATH}/build/cmake/android.toolchain.cmake"

echo "build opencv for android"

function build_opencv_modules
{
    ARCH_ABI=$1
    API_LEVEL=$2
    cmake \
    -DOPENCV_EXTRA_MODULES_PATH=/Users/zwj/work/opencv_contrib/modules \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_CXX_FLAGS_RELEASE=-g0 \
    -DCMAKE_TOOLCHAIN_FILE=$CMAKE_TOOLCHAIN_PATH \
    -DWITH_OPENCL=OFF \
    -DWITH_OPENCL_SVM=OFF \
    -DANDROID_NDK=$NDK_PATH  \
    -DCMAKE_CXX_FLAGS=-std=c++11 \
    -DANDROID_ABI=$ARCH_ABI \
    -DANDROID_STL=c++_static \
    -DENABLE_CXX11=ON \
    -DBUILD_ANDROID_PROJECTS=OFF \
    -DBUILD_ANDROID_EXAMPLES=OFF \
    -DBUILD_JAVA=OFF  \
    -DBUILD_CUDA_STUBS=OFF \
    -DBUILD_DOCS=OFF \
    -DBUILD_EXAMPLES=OFF \
    -DBUILD_JASPER=OFF \
    -DBUILD_JPEG=OFF \
    -DBUILD_OPENEXR=OFF \
    -DBUILD_PACKAGE=OFF \
    -DBUILD_PERF_TESTS=OFF \
    -DBUILD_PNG=OFF \
    -DBUILD_SHARED_LIBS=OFF \
    -DBUILD_TBB=OFF  \
    -DBUILD_TESTS=OFF \
    -DBUILD_TIFF=OFF  \
    -DBUILD_WITH_DEBUG_INFO=OFF  \
    -DBUILD_WITH_DYNAMIC_IPP=OFF  \
    -DBUILD_ZLIB=ON  \
    -DBUILD_PROTOBUF=ON \
    -DBUILD_opencv_apps=OFF \
    -DBUILD_opencv_calib3d=OFF \
    -DBUILD_opencv_core=ON \
    -DBUILD_opencv_features2d=OFF \
    -DBUILD_opencv_flann=OFF \
    -DBUILD_opencv_highgui=OFF \
    -DBUILD_opencv_imgcodecs=OFF \
    -DBUILD_opencv_imgproc=ON \
    -DBUILD_opencv_ml=OFF \
    -DBUILD_opencv_objdetect=OFF \
    -DBUILD_opencv_photo=OFF \
    -DBUILD_opencv_python2=OFF \
    -DBUILD_opencv_python3=OFF \
    -DBUILD_opencv_shape=OFF \
    -DBUILD_opencv_stitching=OFF \
    -DBUILD_opencv_superres=OFF \
    -DBUILD_opencv_ts=OFF \
    -DBUILD_opencv_video=OFF \
    -DBUILD_opencv_videoio=OFF \
    -DBUILD_opencv_videostab=OFF \
    -DBUILD_opencv_world=ON \
    -DBUILD_opencv_dnn=ON \
    -DBUILD_opencv_gapi=OFF \
    -DWITH_IPP=OFF \
    -DWITH_FFMPEG=OFF \
    -DWITH_CUDA=OFF \
    -DWITH_CUFFT=OFF \
    -DWITH_EIGEN=OFF \
    -DWITH_JASPER=OFF \
    -DWITH_JPEG=OFF \
    -DWITH_PNG=OFF \
    -DWITH_PTHREADS_PF=OFF \
    -DWITH_OPENEXR=OFF \
    -DWITH_MATLAB=OFF \
    -DWITH_TBB=OFF \
    -DWITH_TIFF=OFF \
    -DWITH_WEBP=OFF \
    -DBUILD_opencv_alphamat=OFF \
    -DBUILD_opencv_aruco=OFF \
    -DBUILD_opencv_barcode=ON \
    -DBUILD_opencv_bgsegm=OFF \
    -DBUILD_opencv_bioinspired=OFF \
    -DBUILD_opencv_ccalib=OFF \
    -DBUILD_opencv_cnn_3dobj=OFF \
    -DBUILD_opencv_cvv=OFF \
    -DBUILD_opencv_datasets=OFF \
    -DBUILD_opencv_dnn_objdetect=OFF \
    -DBUILD_opencv_dnn_superres=OFF \
    -DBUILD_opencv_dnns_easily_fooled=OFF \
    -DBUILD_opencv_dpm=OFF \
    -DBUILD_opencv_face=OFF \
    -DBUILD_opencv_freetype=OFF \
    -DBUILD_opencv_fuzzy=OFF \
    -DBUILD_opencv_hdf=OFF \
    -DBUILD_opencv_hfs=OFF \
    -DBUILD_opencv_img_hash=OFF \
    -DBUILD_opencv_intensity_transform=OFF \
    -DBUILD_opencv_julia=OFF \
    -DBUILD_opencv_line_descriptor=OFF \
    -DBUILD_opencv_matlab=OFF \
    -DBUILD_opencv_mcc=OFF \
    -DBUILD_opencv_optflow=OFF \
    -DBUILD_opencv_ovis=OFF \
    -DBUILD_opencv_phase_unwrapping=OFF \
    -DBUILD_opencv_plot=OFF \
    -DBUILD_opencv_quality=OFF \
    -DBUILD_opencv_rapid=OFF \
    -DBUILD_opencv_reg=OFF \
    -DBUILD_opencv_rgbd=OFF \
    -DBUILD_opencv_saliency=OFF \
    -DBUILD_opencv_sfm=OFF \
    -DBUILD_opencv_shape=OFF \
    -DBUILD_opencv_stereo=OFF \
    -DBUILD_opencv_structured_light=OFF \
    -DBUILD_opencv_superres=OFF \
    -DBUILD_opencv_surface_matching=OFF \
    -DBUILD_opencv_text=OFF \
    -DBUILD_opencv_tracking=OFF \
    -DBUILD_opencv_videostab=OFF \
    -DBUILD_opencv_viz=OFF \
    -DBUILD_opencv_wechat_qrcode=ON \
    -DBUILD_opencv_xfeatures2d=OFF \
    -DBUILD_opencv_ximgproc=OFF \
    -DBUILD_opencv_xobjdetect=OFF \
    -DBUILD_opencv_xphoto=OFF \
    -DANDROID_NATIVE_API_LEVEL=${API_LEVEL} \
     ../../

    make clean
    make -j8
    make install
}

function mv_dirs {
    rm -rf $2
    mkdir -p $2
    mv $1/* $2/
}

function build_opencv {
    rm -rf $1
    mkdir $1
    cd $1

    build_opencv_modules $1 $2

    OPENCV_LIBS_PATH="./install/sdk/native/staticlibs/$1"
    OUTPUT_LIBS_PATH="../opencv/$1"
    mv_dirs ${OPENCV_LIBS_PATH} ${OUTPUT_LIBS_PATH}

    OTHER_LIBS_PATH="./install/sdk/native/3rdparty/libs/$1"
    OUTPUT_3RDPARTY_LIBS_PATH="../3rdparty/$1"
    mv_dirs ${OTHER_LIBS_PATH} ${OUTPUT_3RDPARTY_LIBS_PATH}

    INCLUDE_PATH="./install/sdk/native/jni/include"
    OUTPUT_INCLUDE_PATH="../opencv/include"
    mv_dirs ${INCLUDE_PATH} ${OUTPUT_INCLUDE_PATH}

    cd ..
    rm -rf $1

    cur_path=$(pwd)
    echo ${cur_path}
}


rm -rf build_android
mkdir build_android
cd build_android
mkdir opencv
mkdir 3rdparty

build_opencv armeabi-v7a 21
