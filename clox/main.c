#include "common.h"
#include "chunk.h"
#include "debug.h"

int main(int argc, const char** argv) {
    Chunk chunk;
    initChunk(&chunk);

    int constant = addConstant(&chunk, 3.14);
    writeChunk(&chunk, OP_CONSTANT, 1);
    writeChunk(&chunk, constant, 1);

    writeChunk(&chunk, OP_RETURN, 1);
    writeChunk(&chunk, OP_RETURN, 1);
    writeChunk(&chunk, OP_RETURN, 1);
    writeChunk(&chunk, OP_RETURN, 1);

    writeChunk(&chunk, OP_RETURN, 2);
    writeChunk(&chunk, OP_RETURN, 3);
    writeChunk(&chunk, OP_RETURN, 4);
    writeChunk(&chunk, OP_RETURN, 4);

    writeChunk(&chunk, OP_RETURN, 1);
    writeChunk(&chunk, OP_RETURN, 1);
    writeChunk(&chunk, OP_RETURN, 1);
    disassembleChunk(&chunk, "test chunk");

    freeChunk(&chunk);

    return 0;
}
