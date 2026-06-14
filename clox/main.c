#include "common.h"
#include "chunk.h"
#include "debug.h"
#include "memory.h"

int main(int argc, const char** argv) {
    initMemory();
    
    Chunk chunk;
    initChunk(&chunk);

    writeConstant(&chunk, 3.14, 1);

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

    for (int i = 0; i < 300; ++i)
        writeConstant(&chunk, 2.71, i + 5);

    disassembleChunk(&chunk, "test chunk");

    freeChunk(&chunk);

    freeMemory();
    return 0;
}
