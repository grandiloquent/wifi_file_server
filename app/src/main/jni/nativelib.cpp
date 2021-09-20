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
#include "StringUtils.h"

#define LOG_TAG "TAG/Native"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace httplib;

static AAssetManager *manager = nullptr;

static std::map<std::string, std::string> mimetypes{
        {"css",   "text/css"},
        {"mpga",  "audio/mpeg"},
        {"csv",   "text/csv"},
        {"weba",  "audio/webm"},
        {"txt",   "text/plain"},
        {"wav",   "audio/wave"},
        {"vtt",   "text/vtt"},
        {"otf",   "font/otf"},
        {"html",  "text/html"},
        {"htm",   "text/html"},
        {"ttf",   "font/ttf"},
        {"apng",  "image/apng"},
        {"woff",  "font/woff"},
        {"avif",  "image/avif"},
        {"woff2", "font/woff2"},
        {"bmp",   "image/bmp"},
        {"7z",    "application/x-7z-compressed"},
        {"gif",   "image/gif"},
        {"atom",  "application/atom+xml"},
        {"png",   "image/png"},
        {"pdf",   "application/pdf"},
        {"svg",   "image/svg+xml"},
        {"mjs",   "application/javascript"},
        {"js",    "application/javascript"},
        {"webp",  "image/webp"},
        {"json",  "application/json"},
        {"ico",   "image/x-icon"},
        {"rss",   "application/rss+xml"},
        {"tif",   "image/tiff"},
        {"tar",   "application/x-tar"},
        {"tiff",  "image/tiff"},
        {"xhtml", "application/xhtml+xml"},
        {"xht",   "application/xhtml+xml"},
        {"jpeg",  "image/jpeg"},
        {"jpg",   "image/jpeg"},
        {"xslt",  "application/xslt+xml"},
        {"mp4",   "video/mp4"},
        {"xml",   "application/xml"},
        {"mpeg",  "video/mpeg"},
        {"gz",    "application/gzip"},
        {"webm",  "video/webm"},
        {"zip",   "application/zip"},
        {"mp3",   "audio/mp3"},
        {"wasm",  "application/wasm"},
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
            auto extension = SubstringAfterLast(path, ".");
            auto type = mimetypes[extension];
            if (type.empty()) {
                type = "application/octet-stream";
            }
            std::shared_ptr<std::ifstream> fs = std::make_shared<std::ifstream>();
            fs->open(path, std::ios_base::binary);
            fs->seekg(0, std::ios_base::end);
            auto end = fs->tellg();
            fs->seekg(0);
            std::map<std::string, std::string> file_extension_and_mimetype_map;
            res.set_content_provider(static_cast<size_t>(end),
                                     type.c_str(),
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
        auto extension = SubstringAfterLast(filename, ".");
        auto type = mimetypes[extension];
        if (type.empty()) {
            type = "application/octet-stream";
        }
        res.set_content(reinterpret_cast<const char *>(data), len, type.c_str());
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

    env->SetByteArrayRegion(buffer, 0, size,
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

