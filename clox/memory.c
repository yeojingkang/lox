#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include "memory.h"

#define HEAP_SIZE (10 * 1024 * 1024)
#define ALIGNMENT 8
#define GET_ALIGNED(s) ((s + ALIGNMENT - 1) & ~(ALIGNMENT - 1))

typedef struct MemNode {
    struct MemNode* next;
    size_t size;
} MemNode;

#define NODE_SIZE sizeof(MemNode)

void* mem = NULL;
MemNode* freeList = NULL; // The free list of memory

void* loxRealloc(void* ptr, size_t size);
void loxFree(void* ptr);

void printFreeList() {
    MemNode* p = freeList;
    while (p != NULL) {
        printf("%p\nsize: %lu\nnext: %p\n\n", p, p->size, p->next);
        p = p->next;
    }
    printf("------\n");
}

void* reallocate(void* pointer, size_t oldSize, size_t newSize) {
    if (newSize == 0) {
        loxFree(pointer);
        return NULL;
    }

    void* result = loxRealloc(pointer, newSize);
    if (result == NULL) exit(1); // realloc failed
    return result;
}

void initMemory() {
    mem = malloc(HEAP_SIZE);
    if (mem == NULL) exit(1);

    freeList = (MemNode*)mem;
    freeList->next = NULL;
    freeList->size = HEAP_SIZE - NODE_SIZE;
    printFreeList();
}
void freeMemory() {
    printFreeList();
    free(mem);
}

void* loxMalloc(size_t size) {
    const size_t allocSize = GET_ALIGNED(size + NODE_SIZE);

    // Find the best fit free node
    MemNode* bestNode = NULL;
    MemNode* currNode = freeList;
    while (currNode != NULL) {
        if (currNode->size >= allocSize &&
            (bestNode == NULL || currNode->size < bestNode->size)) {
            bestNode = currNode;
        }
        currNode = currNode->next;
    }

    if (bestNode == NULL)
        return NULL; // Unable to allocate

    // Slice the best node and fill the new node data
    bestNode->size -= allocSize;
    MemNode* allocNode = (MemNode*)((unsigned char*)bestNode + NODE_SIZE + bestNode->size);
    allocNode->next = NULL;
    allocNode->size = allocSize - NODE_SIZE;

    return (void*)((unsigned char*)allocNode + NODE_SIZE);
}

void loxFree(void* ptr) {
    if (ptr == NULL)
        return;

    MemNode* freeNode = (MemNode*)((unsigned char*)ptr - NODE_SIZE);

    // Find the prev and next free nodes of freeNode
    MemNode* prev = NULL;
    MemNode* next = freeList;
    while (next != NULL && next < freeNode) {
        prev = next;
        next = next->next;
    }

    // Insert freeNode between the 2 nodes
    freeNode->next = next;
    if (prev != NULL)
        prev->next = freeNode;

    // Merge next node if they are neighbours
    if (next != NULL &&
        (unsigned char*)freeNode + NODE_SIZE + freeNode->size == (unsigned char*)next) {
        freeNode->next = next->next;
        freeNode->size += NODE_SIZE + next->size;
    }

    // Merge prev node if they are neighbours
    if (prev != NULL &&
        (unsigned char*)prev + NODE_SIZE + prev->size == (unsigned char*)freeNode) {
        prev->next = freeNode->next;
        prev->size += NODE_SIZE + freeNode->size;
    }
}

void* loxRealloc(void* ptr, size_t size) {
    void* newPtr = loxMalloc(size);
    if (newPtr == NULL)
        return NULL;
    printFreeList();

    // Copy data in old buffer to new buffer
    if (ptr != NULL) {
        memcpy(newPtr, ptr, min(size, ((MemNode*)((unsigned char*)ptr - NODE_SIZE))->size));
        loxFree(ptr);
    }

    return newPtr;
}
