#include <stdlib.h>
#include "line-info.h"
#include "memory.h"

void initLineInfoArray(LineInfoArray* array) {
    array->count = 0;
    array->capacity = 0;
    array->lines = NULL;
}

void freeLineInfoArray(LineInfoArray* array) {
    FREE_ARRAY(LineInfo, array->lines, array->capacity);
    initLineInfoArray(array);
}

void writeLineInfoArray(LineInfoArray* array, int line) {
    if (array->count > 0 && line == array->lines[array->count - 1].line) {
        ++array->lines[array->count - 1].length;
        return;
    }

    if (array->count >= array->capacity) {
        int oldCapacity = array->capacity;
        array->capacity = GROW_CAPACITY(oldCapacity);
        array->lines = GROW_ARRAY(LineInfo, array->lines, oldCapacity, array->capacity);
    }

    array->lines[array->count++] = (LineInfo){ line, 1 };
}
