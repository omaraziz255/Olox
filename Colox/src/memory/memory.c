/** Created by Omar Ahmed Hesham Aziz on 15/11/2022 */

#include <stdlib.h>

#include <memory/memory.h>
#include <virtual-machine/vm.h>

void* reallocate(void* pointer, size_t oldSize, size_t newSize) {
    if(newSize == 0) {
        free(pointer);
        return NULL;
    }

    void* result = realloc(pointer, newSize);
    if(result == NULL) exit(1);
    return result;
}

static void freeObject(Obj* object) {
    switch(object->type) {
        case OBJ_CLOSURE: {
            FREE(OBJ_CLOSURE, object);
            break;
        }
        case OBJ_FUNCTION: {
            ObjFunction* function = (ObjFunction*) object;
            freeChunk(&function->chunk);
            FREE(ObjFunction, object);
            break;
        }
        case OBJ_NATIVE: {
            FREE(ObjNative, object);
            break;
        }
        case OBJ_STRING: {
            ObjString *string = (ObjString *) object;
            reallocate(object, sizeof(ObjString) + string->length + 1, 0);
            break;
        }
    }
}

void freeObjects() {
    Obj* object = vm.objects;
    while(object != NULL) {
        Obj* next = object->next;
        freeObject(object);
        object = next;
    }
}