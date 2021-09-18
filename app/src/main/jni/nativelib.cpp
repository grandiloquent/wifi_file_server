#include <jni.h>
#include <android/log.h>
#include <dirent.h>
#include "httplib.h"
#include "java_interop.h"
#include "qrcode.h"

#define LOG_TAG "TAG/Native"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace httplib;

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
        res.set_content("Hello World!", "text/html");
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

    uint16_t buf[qrcode.size * qrcode.size];
    memset(buf, 0, qrcode.size * qrcode.size);

    for (uint8_t y = 0; y < qrcode.size; y++) {
        for (uint8_t x = 0; x < qrcode.size; x++) {
            if (qrcode_getModule(&qrcode, x, y)) {
                //LOGE("%d %d ", x + y * qrcode.size, qrcode.size);
                buf[x + y * qrcode.size] = 1;
            } else {
                buf[x + y * qrcode.size] = 0;
            }
        }
    }
    jbyte result[qrcode.size * qrcode.size];
    for (int i = 0; i < qrcode.size * qrcode.size; i++) {
        result[i] = buf[i];
    }

    env->SetByteArrayRegion(buffer, 0, qrcode.size * qrcode.size,
                            result);
    return 0;
}