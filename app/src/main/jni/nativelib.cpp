#include <jni.h>
#include <android/log.h>
#include "httplib.h"
#include "java_interop.h"
#include "qrcode.h"
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include "ArduinoJson/ArduinoJson.h"
#include <errno.h>

#include "FileUtils.h"
#include "AndroidUtils.h"

#define LOG_TAG "TAG/Native"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace httplib;

static AAssetManager *manager = nullptr;

bool endsWith(const std::string &mainStr, const std::string &toMatch) {
    if (mainStr.size() >= toMatch.size() &&
        mainStr.compare(mainStr.size() - toMatch.size(), toMatch.size(), toMatch) == 0)
        return true;
    else
        return false;
}

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
        unsigned char *data;
        unsigned int len = 0;
        readBytesAsset(manager, "index.html", &data, &len);
        res.set_content(reinterpret_cast<const char *>(data), len, "text/html");
        free(data);
    });
    // https://android.googlesource.com/platform/frameworks/native/+/master/libs/diskusage/dirsize.c
    server.Get("/api/files", [](const Request &req, Response &res) {
        std::string path = "/storage/emulated/0";
        if (req.has_param("v")) {
            path = req.get_param_value("v");
        }
        if (!IsDirectory(path, false)) {
            auto extension = file_extension(path);
            auto type = "application/octet-stream";

            if (extension == "css") {
                type = "text/css";
            } else if (extension == "mpga") {
                type = "audio/mpeg";
            } else if (extension == "csv") {
                type = "text/csv";
            } else if (extension == "weba") {
                type = "audio/webm";
            } else if (extension == "txt") {
                type = "text/plain";
            } else if (extension == "wav") {
                type = "audio/wave";
            } else if (extension == "vtt") {
                type = "text/vtt";
            } else if (extension == "otf") {
                type = "font/otf";
            } else if (extension == "html" || extension == "htm") {
                type = "text/html";
            } else if (extension == "ttf") {
                type = "font/ttf";
            } else if (extension == "apng") {
                type = "image/apng";
            } else if (extension == "woff") {
                type = "font/woff";
            } else if (extension == "avif") {
                type = "image/avif";
            } else if (extension == "woff2") {
                type = "font/woff2";
            } else if (extension == "bmp") {
                type = "image/bmp";
            } else if (extension == "7z") {
                type = "application/x-7z-compressed";
            } else if (extension == "gif") {
                type = "image/gif";
            } else if (extension == "atom") {
                type = "application/atom+xml";
            } else if (extension == "png") {
                type = "image/png";
            } else if (extension == "pdf") {
                type = "application/pdf";
            } else if (extension == "svg") {
                type = "image/svg+xml";
            } else if (extension == "js" || extension == "mjs") {
                type = "application/javascript";
            } else if (extension == "webp") {
                type = "image/webp";
            } else if (extension == "json") {
                type = "application/json";
            } else if (extension == "ico") {
                type = "image/x-icon";
            } else if (extension == "rss") {
                type = "application/rss+xml";
            } else if (extension == "tif" || extension == "tiff") {
                type = "image/tiff";
            } else if (extension == "tar") {
                type = "application/x-tar";
            } else if (extension == "xhtml" || extension == "xht") {
                type = "application/xhtml+xml";
            } else if (extension == "jpg" || extension == "jpeg") {
                type = "image/jpeg";
            } else if (extension == "xslt") {
                type = "application/xslt+xml";
            } else if (extension == "mp4") {
                type = "video/mp4";
            } else if (extension == "xml") {
                type = "application/xml";
            } else if (extension == "mpeg") {
                type = "video/mpeg";
            } else if (extension == "gz") {
                type = "application/gzip";
            } else if (extension == "webm") {
                type = "video/webm";
            } else if (extension == "zip") {
                type = "application/zip";
            } else if (extension == "mp3") {
                type = "audio/mp3";
            } else if (extension == "wasm") {
                type = "application/wasm";
            }

            std::shared_ptr<std::ifstream> fs = std::make_shared<std::ifstream>();
            fs->open(path, std::ios_base::binary);
            fs->seekg(0, std::ios_base::end);
            auto end = fs->tellg();
            fs->seekg(0);
            std::map<std::string, std::string> file_extension_and_mimetype_map;
            res.set_content_provider(static_cast<size_t>(end),
                                     type,
                                     [fs](uint64_t offset,
                                          uint64_t length,
                                          DataSink &sink) {
                                         if (fs->fail()) {
                                             return false;
                                         }

                                         fs->seekg(offset, std::ios_base::beg);

                                         size_t bufSize = 81920;
                                         char buffer[bufSize];

                                         fs->read(buffer, bufSize);

                                         sink.write(buffer,
                                                    static_cast<size_t>(fs->gcount()));
                                         return true;
                                     });
            return;
        }
        auto files = GetFiles(path);
        if (files.empty()) {
            res.status = 404;
            return;
        }
        DynamicJsonDocument doc(1024 * 32);
        JsonArray arr = doc.to<JsonArray>();

        for (auto &file:files) {
            JsonObject object = arr.createNestedObject();
            object["path"] = file.path;
            object["size"] = file.size;
            object["isDirectory"] = file.isDirectory;
        }
        std::string result;
        serializeJson(doc, result);

        res.set_content(result.c_str(), "application/json");

    });
    server.Get(R"(/static/([a-zA-Z\\.-]+))", [](const Request &req, Response &res) {

        auto value = req.matches[1];
        auto filename = value.str();
        unsigned char *data;
        unsigned int len = 0;
        readBytesAsset(manager, filename.c_str(), &data, &len);

        res.set_header("Cache-Control", "max-age=259200");
        if (endsWith(filename, ".css"))
            res.set_content(reinterpret_cast<const char *>(data), len, "text/css");
        else if (endsWith(filename, ".js"))
            res.set_content(reinterpret_cast<const char *>(data), len, "application/javascript");
        else if (endsWith(filename, ".svg")) {
            res.set_content(reinterpret_cast<const char *>(data), len, "image/svg+xml");
        } else
            res.set_content(reinterpret_cast<const char *>(data), len, "image/*");

        free(data);
    });
    server.Post("/post", [](const Request &req, Response &res) {
        auto file = req.get_file_value("file");
        std::string path{"/storage/emulated/0/Download/"};
        std::ofstream ofs(path + file.filename, std::ios::binary);
        ofs << file.content;
        res.set_content("done", "text/plain");
    });
    server.listen(host.c_str(), port);

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_euphoria_psycho_fileserver_MainActivity_makeQrCode(JNIEnv *env, jclass clazz,
                                                        jstring value,
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
Java_euphoria_psycho_fileserver_MainActivity_load(JNIEnv *env, jclass clazz,
                                                  jobject assetManager) {
    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
    manager = mgr;
}

