# ArduinoJson - https://arduinojson.org
# Copyright Benoit Blanchon 2014-2021
# MIT License

# I have no idea what this is about, I simply followed the instructions from:
# https://dominikberner.ch/cmake-interface-lib/

add_library(ArduinoJson INTERFACE)

# Adding the install interface generator expression makes sure that the include
# files are installed to the proper location (provided by GNUInstallDirs)
target_include_directories(ArduinoJson
    INTERFACE
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}>
        $<INSTALL_INTERFACE:${CMAKE_INSTALL_INCLUDEDIR}>
)

target_compile_definitions(ArduinoJson
    INTERFACE
        ARDUINOJSON_DEBUG=$<CONFIG:Debug>
)

