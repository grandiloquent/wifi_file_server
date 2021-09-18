#include "AndroidUtils.h"

bool readBytesAsset(AAssetManager *aAssetManager, std::string_view filename, unsigned char **data,
                    unsigned int *len) {

    AAsset *aAsset = AAssetManager_open(aAssetManager, filename.data(), AASSET_MODE_BUFFER);
    if (aAsset == nullptr) {
        *data = nullptr;
        if (len) *len = 0;
        return false;
    }
    auto size = (unsigned int) AAsset_getLength(aAsset);
    *data = (unsigned char *) malloc(size);
    AAsset_read(aAsset, *data, size);
    if (len) *len = size;

    AAsset_close(aAsset);
    return true;
}