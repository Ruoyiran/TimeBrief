//
// Created by royran on 2019/5/8.
//

#include "utils.h"
#include "stdlib.h"
#include <unistd.h>
#include <sstream>

namespace rpmssl {

bool execCmd(const std::string &cmd) {
    int ret = ::system(cmd.c_str());
    return ret == 0;
}

bool fileExists(const std::string &file_path) {
    if (file_path.empty()) {
        return false;
    }
    if (::access(file_path.c_str(), F_OK) == 0) {
        return true;
    }
    return errno != ENOENT;
}

bool safeCreateDirectory(const std::string &dir_path) {
    if (dir_path.empty()) {
        return false;
    }
    if (fileExists(dir_path)) {
        return true;
    }
    std::string cmd = "mkdir -p " + dir_path;
    return execCmd(cmd);
}

bool removeFile(const std::string &dir_path, bool force) {
    std::stringstream ss;
    ss << "rm -r ";
    if (force) {
        ss << "-f ";
    }
    ss << dir_path;
    std::string cmd = ss.str();
    return execCmd(cmd);
}

bool moveFile(const std::string &src_path, const std::string &dst_path) {
    std::stringstream ss;
    ss << "mv " << src_path << " " << dst_path;
    std::string cmd = ss.str();
    return execCmd(cmd);
}

}