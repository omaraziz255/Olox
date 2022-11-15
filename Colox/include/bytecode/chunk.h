/** Created by Omar Ahmed Hesham Aziz on 15/11/2022 */

#ifndef COLOX_CHUNK_H
#define COLOX_CHUNK_H

#include <utils/common.h>

typedef enum {
    OP_RETURN
} OpCode;

typedef struct {
    int count;
    int capacity;
    uint8_t* code;
} Chunk;

void initChunk(Chunk* chunk);
void freeChunk(Chunk* chunk);
void writeChunk(Chunk* chunk, uint8_t byte);

#endif //COLOX_CHUNK_H
