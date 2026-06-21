#include <stdio.h>
#include "common.h"
#include "debug.h"
#include "vm.h"

VM vm; // NOTE: Consider removing it (all VM functions require a VM arg)

static void resetStack() {
    vm.stack.count = 0;
}


void initVM() {
    initValueArray(&vm.stack);
    resetStack();
}

void freeVM() {

}

static InterpretResult run() {
#define READ_BYTE() (*vm.ip++)
#define READ_CONSTANT() (vm.chunk->constants.values[READ_BYTE()])
#define BINARY_OP(op) \
    do { \
        double r = pop(); \
        vm.stack.values[vm.stack.count - 1] = vm.stack.values[vm.stack.count - 1] op r; \
    } while (false)

    for (;;) {
#ifdef DEBUG_TRACE_EXECUTION
        printf("          ");
        for (Value* slot = vm.stack.values; slot < vm.stack.values + vm.stack.count; slot++) {
            printf("[ ");
            printValue(*slot);
            printf(" ]");
        }
        printf("\n");
        disassembleInstruction(vm.chunk, (int)(vm.ip - vm.chunk->code));
#endif

        uint8_t instruction;
        switch (instruction = READ_BYTE()) {
            case OP_CONSTANT: {
                Value constant = READ_CONSTANT();
                push(constant);
                break;
            }
            case OP_NEGATE:
                vm.stack.values[vm.stack.count - 1] = -vm.stack.values[vm.stack.count - 1];
                break;
            case OP_ADD:        BINARY_OP(+); break;
            case OP_SUBTRACT:   BINARY_OP(-); break;
            case OP_MULTIPLY:   BINARY_OP(*); break;
            case OP_DIVIDE:     BINARY_OP(/); break;
            case OP_RETURN:
                printValue(pop());
                printf("\n");
                return INTERPRET_OK;
        }
    }

#undef BINARY_OP
#undef READ_CONSTANT
#undef READ_BYTE
}

InterpretResult interpret(Chunk* chunk) {
    vm.chunk = chunk;
    vm.ip = chunk->code;
    return run();
}

void push(Value value){
    writeValueArray(&vm.stack, value);
}

Value pop(){
    // Not using freeValueArray so stack doesn't shrink
    return vm.stack.values[--vm.stack.count];
}
