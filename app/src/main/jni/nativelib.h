#ifndef NATIVELIB_H
#define NATIVELIB_H
// #include "nativelib.h"

#define LOG_TAG "TAG/Native"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

std::string findMimeType(std::string &path) {

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
    auto extension = SubstringAfterLast(path, ".");
    auto type = mimetypes[extension];
    if (type.empty()) {
        type = "application/octet-stream";
    }
    return type;
}

void handlingVideoRequests(const httplib::Request &req, httplib::Response &res) {
    LOGE("%s", req.path.c_str());
    res.set_content("", "text/plain");
}
//
void handlingDeleteFileRequests(const httplib::Request &req, httplib::Response &res){
    res.set_header("Access-Control-Allow-Origin", "*");
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
}
void handlingMoveFileRequests(const httplib::Request &req, httplib::Response &res){
    res.set_header("Access-Control-Allow-Origin", "*");
    std::string path = "/storage/emulated/0";
    if (req.has_param("v")) {
        path = req.get_param_value("v");
    }
    if (!IsDirectory(path, false)) {
        std::filesystem::path p = path;
        std::filesystem::create_directories(p.parent_path() / "Moved");
        std::filesystem::rename(path, p.parent_path() / "Moved" / p.filename());
        res.set_content("OK", "text/plain");
    }
}

void moveFile(const httplib::Request &req, httplib::Response &res){
    res.set_header("Access-Control-Allow-Origin", "*");
    std::string path = "/storage/emulated/0";
    if (req.has_param("v")) {
        path = req.get_param_value("v");
    }
    if (!IsDirectory(path, false)) {
        std::filesystem::path p = path;
        std::filesystem::create_directories(p.parent_path() / "Removed");
        std::filesystem::rename(path, p.parent_path() / "Removed" / p.filename());
        res.set_content("OK", "text/plain");
    }
}

void moveVideo(const httplib::Request &req, httplib::Response &res){
    res.set_header("Access-Control-Allow-Origin", "*");
    std::string path = "/storage/emulated/0";
    if (req.has_param("v")) {
        path = req.get_param_value("v");
    }
    if (!IsDirectory(path, false)) {
        std::filesystem::path p = path;
        std::filesystem::create_directories("/storage/emulated/0/Files");
        std::filesystem::rename(path, "/storage/emulated/0/Files" / p.filename());
        res.set_content("OK", "text/plain");
    }
}

#endif
