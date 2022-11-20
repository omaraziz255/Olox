/** Created by Omar Ahmed Hesham Aziz on 18/11/2022 */

#ifndef COLOX_VM_H
#define COLOX_VM_H

#include <bytecode/chunk.h>

#define STACK_MAX 256

typedef struct {
    Chunk* chunk;
    uint8_t* ip;
    Value stack[STACK_MAX];
    Value* stackTop;
} VM;

typedef enum {
    INTERPRET_OK,
    INTERPRET_COMPILE_ERROR,
    INTERPRET_RUNTIME_ERROR
} InterpretResult;

void initVM();
void freeVM();
InterpretResult interpret(Chunk* chunk);
void push(Value value);
Value pop();

#endif //COLOX_VM_H
