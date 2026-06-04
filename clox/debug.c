#include <stdio.h>
#include "debug.h"

void disassembleChunk(Chunk* chunk, const char* name) {
    printf("== %s ==\n", name);
    for (int i = 0; i < chunk->count;) {
        i = disassembleInstruction(chunk, i);
    }
}

static int simpleInstruction(const char* name, int offset) {
    printf("%s\n", name);
    return offset + 1;
}

int disassembleInstruction(Chunk* chunk, int offset) {
    printf("%04d ", offset);

    const uint8_t insn = chunk->code[offset];
    switch (insn) {
    case OP_RETURN:
        return simpleInstruction("OP_RETURN", offset);
    default:
        printf("Unknown opcode %d\n", insn);
        return offset + 1;
    }
}
