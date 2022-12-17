cmake_minimum_required(VERSION 3.22)
project(Colox C)

set(CMAKE_C_STANDARD 99)
set(CMAKE_C_FLAGS_DEBUG "${CMAKE_C_FLAGS_DEBUG} -DDEBUG_PRINT_CODE -DEBUG_TRACE_EXECUTION")
set(CMAKE_C_FLAGS_DEBUG "${CMAKE_C_FLAGS_DEBUG} -DDEBUG_STRESS_GC -DDEBUG_LOG_GC")

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/include)
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/src)

add_executable(Colox main.c)
target_link_libraries(Colox COLOX)
