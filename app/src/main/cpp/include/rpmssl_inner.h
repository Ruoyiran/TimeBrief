//
// Created by royran on 2019/5/8.
//

#ifndef DATA_CENTER_SECURITY_RPMSSL_INNER_H
#define DATA_CENTER_SECURITY_RPMSSL_INNER_H

#include <string>

/**
 * RapidMinerSSL 数据中台内部数据加密工具，对应的库文件：librpmsslm.a
 */
namespace rpmssl {

class RpmsslInner {

public:
    static bool encryptFile(const std::string &input_file_path, const std::string &output_file_path);

    static bool decryptFile(const std::string &input_file_path, const std::string &output_file_path);

    static bool encryptFileToStream(const std::string &input_file_path, std::ostringstream &oss);

    static bool decryptFileToStream(const std::string &input_file_path, std::ostringstream &oss);

    static bool encryptString(const std::string &input_data, std::string &output_data);

    static bool decryptString(const std::string &input_data, std::string &output_data);

    static bool encryptStream(std::istream &input_stream, std::ostream &output_stream);

    static bool decryptStream(std::istream &input_stream, std::ostream &output_stream);

};

}

#endif //DATA_CENTER_SECURITY_RPMSSL_INNER_H
