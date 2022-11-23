/** Created by Omar Ahmed Hesham Aziz on 20/11/2022 */

#ifndef COLOX_COMPILER_H
#define COLOX_COMPILER_H

#include <virtual-machine/vm.h>

bool compile(const char* source, Chunk* chunk);

#endif //COLOX_COMPILER_H
