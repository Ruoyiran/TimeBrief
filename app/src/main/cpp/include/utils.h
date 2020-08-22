//
// Created by royran on 2019/5/8.
//

#ifndef DATA_CENTER_SECURITY_UTILS_H
#define DATA_CENTER_SECURITY_UTILS_H

#include <string>

namespace rpmssl {

bool execCmd(const std::string &cmd);

bool fileExists(const std::string &file_path);

bool safeCreateDirectory(const std::string &dir_path);

bool removeFile(const std::string &dir_path, bool force = true);

bool moveFile(const std::string &src_path, const std::string &dst_path);

}


#endif //DATA_CENTER_SECURITY_UTILS_H
