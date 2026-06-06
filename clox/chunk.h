#ifndef clox_chunk_h
#define clox_chunk_h

#include "common.h"
#include "line-info.h"
#include "value.h"

typedef enum {
    OP_CONSTANT,
    OP_RETURN,
} OpCode;

// Chunk is a series of bytecode insns
typedef struct {
    int count;
    int capacity;
    uint8_t* code;
    LineInfoArray lineInfos;
    ValueArray constants;
} Chunk;

void initChunk(Chunk* chunk);
void freeChunk(Chunk* chunk);
void writeChunk(Chunk* chunk, uint8_t byte, int line);

int addConstant(Chunk* chunk, Value value);

int getLine(Chunk* chunk, int offset);

#endif
