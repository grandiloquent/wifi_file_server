#include <string>
#include "StringUtils.h"

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