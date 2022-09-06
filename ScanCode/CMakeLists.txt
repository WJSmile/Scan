# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)
# Declares and names the project.

project("scancode")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fno-exceptions -frtti")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Os -ffunction-sections -fdata-sections -fvisibility=hidden")
set(CMAKE_CXX_FLAGS "${CMAKE_C_FLAGS}")


set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Os  -Wl,--exclude-libs,ALL -Wl,--gc-sections,-s -Wl,--version-script=${CMAKE_CURRENT_SOURCE_DIR}/version_script.txt")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
file(GLOB native_file src/main/cpp/*.c src/main/cpp/*.cpp)
add_library( # Sets the name of the library.
        scancode

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${native_file})

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

set(JNI_LIBS_DIR ${CMAKE_SOURCE_DIR}/libs)

add_subdirectory(src/main/cpp/zxing)
include_directories(src/main/cpp/zxing)

add_library(libiconv
        STATIC
        IMPORTED)
set_target_properties(libiconv
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libiconv.a)


add_library(libzbarjni
        STATIC
        IMPORTED)
set_target_properties(libzbarjni
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libzbarjni.a)


add_library(libcpufeatures
        STATIC
        IMPORTED)
set_target_properties(libcpufeatures
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libcpufeatures.a)


add_library(libittnotify
        STATIC
        IMPORTED)
set_target_properties(libittnotify
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libittnotify.a)


add_library(libopencv_world
        STATIC
        IMPORTED)
set_target_properties(libopencv_world
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libopencv_world.a)


add_library(liblibopenjp2
        STATIC
        IMPORTED)
set_target_properties(liblibopenjp2
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/liblibopenjp2.a)

add_library(libtegra_hal
        STATIC
        IMPORTED)
set_target_properties(libtegra_hal
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libtegra_hal.a)

add_library(libzlib
        STATIC
        IMPORTED)
set_target_properties(libzlib
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/libzlib.a)


add_library(liblibprotobuf
        STATIC
        IMPORTED)
set_target_properties(liblibprotobuf
        PROPERTIES IMPORTED_LOCATION
        ${JNI_LIBS_DIR}/${ANDROID_ABI}/liblibprotobuf.a)


include_directories(${JNI_LIBS_DIR}/include)

target_link_libraries(
        scancode
        ZXing
        libzbarjni
        libiconv
        libopencv_world
        liblibprotobuf
        libtegra_hal
        libcpufeatures
        libittnotify
        liblibopenjp2
        libzlib
        jnigraphics
        ${log-lib})