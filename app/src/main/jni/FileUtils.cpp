#include "FileUtils.h"


#include <sys/ioctl.h>
#include <sys/stat.h>

#ifndef BLKGETSIZE64
# define BLKGETSIZE64   _IOR(0x12,114,size_t)
#endif

uint64_t get_block_device_size(int fd) {
    uint64_t size = 0;
    int ret;
    ret = ioctl(fd, BLKGETSIZE64, &size);
    if (ret)
        return 0;
    return size;
}

int64_t get_file_size(int fd) {
    struct stat buf;
    int ret;
    int64_t computed_size;
    ret = fstat(fd, &buf);
    if (ret)
        return 0;
    if (S_ISREG(buf.st_mode))
        computed_size = buf.st_size;
    else if (S_ISBLK(buf.st_mode))
        computed_size = get_block_device_size(fd);
    else
        computed_size = 0;
    return computed_size;
}
