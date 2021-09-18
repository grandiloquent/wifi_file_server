#ifndef ANDROIDUTILS_H
#define ANDROIDUTILS_H
// #include "AndroidUtils.h"

#include <stdbool.h>
#include <android/asset_manager.h>
#include <string>
bool readBytesAsset(AAssetManager *aAssetManager, std::string_view filename, unsigned char **data,
                    unsigned int *len);

#endif