//
// Created by ranpeng on 2020-02-02.
//

#ifndef SSLANDROID_JNI_LOGGER_H
#define SSLANDROID_JNI_LOGGER_H

#include <android/log.h>


#define TAG "JNI_LOG"

#define INFO(...) __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__)

#define DEBUG(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

#define ERROR(...) __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)


#endif //SSLANDROID_JNI_LOGGER_H
