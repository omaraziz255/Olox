/** Created by Omar Ahmed Hesham Aziz on 23/11/2022 */

#include <stdio.h>
#include <string.h>

#include <memory/memory.h>
#include <entities/object.h>
#include <entities/value.h>
#include <entities/table.h>
#include <virtual-machine/vm.h>

#define ALLOCATE_OBJ(type, objectType) (type*)allocateObject(sizeof(type), objectType)

static Obj* allocateObject(size_t size, ObjType type) {
    Obj* object = (Obj*) reallocate(NULL, 0, size);
    object->type = type;
    object->next = vm.objects;
    vm.objects = object;
    return object;
}

ObjFunction* newFunction() {
    ObjFunction* function = ALLOCATE_OBJ(ObjFunction, OBJ_FUNCTION);

    function->arity = 0;
    function->name = NULL;
    initChunk(&function->chunk);
    return function;
}

static uint32_t hashString(const char* key, int length) {
    uint32_t hash = 2166136261u;
    for(int i = 0; i < length; i++) {
        hash ^= key[i];
        hash *= 16777619;
    }

    return hash;
}

static ObjString* makeString(int length, uint32_t hash) {
    ObjString* string = (ObjString*) allocateObject(sizeof(ObjString) + length + 1, OBJ_STRING);
    string->length = length;
    string->hash = hash;
    tableSet(&vm.strings, string, NIL_VAL);
    return string;
}

ObjString* copyString(const char* chars, int length) {
    uint32_t hash = hashString(chars, length);
    ObjString* interned = tableFindString(&vm.strings, chars, length, hash);
    if(interned != NULL)
        return interned;
    ObjString* string = makeString(length, hash);
    memcpy(string->chars, chars, length);
    string->chars[length] = '\0';

    return string;
}

static void printFunction(ObjFunction* function) {
    if(function->name == NULL) {
        printf("<script>");
        return;
    }
    printf("<fn %s>", function->name->chars);
}

void printObject(Value value) {
    switch(OBJ_TYPE(value)) {
        case OBJ_FUNCTION:
            printFunction(AS_FUNCTION(value));
            break;
        case OBJ_STRING:
            printf("%s", AS_CSTRING(value));
            break;
    }
}