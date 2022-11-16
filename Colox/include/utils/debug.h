/** Created by Omar Ahmed Hesham Aziz on 15/11/2022 */

#ifndef COLOX_DEBUG_H
#define COLOX_DEBUG_H

#include <bytecode/chunk.h>

void disassembleChunk(Chunk* chunk, const char* name);
int disassembleInstruction(Chunk* chunk, int offset);

#endif //COLOX_DEBUG_H
