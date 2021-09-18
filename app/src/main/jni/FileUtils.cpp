#include "FileUtils.h"


#include <sys/ioctl.h>
#include <sys/stat.h>
#include <dirent.h>
#include <fcntl.h>

#ifndef BLKGETSIZE64
# define BLKGETSIZE64   _IOR(0x12,114,size_t)
#endif

std::vector<File> GetFiles(std::string&
                                   path) {
    std::vector <File> files = {};

    struct dirent *entry;
    DIR *dir = opendir(path.c_str());
    if (dir == nullptr) {
        return files;
    }
    struct stat s;
    int dfd = dirfd(dir);
    while ((entry = readdir(dir)) != nullptr) {
        if (!strcmp(entry->d_name, ".") || !strcmp(entry->d_name, "..")) continue;
        std::string fullPath = {path + "/" + entry->d_name};
        if (entry->d_type == DT_DIR) {
            files.push_back(File{
                    fullPath,
                    0,
                    true,
            });
        } else {
            if (fstatat(dfd, entry->d_name, &s, AT_SYMLINK_NOFOLLOW) == 0) {
                files.push_back(File{
                        fullPath,
                        s.st_blocks * 512,
                        false,
                });
            }
            //LOGE("%s", strerror(errno));
        }
    }
    closedir(dir);
    std::sort(files.begin(), files.end(), [](File &a, File &b) {
        if (a.isDirectory == b.isDirectory) {
            return a.path < b.path;
        } else if (a.isDirectory) {
            return true;
        }
        return false;
    });
    return files;
}

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
