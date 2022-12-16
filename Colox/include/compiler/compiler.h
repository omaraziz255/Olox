/** Created by Omar Ahmed Hesham Aziz on 20/11/2022 */

#ifndef COLOX_COMPILER_H
#define COLOX_COMPILER_H

#include <entities/object.h>
#include <virtual-machine/vm.h>

ObjFunction* compile(const char* source);

#endif //COLOX_COMPILER_H
