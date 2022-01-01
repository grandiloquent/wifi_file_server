#include <jni.h>
#include <android/log.h>
#include "httplib.h"
#include "java_interop.h"
#include "qrcode.h"
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include "ArduinoJson/ArduinoJson.h"
#include <errno.h>
#include <filesystem>

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

std::string getExternalStoragePath(JNIEnv *env, jobject context) {
    jclass activity = env->FindClass("euphoria/psycho/fileserver/MainActivity");
    jmethodID m = env->GetStaticMethodID(activity, "getExternalStoragePath",
                                         "(Landroid/content/Context;)Ljava/lang/String;");

    auto result = reinterpret_cast<jstring>( env->CallStaticObjectMethod(activity, m, context));
    jboolean isCopy;
    const char *convertedValue = (env)->GetStringUTFChars(result, &isCopy);
    env->DeleteLocalRef(activity);

    std::string storage(convertedValue);

    env->ReleaseStringUTFChars(result, convertedValue);
    return storage;
}

extern "C" JNICALL jboolean
Java_euphoria_psycho_fileserver_MainActivity_startServer(JNIEnv *env, jclass obj, jobject context,
                                                         jstring ip,
                                                         jstring resourceDirectory,
                                                         jint port) {

    std::string storage = getExternalStoragePath(env, context);

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
    server.Get("/videos", [](const Request &req, Response &res) {
        unsigned char *data;
        unsigned int len = 0;
        readBytesAsset(manager, "videos.html", &data, &len);
        res.set_content(reinterpret_cast<const char *>(data), len, "text/html");
        free(data);
    });
    // https://android.googlesource.com/platform/frameworks/native/+/master/libs/diskusage/dirsize.c

    server.Get("/api/remove", [](const Request &req, Response &res) {
        std::string path = "/storage/emulated/0";
        if (req.has_param("v")) {
            path = req.get_param_value("v");
        }
        if (!IsDirectory(path, false)) {
            std::filesystem::path p = path;
            std::filesystem::create_directories(p.parent_path() / "Recycle");
            std::filesystem::rename(path, p.parent_path() / "Recycle" / p.filename());
            res.set_content("OK", "text/plain");
        }
    });
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
            res.set_header("Access-Control-Allow-Origin", "*");
            //
            res.set_header("Content-Disposition",
                           "'attachment; filename=\"" + SubstringAfterLast(path, "/") + "\"'");
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
        DynamicJsonDocument doc(1024 * 512);
        JsonArray arr = doc.to<JsonArray>();

        for (auto &file:files) {
            JsonObject object = arr.createNestedObject();
            object["path"] = file.path;
            object["size"] = file.size;
            object["isDirectory"] = file.isDirectory;
        }
        std::string result;
        serializeJson(doc, result);

        res.set_header("Access-Control-Allow-Origin", "*");
        res.set_content(result.c_str(), "application/json");

    });
    server.Get(R"(/static/([a-zA-Z\\._-]+))", [](const Request &req, Response &res) {

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
        auto v = req.get_file_value("v");
        std::string path{"/storage/emulated/0/"};
        auto file = req.get_file_value("file");

        if (!v.content.empty())
            path = v.content + "/" + file.filename;
        else
            path = path + file.filename;
        std::ofstream ofs(path, std::ios::binary);
        ofs << file.content;
        res.set_content("done", "text/plain");
    });
    server.Get("/api/file", [](const Request &req, Response &res) {

        if (req.has_param("old") && req.has_param("new")) {
            auto old = req.get_param_value("old");
            auto newPath = SubstringBeforeLast(old, "/");
            LOGE("%s", newPath.c_str());
            newPath.append("/")
                    .append(req.get_param_value("new"));
            int status;
            struct stat statBuf;
            status = lstat(newPath.c_str(), &statBuf);
            if (status) {
                LOGE("%s %s", newPath.c_str(), old.c_str());
                rename(old.c_str(), newPath.c_str());
            }
            res.set_header("Access-Control-Allow-Origin", "*");
            res.status = 200;
        } else {
            res.status = 403;
            return;
        }
    });

    server.Get("/api/storage", [&](const Request &req, Response &res) {

        res.set_content(storage, "text/plain");
    });

    server.Get(R"(/books/(.*))", [&](const Request &req, Response &res) {
        auto query = req.matches[1];
        std::string dir = "/storage/emulated/0/Books";
        if (query.length() == 0) {
            std::stringstream ss;
            ss << R"(<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
</head>

<body>
 <style>
        html,
        body,
        h1 {
            font-family: Roboto, Helvetica Neue, Arial, sans-serif;
        }

        body,
        h1 {
            font-size: small;
        }

        body {
            margin: 0;
            background: #fff;
            color: #4d5156;
        }

        a {
            color: #1558d6;
            text-decoration: none;
            -webkit-tap-highlight-color: transparent;
            display: block;
        }

        a {
            display: block;
            background-color: #fff;
            /* text-align: center; */
            font-size: 14px;
            color: #202124;
            /* border-radius: 20px; */
            height: 24px;
            line-height: 24px;
            border-bottom: 1px solid #dadce0;
            /* margin: 16px 16px 22px 16px; */
            padding: 8px 13px;
        }
    </style>
)";
            for (auto const &dir_entry: std::filesystem::directory_iterator{dir}) {
                ss << "<a href=\"" << dir_entry.path().filename().string()
                   << "\">"
                   << dir_entry.path().filename().string()
                   << "</a>";
            }
            ss << R"(</body>

</html>)";
            res.set_content(ss.str(), "text/html");
            return;
        }
        auto  path=dir + "/" + query.str();
        if (std::filesystem::is_directory(path)) {
            std::stringstream ss;
            ss << R"(<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
</head>

<body>
 <style>
        html,
        body,
        h1 {
            font-family: Roboto, Helvetica Neue, Arial, sans-serif;
        }

        body,
        h1 {
            font-size: small;
        }

        body {
            margin: 0;
            background: #fff;
            color: #4d5156;
        }

        a {
            color: #1558d6;
            text-decoration: none;
            -webkit-tap-highlight-color: transparent;
            display: block;
        }

        a {
            display: block;
            background-color: #fff;
            /* text-align: center; */
            font-size: 14px;
            color: #202124;
            /* border-radius: 20px; */
            height: 24px;
            line-height: 24px;
            border-bottom: 1px solid #dadce0;
            /* margin: 16px 16px 22px 16px; */
            padding: 8px 13px;
        }
    </style>
)";
            for (auto const &dir_entry: std::filesystem::directory_iterator{dir+"/"+query.str()}) {
                    ss << "<a href=\""
                            << dir_entry.path().parent_path().filename().string()
                            <<"/"
                    << dir_entry.path().filename().string()
                       << "\">"
                       << dir_entry.path().filename().string()
                       << "</a>";
            }
            ss << R"(</body>

</html>)";
            res.set_content(ss.str(), "text/html");
        } else {

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

        }


    });


    server.listen(host.c_str(), port);

    return 0;
}

extern "C"
JNIEXPORT jint

JNICALL
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
Java_euphoria_psycho_fileserver_MainActivity_load(JNIEnv
                                                  *env,
                                                  jclass clazz,
                                                  jobject
                                                  assetManager) {
    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
    manager = mgr;
}

