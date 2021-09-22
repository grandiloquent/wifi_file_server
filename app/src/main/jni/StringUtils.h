#ifndef STRINGUTILS_H
#define STRINGUTILS_H
// #include "StringUtils.h"

bool endsWith(const std::string &mainStr, const std::string &toMatch);
std::string SubstringAfterLast(const std::string &value,
                               const std::string &str);
std::string SubstringBeforeLast(const std::string &value,
                                const std::string &str);
#endif