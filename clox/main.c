#include "common.h"
#include "chunk.h"
#include "debug.h"
#include "vm.h"

void ch1(Chunk* chunk) {
    int one = addConstant(chunk, 1);
    int two = addConstant(chunk, 2);
    int three = addConstant(chunk, 3);
    int four = addConstant(chunk, 4);
    int five = addConstant(chunk, 5);

    // 1 * 2 + 3
    writeChunk(chunk, OP_CONSTANT, 11);
    writeChunk(chunk, one, 11);
    writeChunk(chunk, OP_CONSTANT, 11);
    writeChunk(chunk, two, 11);

    writeChunk(chunk, OP_MULTIPLY, 11);

    writeChunk(chunk, OP_CONSTANT, 11);
    writeChunk(chunk, three, 11);

    writeChunk(chunk, OP_ADD, 11);

    // 1 + 2 * 3
    writeChunk(chunk, OP_CONSTANT, 12);
    writeChunk(chunk, one, 12);
    writeChunk(chunk, OP_CONSTANT, 12);
    writeChunk(chunk, two, 12);
    writeChunk(chunk, OP_CONSTANT, 12);
    writeChunk(chunk, three, 12);

    writeChunk(chunk, OP_MULTIPLY, 12);
    writeChunk(chunk, OP_ADD, 12);

    // 3 - 2 - 1
    writeChunk(chunk, OP_CONSTANT, 13);
    writeChunk(chunk, three, 13);
    writeChunk(chunk, OP_CONSTANT, 13);
    writeChunk(chunk, two, 13);

    writeChunk(chunk, OP_SUBTRACT, 13);

    writeChunk(chunk, OP_CONSTANT, 13);
    writeChunk(chunk, one, 13);

    writeChunk(chunk, OP_SUBTRACT, 13);

    // 1 + 2 * 3 - 4 / -5
    writeChunk(chunk, OP_CONSTANT, 14);
    writeChunk(chunk, one, 14);
    writeChunk(chunk, OP_CONSTANT, 14);
    writeChunk(chunk, two, 14);
    writeChunk(chunk, OP_CONSTANT, 14);
    writeChunk(chunk, three, 14);

    writeChunk(chunk, OP_MULTIPLY, 14);
    writeChunk(chunk, OP_ADD, 14);

    writeChunk(chunk, OP_CONSTANT, 14);
    writeChunk(chunk, four, 14);
    writeChunk(chunk, OP_CONSTANT, 14);
    writeChunk(chunk, five, 14);

    writeChunk(chunk, OP_NEGATE, 14);
    writeChunk(chunk, OP_DIVIDE, 14);
    writeChunk(chunk, OP_SUBTRACT, 14);
}

void ch2(Chunk* chunk) {
    int zero = addConstant(chunk, 0);
    int two = addConstant(chunk, 2);
    int three = addConstant(chunk, 3);
    int four = addConstant(chunk, 4);

    // 4 - 3 * -2

    // Without OP_NEGATE
    writeChunk(chunk, OP_CONSTANT, 21);
    writeChunk(chunk, four, 21);
    writeChunk(chunk, OP_CONSTANT, 21);
    writeChunk(chunk, three, 21);
    writeChunk(chunk, OP_CONSTANT, 21);
    writeChunk(chunk, zero, 21);
    writeChunk(chunk, OP_CONSTANT, 21);
    writeChunk(chunk, two, 21);

    writeChunk(chunk, OP_SUBTRACT, 21);
    writeChunk(chunk, OP_MULTIPLY, 21);
    writeChunk(chunk, OP_SUBTRACT, 21);

    // Without OP_SUBTRACT
    writeChunk(chunk, OP_CONSTANT, 22);
    writeChunk(chunk, four, 22);
    writeChunk(chunk, OP_CONSTANT, 22);
    writeChunk(chunk, three, 22);
    writeChunk(chunk, OP_CONSTANT, 22);
    writeChunk(chunk, two, 22);

    writeChunk(chunk, OP_NEGATE, 22);
    writeChunk(chunk, OP_MULTIPLY, 22);
    writeChunk(chunk, OP_NEGATE, 22);
    writeChunk(chunk, OP_ADD, 22);
}

int main(int argc, const char** argv) {
    initVM();

    Chunk chunk;
    initChunk(&chunk);

    /*
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
    */

    ch1(&chunk);
    ch2(&chunk);

    int constant = addConstant(&chunk, 3.14);
    writeChunk(&chunk, OP_CONSTANT, 1);
    writeChunk(&chunk, constant, 1);
    writeChunk(&chunk, OP_CONSTANT, 1);
    writeChunk(&chunk, constant, 1);
    writeChunk(&chunk, OP_CONSTANT, 1);
    writeChunk(&chunk, constant, 1);
    writeChunk(&chunk, OP_CONSTANT, 1);
    writeChunk(&chunk, constant, 1);

    writeChunk(&chunk, OP_RETURN, 999);

    disassembleChunk(&chunk, "test chunk");

    interpret(&chunk);

    freeVM();
    freeChunk(&chunk);

    return 0;
}
