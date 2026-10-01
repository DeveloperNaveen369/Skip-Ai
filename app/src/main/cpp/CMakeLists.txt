cmake_minimum_required(VERSION 3.22.1)

project("qwen_native")

add_library(
    qwen_native
    SHARED
    qwen_native.cpp
)

target_include_directories(
    qwen_native
    PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/llama-include
    ${CMAKE_CURRENT_SOURCE_DIR}/ggml-include
)

add_library(llama SHARED IMPORTED)

set_target_properties(llama PROPERTIES
    IMPORTED_LOCATION
    "${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/arm64-v8a/libllama.so"
)

find_library(log-lib log)

target_link_libraries(
    qwen_native
    llama
    ${log-lib}
)