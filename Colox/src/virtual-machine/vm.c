/** Created by Omar Ahmed Hesham Aziz on 18/11/2022 */
#include <stdio.h>

#include <utils/common.h>
#include <virtual-machine/vm.h>
#include <compiler/compiler.h>
#include <utils/debug.h>

VM vm;

static InterpretResult run() {
#define READ_BYTE() (*vm.ip++)
#define READ_CONSTANT() (vm.chunk->constants.values[READ_BYTE()])

#define BINARY_OP(op) \
    do {              \
        double b = pop(); \
        double a = pop(); \
        push(a op b); \
    } while(false)


    for(;;) {
#ifdef DEBUG_TRACE_EXECUTION
        printf("          ");
        for (Value* slot = vm.stack; slot < vm.stackTop; slot++) {
            printf("[ ");
            printValue(*slot);
            printf(" ]");
        }
        printf("\n");
        disassembleInstruction(vm.chunk, (int)(vm.ip - vm.chunk->code));
#endif
        switch (READ_BYTE()) {
            case OP_CONSTANT: {
                Value constant = READ_CONSTANT();
                push(constant);
                break;
            }
            case OP_ADD:        BINARY_OP(+);break;
            case OP_SUBTRACT:   BINARY_OP(-);break;
            case OP_MULTIPLY:   BINARY_OP(*);break;
            case OP_DIVIDE:     BINARY_OP(/);break;
            case OP_NEGATE:     *(vm.stackTop-1) *= -1; break;
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

static void resetStack() {
    vm.stackTop = vm.stack;
}

void initVM() {
    resetStack();
}

void freeVM() {

}

InterpretResult interpret(const char* source) {
    compile(source);
    return INTERPRET_OK;
}

void push(Value value) {
    *vm.stackTop = value;
    vm.stackTop++;
}

Value pop() {
    vm.stackTop--;
    return *vm.stackTop;
}

