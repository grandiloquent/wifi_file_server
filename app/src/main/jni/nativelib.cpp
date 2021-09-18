#include <jni.h>
#include <android/log.h>
#include <dirent.h>
#include "httplib.h"
#include "java_interop.h"
#include "qrcode.h"
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include "ArduinoJson/ArduinoJson.h"

#define LOG_TAG "TAG/Native"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace httplib;

struct File {
public:
    std::string path;
    long size;
    bool isDirectory;
};
static AAssetManager *manager = nullptr;

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


bool readBytesAsset(std::string_view filename, unsigned char **data, unsigned int *len) {
    AAssetManager *aAssetManager = manager;
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

extern "C" {
JNIEXPORT jboolean JNICALL
Java_euphoria_psycho_fileserver_MainActivity_startServer(JNIEnv *env, jclass obj,
                                                         jstring ip,
                                                         jstring resourceDirectory,
                                                         jint port);
};

extern "C" JNIEXPORT jboolean JNICALL
Java_euphoria_psycho_fileserver_MainActivity_startServer(JNIEnv *env, jclass obj,
                                                         jstring ip,
                                                         jstring resourceDirectory,
                                                         jint port) {

    const std::string directory =
            jsonparse::jni::Convert<std::string>::from(env, resourceDirectory);
    const std::string host =
            jsonparse::jni::Convert<std::string>::from(env, ip);
    Server server;
    server.Get("/", [](const Request &req, Response &res) {
//        auto path = "/storage/emulated/0";
//        struct dirent *entry;
//        DIR *dir = opendir(path);
//        if (dir == NULL) {
//            return;
//        }
//        while ((entry = readdir(dir)) != NULL) {
//            if (!strcmp(entry->d_name, ".") || !strcmp(entry->d_name, "..")) continue;
//            LOGE("%s\n", entry->d_name);
//        }
//
//        closedir(dir);
        unsigned char *data;
        unsigned int len = 0;
        readBytesAsset("index.html", &data, &len);
        res.set_content(reinterpret_cast<const char *>(data), len, "text/html");
        free(data);
    });
    server.Get("/api/files", [](const Request &req, Response &res) {
        std::string path = "/storage/emulated/0";
        struct dirent *entry;
        DIR *dir = opendir(path.c_str());
        if (dir == nullptr) {
            res.status = 404;
            return;
        }
        std::vector <File> files = {};
        struct stat s;
        while ((entry = readdir(dir)) != nullptr) {
            if (!strcmp(entry->d_name, ".") || !strcmp(entry->d_name, "..")) continue;
            std::string fullPath = {path + "/" + entry->d_name};
            lstat(path.c_str(), &s);
            if (S_ISREG(s.st_mode) == 0) {
                files.push_back(File{
                        fullPath,
                        s.st_size,
                        false,
                });
            } else {
                files.push_back(File{
                        fullPath,
                        0,
                        true,
                });
            }
        }
        std::sort(files.begin(), files.end(), [](File &a, File &b) {
            if (a.isDirectory && b.isDirectory) {
                return a.path.compare(b.path);
            } else if (a.isDirectory) {
                return 1;
            }
            return 0;
        });

        DynamicJsonDocument doc(1024);
        JsonArray arr = doc.as<JsonArray>();
        JsonObject object = arr.createNestedObject();
        object["path"] = "123";
        std::string result;
        serializeJson(doc, result);

        closedir(dir);
        res.set_content(result.c_str(), "application/json");

    });
    server.listen(host.c_str(), port);

    return 0;
}
extern "C"
JNIEXPORT jint JNICALL
Java_euphoria_psycho_fileserver_MainActivity_makeQrCode(JNIEnv *env, jclass clazz, jstring value,
                                                        jbyteArray buffer) {
    QRCode qrcode;
    uint8_t qrCodeBytes[qrcode_getBufferSize(3)];
    const std::string str =
            jsonparse::jni::Convert<std::string>::from(env, value);
    qrcode_initText(&qrcode, qrCodeBytes, 3, ECC_LOW, str.c_str());

    uint16_t size = qrcode.size * qrcode.size;
    uint16_t buf[size];

    for (uint8_t y = 0; y < qrcode.size; y++) {
        for (uint8_t x = 0; x < qrcode.size; x++) {
            if (qrcode_getModule(&qrcode, x, y)) {
                buf[x + y * qrcode.size] = 1;
            } else {
                buf[x + y * qrcode.size] = 0;
            }
        }
    }
    jbyte result[size];
    for (int i = 0; i < size; i++) {
        result[i] = buf[i];
    }

    env->SetByteArrayRegion(buffer, 0, qrcode.size * qrcode.size,
                            result);
    return 0;
}
extern "C"
JNIEXPORT void JNICALL
Java_euphoria_psycho_fileserver_MainActivity_load(JNIEnv *env, jclass clazz, jobject assetManager) {
    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
    manager = mgr;
}

