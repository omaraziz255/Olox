/** Created by Omar Ahmed Hesham Aziz on 15/11/2022 */

#ifndef COLOX_MEMORY_H
#define COLOX_MEMORY_H

#include <utils/common.h>

#define GROW_CAPACITY(capacity) ((capacity) < 8 ? 8 : (capacity) * 2)

#define GROW_ARRAY(type, pointer, oldCount, newCount) (type*)reallocate(pointer, sizeof(type) * (oldCount), \
sizeof(type) * (newCount))

#define FREE_ARRAY(type, pointer, oldCount) reallocate(pointer, sizeof(type) * (oldCount), 0);

void* reallocate(void* pointer, size_t oldSize, size_t newSize);

#endif //COLOX_MEMORY_H