//
// Created by ranpeng on 2020-02-02.
//

#include "com_royran_timebrief_ssl_RpmSSL.h"

#include "jni_logger.h"
#include "rpmssl_inner.h"

#ifdef __cplusplus
extern "C" {
#endif

jbyteArray Java_com_royran_timebrief_ssl_RpmSSL_encryptBytes(JNIEnv *env, jobject, jbyteArray j_array) {
    jbyte *c_array = env->GetByteArrayElements(j_array, 0);
    jsize arr_len = env->GetArrayLength(j_array);
    if (arr_len == 0) {
        ERROR("data is empty");
        return nullptr;
    }
    char* buf = (char*)c_array;
    std::string input_str(buf, arr_len);
    std::string enc;
    bool ok = rpmssl::RpmsslInner::encryptString(input_str, enc);
    env->ReleaseByteArrayElements(j_array, c_array, 0);
    if (!ok) {
        ERROR("encryptString failed");
        return nullptr;
    }
    DEBUG("encryptString succeed, origin length: %d, encode length: %d", arr_len, enc.length());
    int enc_len = enc.length();
    jbyteArray c_result = env->NewByteArray(enc_len);
    env->SetByteArrayRegion(c_result, 0, enc_len, reinterpret_cast<jbyte*>(const_cast<char*>(enc.c_str())));
    return c_result;
}

jbyteArray Java_com_royran_timebrief_ssl_RpmSSL_decryptBytes(JNIEnv *env, jobject, jbyteArray j_array) {
    jbyte *c_array = env->GetByteArrayElements(j_array, 0);
    jsize arr_len = env->GetArrayLength(j_array);
    if (arr_len == 0) {
        ERROR("data is empty");
        return nullptr;
    }
    char* buf = (char*)c_array;
    std::string input_str(buf, arr_len);
    std::string dec;
    bool ok = rpmssl::RpmsslInner::decryptString(input_str, dec);
    env->ReleaseByteArrayElements(j_array, c_array, 0);
    if (!ok) {
        ERROR("decryptString failed");
        return nullptr;
    }
    DEBUG("decryptString succeed, origin length: %d, decode length: %d", arr_len, dec.length());
    int enc_len = dec.length();
    jbyteArray c_result = env->NewByteArray(enc_len);
    env->SetByteArrayRegion(c_result, 0, enc_len, reinterpret_cast<jbyte*>(const_cast<char*>(dec.c_str())));
    return c_result;
}

#ifdef __cplusplus
}
#endif