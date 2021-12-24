#include <string>
#include "StringUtils.h"
//#include <android/log.h>
//
//#define LOG_TAG "TAG/Native"
//#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
//#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

bool endsWith(const std::string &mainStr, const std::string &toMatch) {
    if (mainStr.size() >= toMatch.size() &&
        mainStr.compare(mainStr.size() - toMatch.size(), toMatch.size(), toMatch) == 0)
        return true;
    else
        return false;
}

std::string SubstringAfterLast(const std::string &value,
                               const std::string &str) {
    auto index = value.find_last_of(str);
    if (index != std::string::npos)
        return value.substr(index + str.length());
    else
        return std::string();
}

std::string SubstringBeforeLast(const std::string &value,
                                const std::string &str) {
    auto index = value.find_last_of(str);
    if (index != std::string::npos)
        return value.substr(0, index);
    else
        return std::string();
}
