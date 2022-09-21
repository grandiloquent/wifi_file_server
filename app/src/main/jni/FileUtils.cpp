#include "FileUtils.h"


#include <sys/ioctl.h>
#include <sys/stat.h>
#include <dirent.h>
#include <fcntl.h>
#include <regex>
#include <unistd.h>

#ifndef BLKGETSIZE64
# define BLKGETSIZE64   _IOR(0x12,114,size_t)
#endif

std::vector<File> GetFiles(std::string &
path) {
    std::vector<File> files = {};

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
                        s.st_mtim.tv_sec
                });
            }
            //LOGE("%s", strerror(errno));
        }
    }
    closedir(dir);
    std::sort(files.begin(), files.end(), [](File &a, File &b) {
        if (a.isDirectory == b.isDirectory) {
            // a.path < b.path;
            return a.st_mtim > b.st_mtim;
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

bool IsDirectory(std::string &fileName, bool followLinks) {
    int status;
    struct stat statBuf;
    if (followLinks)
        status = stat(fileName.c_str(), &statBuf);
    else
        status = lstat(fileName.c_str(), &statBuf);
    status = (status == 0 && S_ISDIR(statBuf.st_mode));
    return status;
}

std::string file_extension(const std::string &path) {
    std::smatch m;
    static auto re = std::regex("\\.([a-zA-Z0-9]+)$");
    if (std::regex_search(path, m, re)) { return m[1].str(); }
    return std::string();
}

// https://android.googlesource.com/platform/system/core/+/kitkat-release/toolbox/rm.c

#define OPT_FORCE     2

/* return -1 on failure, with errno set to the first error */
static int unlink_recursive(const char *name, int flags) {
    struct stat st;
    DIR *dir;
    struct dirent *de;
    int fail = 0;
    /* is it a file or directory? */
    if (lstat(name, &st) < 0)
        return ((flags & OPT_FORCE) && errno == ENOENT) ? 0 : -1;
    /* a file, so unlink it */
    if (!S_ISDIR(st.st_mode))
        return unlink(name);
    /* a directory, so open handle */
    dir = opendir(name);
    if (dir == NULL)
        return -1;
    /* recurse over components */
    errno = 0;
    while ((de = readdir(dir)) != NULL) {
        char dn[PATH_MAX];
        if (!strcmp(de->d_name, "..") || !strcmp(de->d_name, "."))
            continue;
        sprintf(dn, "%s/%s", name, de->d_name);
        if (unlink_recursive(dn, flags) < 0) {
            if (!(flags & OPT_FORCE)) {
                fail = 1;
                break;
            }
        }
        errno = 0;
    }
    /* in case readdir or unlink_recursive failed */
    if (fail || errno < 0) {
        int save = errno;
        closedir(dir);
        errno = save;
        return -1;
    }
    /* close directory handle */
    if (closedir(dir) < 0)
        return -1;
    /* delete target directory */
    return rmdir(name);
}
