/** Created by Omar Ahmed Hesham Aziz on 23/11/2022 */

#ifndef COLOX_OBJECT_H
#define COLOX_OBJECT_H

#include <utils/common.h>
#include <bytecode/chunk.h>
#include <entities/value.h>

#define OBJ_TYPE(value)     (AS_OBJ(value)->type)
#define IS_FUNCTION(value)  isObjType(value, OBJ_FUNCTION)
#define IS_STRING(value)    isObjType(value, OBJ_STRING)

#define AS_FUNCTION(value)  ((ObjFunction*)AS_OBJ(value))
#define AS_STRING(value)    ((ObjString*)AS_OBJ(value))
#define AS_CSTRING(value)   (((ObjString*)AS_OBJ(value))->chars)

typedef enum {
    OBJ_FUNCTION,
    OBJ_STRING
} ObjType;

struct Obj {
    ObjType type;
    struct Obj* next;
};

typedef struct {
    Obj obj;
    int arity;
    Chunk chunk;
    ObjString* name;
} ObjFunction;

struct ObjString {
    Obj obj;
    int length;
    uint32_t hash;
    char chars[];
};

ObjFunction* newFunction();
ObjString* copyString(const char* chars, int length);
void printObject(Value value);

static inline bool isObjType(Value value, ObjType type) {
    return IS_OBJ(value) && AS_OBJ(value)->type == type;
}

#endif //COLOX_OBJECT_H
