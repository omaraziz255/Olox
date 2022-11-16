/** Created by Omar Ahmed Hesham Aziz on 16/11/2022 */

#ifndef COLOX_VALUE_H
#define COLOX_VALUE_H

#include <utils/common.h>

typedef double Value;

typedef struct {
    int capacity;
    int count;
    Value* values;
} ValueArray;

void initValueArray(ValueArray* array);
void writeValueArray(ValueArray* array, Value value);
void freeValueArray(ValueArray* array);
void printValue(Value value);

#endif //COLOX_VALUE_H
