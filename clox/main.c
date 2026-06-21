#include "common.h"
#include "chunk.h"
#include "debug.h"
#include "vm.h"

int main(int argc, const char** argv) {
    initVM();

    Chunk chunk;
    initChunk(&chunk);

    int constant = addConstant(&chunk, 3.14);
    writeChunk(&chunk, OP_CONSTANT, 1);
    writeChunk(&chunk, constant, 1);

    writeChunk(&chunk, OP_NEGATE, 2);

    constant = addConstant(&chunk, 2.71);
    writeChunk(&chunk, OP_CONSTANT, 3);
    writeChunk(&chunk, constant, 3);

    writeChunk(&chunk, OP_ADD, 3);

    constant = addConstant(&chunk, 5);
    writeChunk(&chunk, OP_CONSTANT, 3);
    writeChunk(&chunk, constant, 3);

    writeChunk(&chunk, OP_DIVIDE, 3);

    writeChunk(&chunk, OP_RETURN, 4);
    disassembleChunk(&chunk, "test chunk");

    interpret(&chunk);

    freeVM();
    freeChunk(&chunk);

    return 0;
}
