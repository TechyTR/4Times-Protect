cmake_minimum_required(VERSION 3.22.1)
project(fourtimes_security LANGUAGES CXX)

add_library(fourtimes_security SHARED native-lib.cpp)
target_compile_features(fourtimes_security PRIVATE cxx_std_17)
target_compile_options(fourtimes_security PRIVATE
    -Wall -Wextra -Wpedantic -fvisibility=hidden -fstack-protector-strong
)
target_link_options(fourtimes_security PRIVATE -Wl,-z,relro -Wl,-z,now)
