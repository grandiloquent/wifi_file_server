
cmake_minimum_required(VERSION 3.18)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++2a -fno-rtti -fexceptions -Wall")

add_library(nativelib SHARED
        nativelib.cpp logging.cpp FileUtils.cpp AndroidUtils.cpp
        StringUtils.cpp)

add_library(qrcode SHARED qrcode.c)

add_library(httplib SHARED httplib.cc)

add_subdirectory(ArduinoJson)


target_link_libraries(nativelib
        qrcode
        android
        ArduinoJson
        httplib
        log)