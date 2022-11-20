add_subdirectory(bytecode)
add_subdirectory(entities)
add_subdirectory(memory)
add_subdirectory(utils)
add_subdirectory(virtual-machine)

add_library(COLOX STATIC ${SOURCES})
target_link_libraries(COLOX ${LIBS})