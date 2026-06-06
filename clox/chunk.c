#include <stdlib.h>
#include "chunk.h"
#include "memory.h"

void initChunk(Chunk* chunk) {
    chunk->count = 0;
    chunk->capacity = 0;
    chunk->code = NULL;
    initLineInfoArray(&chunk->lineInfos);
    initValueArray(&chunk->constants);
}

void freeChunk(Chunk* chunk) {
    freeValueArray(&chunk->constants);
    freeLineInfoArray(&chunk->lineInfos);
    FREE_ARRAY(uint8_t, chunk->code, chunk->capacity);
    initChunk(chunk);
}

void writeChunk(Chunk* chunk, uint8_t byte, int line) {
    if (chunk->count >= chunk->capacity) {
        int oldCapacity = chunk->capacity;
        chunk->capacity = GROW_CAPACITY(oldCapacity);
        chunk->code = GROW_ARRAY(uint8_t, chunk->code, oldCapacity, chunk->capacity);
    }

    chunk->code[chunk->count] = byte;
    writeLineInfoArray(&chunk->lineInfos, line);
    ++chunk->count;
}

int addConstant(Chunk* chunk, Value value) {
    writeValueArray(&chunk->constants, value);
    return chunk->constants.count - 1;
}

int getLine(Chunk* chunk, int offset) {
    int lineInfoIdx = 0;
    while (offset > 0) {
        int currLineLength = chunk->lineInfos.lines[lineInfoIdx].length;
        if (currLineLength > offset) break;

        offset -= currLineLength;
        ++lineInfoIdx;
    }

    return chunk->lineInfos.lines[lineInfoIdx].line;
}
