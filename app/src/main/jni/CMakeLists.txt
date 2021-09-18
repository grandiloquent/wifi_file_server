set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++2a -fno-rtti -fexceptions -Wall")

add_library(nativelib SHARED
        nativelib.cpp logging.cpp)

add_library(qrcode SHARED qrcode.c)

target_link_libraries(nativelib
        qrcode
        android
        log)