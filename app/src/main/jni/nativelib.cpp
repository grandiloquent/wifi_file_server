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
#include "nativelib.h"


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

    server.Get("/api/remove", handlingDeleteFileRequests);
    server.Get("/api/move", handlingMoveFileRequests);
    server.Get("/api/files", [](const Request &req, Response &res) {
        std::string path = "/storage/emulated/0";

        if (req.has_param("v")) {
            path = req.get_param_value("v");
        }
        if (!IsDirectory(path, false)) {

            res.set_header("Access-Control-Allow-Origin", "*");
            //
            res.set_header("Content-Disposition",
                           "'attachment; filename=\"" + SubstringAfterLast(path, "/") + "\"");
            std::shared_ptr<std::ifstream> fs = std::make_shared<std::ifstream>();
            fs->open(path, std::ios_base::binary);
            fs->seekg(0, std::ios_base::end);
            auto end = fs->tellg();
            if (end == 0)return;
            fs->seekg(0);
            std::map<std::string, std::string> file_extension_and_mimetype_map;
            res.set_content_provider(static_cast<size_t>(end),
                                     findMimeType(path).c_str(),
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
            newPath.append("/")
                    .append(req.get_param_value("new"));
            int status;
            struct stat statBuf;
            status = lstat(newPath.c_str(), &statBuf);
            if (status) {
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
            std::vector<std::filesystem::path> sorted_by_name;
            for (auto const &dir_entry: std::filesystem::directory_iterator{dir}) {
                sorted_by_name.push_back(dir_entry.path());
            }
            std::sort(sorted_by_name.begin(), sorted_by_name.end(),
                      [](const auto &lhs, const auto &rhs) {
                          return lhs.filename().string() < rhs.filename().string();
                      });
            for (auto &dir_entry : sorted_by_name) {
                ss << "<a href=\"" << dir_entry.filename().string()
                   << "\">"
                   << dir_entry.filename().string()
                   << "</a>";
            }
            ss << R"(</body>

</html>)";
            res.set_content(ss.str(), "text/html");
            return;
        }
        auto path = dir + "/" + query.str();
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
            std::vector<std::filesystem::path> sorted_by_name;
            for (auto const &dir_entry: std::filesystem::directory_iterator{path}) {
                sorted_by_name.push_back(dir_entry.path());
            }
            std::sort(sorted_by_name.begin(), sorted_by_name.end(),
                      [](const auto &lhs, const auto &rhs) {
                          return lhs.filename().string() > rhs.filename().string();
                      });
            for (auto &dir_entry : sorted_by_name) {
                ss << "<a href=\""
                   << dir_entry.parent_path().filename().string()
                   << "/"
                   << dir_entry.filename().string()
                   << "\">"
                   << dir_entry.filename().string()
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

    server.Get("/music", [&](const Request &req, Response &res) {
        res.set_content(R"(<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
    <style>
        html {
            font-size: 12px;
            font-family: Roboto, Noto Naskh Arabic UI, Arial, sans-serif;
        }

        body {
            margin: 0;
            background-color: #030303;
            -webkit-font-smoothing: antialiased;
            overflow: visible;
        }

        .player-bar-a11y-label {
            position: absolute;
            top: auto;
            left: -101vw;
            width: 1px;
            height: 1px;
            border: 0;
            overflow: hidden;
        }

        .music-player-bar {
            background-color: #212121;
            display: grid;
            grid-template-columns: 0fr 1fr 0fr;
            grid-template-areas: "start middle end";
            color: #909090;
            position: fixed;
            bottom: 0;
            left: 0;
            height: 64px;
            transition-property: transform, height, background-color;
            transition-duration: 300ms;
            transition-timing-function: cubic-bezier(0.2, 0, 0.6, 1);
            will-change: transform;
            z-index: 3;
            width: 100%;
            transform: translate3d(0, 0, 0);
            visibility: visible;
        }

        .left-controls {
            grid-area: start;
            flex: none;
            display: flex;
            align-items: center;
        }

        .left-controls-buttons {
            flex: none;
            display: flex;
            align-items: center;
        }

        .paper-icon-button {
            display: inline-block;
            position: relative;
            outline: none;
            user-select: none;
            cursor: pointer;
            z-index: 0;
            line-height: 1;
            -webkit-tap-highlight-color: transparent;
            box-sizing: border-box !important;
            flex: none;
            padding: 4px;
            width: 32px;
            height: 32px;

        }

        .iron-icon {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            position: relative;
            vertical-align: middle;
            fill: #fff;
            stroke: none;
            width: 100%;
            height: 100%;
        }

        .middle-controls {
            grid-area: middle;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow: hidden;
        }

        .content-info-wrapper {
            display: flex;
            flex-direction: column;
            overflow: hidden;
            margin: 0;
        }


        .title {
            display: block;
            text-overflow: ellipsis;
            overflow: hidden;
            white-space: nowrap;
            font-family: Roboto, Noto Naskh Arabic UI, Arial, sans-serif;
            font-size: 14px;
            line-height: 1.3;
            font-weight: 500;
            color: #fff;
        }

        .byline-wrapper {
            display: flex;
            align-items: center;
        }

        .right-controls {
            grid-area: end;
            justify-self: end;
            position: relative;
            justify-content: flex-end;
            margin: 0 4px 0 0;
            flex: none;
            display: flex;
            align-items: center;
        }

        .subtitle {
            display: flex;
            flex-direction: row;
            width: 100%;
            overflow: hidden;
        }

        .byline {
            text-overflow: ellipsis;
            overflow: hidden;
            white-space: pre;
            display: flex;
            flex-direction: row;
            font-family: Roboto, Noto Naskh Arabic UI, Arial, sans-serif;
            font-size: 14px;
            line-height: 1.3;
            font-weight: 400;
            color: rgba(255, 255, 255, .7);

        }

        .paper-slider {
            display: -ms-flexbox;
            display: -webkit-flex;
            display: flex;
            -ms-flex-pack: justify;
            -webkit-justify-content: space-between;
            justify-content: space-between;
            -ms-flex-align: center;
            -webkit-align-items: center;
            align-items: center;
            user-select: none;
            -webkit-tap-highlight-color: rgba(0, 0, 0, 0);

            position: absolute;
            cursor: pointer;
            display: block;
            top: 1px;
            left: -16px;
            width: 100%;
            transform: translateY(-50%);
        }

        .slider-container {
            position: relative;
            width: 100%;
            height: calc(30px + 2px);
            margin-left: calc(15px + 2px / 2);
            margin-right: calc(15px + 2px / 2);
            transform: scaleX(1);
        }

        .bar-container {
            position: absolute;
            top: 0;
            bottom: 0;
            left: 0;
            right: 0;
            overflow: hidden;
        }

        .paper-progress {
            display: block;
            position: relative;
            overflow: hidden;
            padding: 15px 0;
            width: 100%;
            touch-action: none;
        }

        .progress-container {
            position: relative;
            height: 2px;
            background: rgba(255, 255, 255, 0.1);
        }

        .primary-progress {
            position: absolute;
            top: 0;
            right: 0;
            bottom: 0;
            left: 0;
            transform-origin: left center;
            transform: scaleX(0);
            will-change: transform;
            background-color: rgb(255, 0, 0);
        }

        .secondary-progress {
            position: absolute;
            top: 0;
            right: 0;
            bottom: 0;
            left: 0;
            transform-origin: left center;
            transform: scaleX(0);
            will-change: transform;
            background-color: rgba(255, 255, 255, 0.1);
        }

        .slider-knob {
            position: absolute;
            left: 0;
            top: 0;
            margin-left: calc(-15px - 2px / 2);
            width: calc(30px + 2px);
            height: calc(30px + 2px);
            touch-action: none;
        }

        .slider-knob-inner {
            margin: 10px;
            width: calc(100% - 20px);
            height: calc(100% - 20px);
            background-color: #3367d6;
            border: 2px solid #3367d6;
            border-radius: 50%;
            -moz-box-sizing: border-box;
            box-sizing: border-box;
            transition-property: -webkit-transform, background-color, border;
            transition-property: transform, background-color, border;
            transition-duration: 0.18s;
            transition-timing-function: ease;
        }

        .list-item {
            padding: 0 8px 0 16px;
            margin-bottom: 16px;
            position: relative;
            display: flex;
            color: #fff;
        }

        .simple-endpoint {
            position: absolute;
            top: 0;
            right: 0;
            bottom: 0;
            left: 0;
        }

        .left-items {
            flex: none;
            position: relative;
            border-radius: 2px;
            overflow: hidden;
            width: 48px;
            height: 48px;
            margin: 0 16px 0 0;
        }

        .thumbnail {
            display: block;
            position: absolute;
            width: 100%;
            height: 100%;
            border-radius: 2px;
            overflow: hidden;
            color: #fff;
        }

        img {
            display: block;
            width: 100%;
            height: 100%;
            object-fit: contain;
        }

        .flex-columns {
            flex: 1;
            display: flex;
            overflow: hidden;
            flex-wrap: wrap;
            align-items: center;
        }

        .title-column {
            display: flex;
            flex: 6 1 0;
            justify-content: space-between;
            overflow: hidden;
            flex-basis: 100%;
            margin: 0 0 3px;
        }

        .secondary-flex-columns {
            flex: 9 1 0;
            display: flex;
            overflow: hidden;
            align-items: center;
            flex-basis: 1px;
        }
    </style>
</head>

<body>
    <audio></audio>
    <div class="list">

    </div>
    <div class="music-player-bar">
        <h2 class="player-bar-a11y-label">播放器控制栏</h2>
        <div class="left-controls">
            <div class="left-controls-buttons">

                <div class="paper-icon-button" style="margin: 0 0 0 8px;">
                    <div class="iron-icon">
                        <svg viewBox="0 0 24 24" preserveAspectRatio="xMidYMid meet" focusable="false"
                            class="style-scope tp-yt-iron-icon"
                            style="pointer-events: none; display: block; width: 100%; height: 100%;">
                            <g class="style-scope tp-yt-iron-icon">
                                <path d="M6 6h2v12H6zm3.5 6l8.5 6V6z" class="style-scope tp-yt-iron-icon"></path>
                            </g>
                        </svg>
                    </div>
                </div>

                <div class="paper-icon-button" id="play-button" style="height: 40px;width: 40px;">
                    <div class="iron-icon">
                        <svg viewBox="0 0 24 24" preserveAspectRatio="xMidYMid meet" focusable="false"
                            class="style-scope tp-yt-iron-icon"
                            style="pointer-events: none; display: block; width: 100%; height: 100%;">
                            <g class="style-scope tp-yt-iron-icon">
                                <path d="M8 5v14l11-7z" class="style-scope tp-yt-iron-icon"></path>
                            </g>
                        </svg>
                    </div>
                </div>

                <div class="paper-icon-button">
                    <div class="iron-icon">
                        <svg viewBox="0 0 24 24" preserveAspectRatio="xMidYMid meet" focusable="false"
                            class="style-scope tp-yt-iron-icon"
                            style="pointer-events: none; display: block; width: 100%; height: 100%;">
                            <g class="style-scope tp-yt-iron-icon">
                                <path d="M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z" class="style-scope tp-yt-iron-icon">
                                </path>
                            </g>
                        </svg>
                    </div>
                </div>


            </div>
        </div>
        <div class="middle-controls">
            <div class="content-info-wrapper">
                <div class="title"></div>
                <div class="byline-wrapper">
                    <div class="subtitle">
                        <div class="byline"></div>
                    </div>
                </div>
            </div>

        </div>
        <div class="right-controls">
        </div>
        <div class="paper-slider">
            <div class="slider-container">
                <div class="bar-container">
                    <div class="paper-progress">
                        <div class="progress-container">
                            <div class="secondary-progress">
                            </div>
                            <div class="primary-progress">
                            </div>
                        </div>
                    </div>
                </div>
                <div class="slider-knob" style="display: none;">
                    <div class="slider-knob-inner">
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script>
        const audio = document.querySelector('audio');

        async function play() {
            await audio.play();
        }

        const playButton = document.querySelector('#play-button');
        playButton.setAttribute('hidden', '');
        playButton.addEventListener('click', async ev => {
            await play();
            playButton.querySelector('path').setAttribute('d', 'M6 19h4V5H6v14zm8-14v14h4V5h-4z');
        })

        const primaryProgress = document.querySelector('.primary-progress');
        const secondaryProgress = document.querySelector('.secondary-progress');
        const byline = document.querySelector('.byline');
        const sliderKnob = document.querySelector('.slider-knob');
        const title = document.querySelector('.title');
        const list = document.querySelector('.list');

        audio.addEventListener('durationchange', evt => {
        })
        audio.addEventListener('progress', evt => {
            if (audio.buffered.length) {
                secondaryProgress.style.transform = `scaleX(${audio.buffered.end(0) / audio.duration})`;
            }
        })
        audio.addEventListener('timeupdate', evt => {
            const value = audio.currentTime / audio.duration;
            primaryProgress.style.transform = `scaleX(${value})`;
            //sliderKnob.style.left = `${value * 100}%`;
            byline.textContent = formatDuration(audio.currentTime);
        })
        function formatDuration(ms) {
            if (isNaN(ms)) return '0:00';
            if (ms < 0) ms = -ms;
            const time = {
                hour: Math.floor(ms / 3600) % 24,
                minute: Math.floor(ms / 60) % 60,
                second: Math.floor(ms) % 60,
            };
            return Object.entries(time)
                .filter((val, index) => index || val[1])
                .map(val => (val[1] + '').padStart(2, '0'))
                .join(':');
        }
        function substringAfterLast(string, delimiter, missingDelimiterValue) {
            const index = string.lastIndexOf(delimiter);
            if (index === -1) {
                return missingDelimiterValue || string;
            } else {
                return string.substring(index + delimiter.length);
            }
        }
        async function fetchItems() {
            const response = await fetch(`/api/files?v=%2Fstorage%2Femulated%2F0%2FMusics%2F听书`);
            const items = await response.json();
            const strings = [];
            items.forEach(element => {
                const string = `<div class="list-item" data-path="${element.path}">
            <a class="simple-endpoint">
            </a>
            <div class="left-items">
                <div class="thumbnail">
                </div>
            </div>
            <div class="flex-columns">
                <div class="title-column">
                    <div style="font-size: 14px;line-height: 1.3;display: flex;white-space: pre;">${substringAfterLast(element.path, "/")}</div>
                </div>
                <div class="secondary-flex-columns">
                    <div style="font-size: 14px;line-height: 1.3;display: flex;white-space: pre;">
                       </div>
                </div>
            </div>
        </div>`;
                strings.push(string);
            });
            list.innerHTML = strings.join('');


            document.querySelectorAll('.list-item')
                .forEach(listItem => listItem.addEventListener('click', ev => {
                    audio.src = "/api/files?v=" + listItem.dataset.path;
                    title.textContent = listItem.querySelector('.title-column div').textContent;
                    play();
                }));


        }
        fetchItems();
        window.addEventListener('hashchange', ev => {
            const values = /((\d+)h)?((\d+)m)?(\d+)s/.exec(location.hash);
            let currentTime = 0;
            if (values[2]) {
                currentTime += parseInt(values[2]) * 3600;
            }
            if (values[4]) {
                currentTime += parseInt(values[4]) * 60;
            }
            if (values[5]) {
                currentTime += parseInt(values[5]);
            }
            audio.currentTime = currentTime;
        })
    </script>
</body>

</html>)", "text/html");
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

