#ifndef FILEUTILS_H
#define FILEUTILS_H
// #include "FileUtils.h"

#include <string>
#include <cstdint>
#include <vector>

struct File {
public:
    std::string path;
    long size;
    bool isDirectory;
};

uint64_t get_block_device_size(int fd);

int64_t get_file_size(int fd);


std::vector<File> GetFiles(std::string &path);

bool IsDirectory(std::string &fileName, bool followLinks);

std::string file_extension(const std::string &path);

//static int64_t get_file_size(int fd, uint64_t reserve_len) {
//    struct stat buf;
//    int ret = fstat(fd, &buf);
//    if (ret) return 0;
//    int64_t computed_size;
//    if (S_ISREG(buf.st_mode)) {
//        computed_size = buf.st_size - reserve_len;
//    } else if (S_ISBLK(buf.st_mode)) {
//        uint64_t block_device_size = get_block_device_size(fd);
//        if (block_device_size < reserve_len ||
//            block_device_size > std::numeric_limits<int64_t>::max()) {
//            computed_size = 0;
//        } else {
//            computed_size = block_device_size - reserve_len;
//        }
//    } else {
//        computed_size = 0;
//    }
//    return computed_size;
//}
#endif