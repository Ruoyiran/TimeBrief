//
// Created by royran on 2019/5/8.
//

#include "rpmssl_inner.h"
#include <iostream>
#include <sstream>
#include "utils.h"
#include "openssl/aes.h"
#include <string.h>
#include <fstream>
#include <vector>
#include "md5.h"

/**
 * rpmssl: RapidMinerSSL
 */
namespace rpmssl {

const int kAesBits = 256;

const char* kClearTextPasswordInner = "!!!PasswordIsSafety!!!";

static std::string encryptPassword(const std::string& password);

bool RpmsslInner::encryptFile(const std::string &input_file_path, const std::string &output_file_path) {
    std::ifstream in(input_file_path, std::ios::binary);
    if (in.fail()) {
        printf("error reading input file\n");
        return false;
    }
    std::string temp_out_file = output_file_path + ".tmp";
    std::ofstream out(temp_out_file, std::ios::binary);
    if (out.fail()) {
        printf("error writing output file\n");
        return false;
    }
    bool ok = encryptStream(in, out);
    if (ok) {
        ok = moveFile(temp_out_file, output_file_path);
    } else {
        removeFile(temp_out_file, true);
    }
    in.close();
    out.close();
    return ok;
}

bool RpmsslInner::decryptFile(const std::string &input_file_path, const std::string &output_file_path) {
    std::ifstream in(input_file_path, std::ios::binary);
    if (in.fail()) {
        printf("error reading input file\n");
        return false;
    }
    std::string temp_out_file = output_file_path + ".tmp";
    std::ofstream out(temp_out_file, std::ios::binary);
    if (out.fail()) {
        printf("error writing output file\n");
        return false;
    }
    bool ok = decryptStream(in, out);
    if (ok) {
        ok = moveFile(temp_out_file, output_file_path);
    } else {
        removeFile(temp_out_file, true);
    }
    in.close();
    out.close();
    return ok;
}

bool RpmsslInner::encryptFileToStream(const std::string &input_file_path, std::ostringstream &oss) {
    std::ifstream in(input_file_path, std::ios::binary);
    if (in.fail()) {
        printf("error reading input file\n");
        return false;
    }
    bool ok = encryptStream(in, oss);
    in.close();
    return ok;
}

bool RpmsslInner::decryptFileToStream(const std::string &input_file_path, std::ostringstream &oss) {
    std::ifstream in(input_file_path, std::ios::binary);
    if (in.fail()) {
        printf("error reading input file\n");
        return false;
    }
    bool ok = decryptStream(in, oss);
    in.close();
    return ok;
}

bool RpmsslInner::encryptString(const std::string &input_data, std::string &output_data) {
    std::istringstream iss(input_data);
    std::ostringstream oss;
    bool ok = encryptStream(iss, oss);
    output_data = oss.str();
    return ok;
}

bool RpmsslInner::decryptString(const std::string &input_data, std::string &output_data) {
    std::istringstream iss(input_data);
    std::ostringstream oss;
    bool ok = decryptStream(iss, oss);
    output_data = oss.str();
    return ok;
}

bool RpmsslInner::encryptStream(std::istream &input_stream, std::ostream &output_stream) {
    if (input_stream.fail() || output_stream.fail()) {
        return false;
    }
    std::string password = encryptPassword(kClearTextPasswordInner);

    unsigned char inbuf[4096];
    unsigned char outbuf[4096];

    int key_size = std::min<int>(kAesBits/8, password.size());
    unsigned char user_key[key_size];
    AES_KEY encrypt_key = {};
    size_t offset = 0;
    size_t len = 0;
    int outlen = 0;
    strncpy((char*)user_key, password.c_str(), key_size);

    AES_set_encrypt_key(user_key, kAesBits, &encrypt_key);
    while (true) {
        len = sizeof(inbuf) - AES_BLOCK_SIZE;
        input_stream.read((char *)inbuf, len);
        len = input_stream.gcount();
        if (len <= 0) {
            break;
        }

        outlen = len;
        if (input_stream.eof()) {  // file eof need padding
            outlen = (outlen / AES_BLOCK_SIZE + 1) * AES_BLOCK_SIZE;
            memset(inbuf + len, (outlen - len), outlen - len);  // padding
        }

        offset = 0;
        for (int i = 0; i < outlen / AES_BLOCK_SIZE; ++i) {
            AES_ecb_encrypt(inbuf + offset, outbuf + offset, &encrypt_key, AES_ENCRYPT);
            offset += AES_BLOCK_SIZE;
        }
        output_stream.write((char*)outbuf, outlen);
    }
    output_stream.flush();
    return true;
}

bool RpmsslInner::decryptStream(std::istream &input_stream, std::ostream &output_stream) {
    if (input_stream.fail() || output_stream.fail()) {
        return false;
    }
    std::string password = encryptPassword(kClearTextPasswordInner);
    unsigned char inbuf[4096];
    unsigned char outbuf[4096];
    int key_size = std::min<int>(kAesBits/8, password.size());
    unsigned char user_key[key_size];
    AES_KEY decrypt_key = {};
    size_t offset = 0;
    size_t len = 0;
    int outlen = 0;

    strncpy((char*)user_key, password.c_str(), key_size);

    AES_set_decrypt_key(user_key, kAesBits, &decrypt_key);
    while (true) {
        input_stream.read((char *)inbuf, sizeof(inbuf));
        len = input_stream.gcount();
        if (len <= 0) {
            break;
        }

        offset = 0;
        for (int i = 0; i < len / AES_BLOCK_SIZE; ++i) {
            AES_ecb_encrypt(inbuf + offset, outbuf + offset, &decrypt_key, AES_DECRYPT);
            offset += AES_BLOCK_SIZE;
        }

        outlen = len;

        if (input_stream.eof()) { // parser padding
            int padding = (int)outbuf[outlen - 1];
            for (int j = 0; j < padding; ++j) {
                if ((int)outbuf[outlen - j - 1] != padding) {
                    padding = 0;
                    break;
                }
            }
            outlen -= padding;
        }

        output_stream.write((char*)outbuf, outlen);
    }
    output_stream.flush();
    return true;
}

std::string encryptPassword(const std::string& password) {
    auto md5_str1 = MD5(password).toStr();
    auto md5_str2 = MD5(md5_str1).toStr();
    auto md5_str3 = MD5(md5_str2).toStr();
    auto md5_str4 = MD5(md5_str3).toStr();
    std::string enc_password;
    enc_password.append(md5_str4.begin()+3, md5_str4.begin()+11);
    enc_password.append(md5_str3.begin()+7, md5_str3.begin()+15);
    enc_password.append(md5_str2.begin()+1, md5_str2.begin()+9);
    enc_password.append(md5_str1.begin()+4, md5_str1.begin()+12);
    return enc_password;
}

}
