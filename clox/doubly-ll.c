#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stdint.h>

typedef struct Node
{
    char* data;
    struct Node* next;
    struct Node* prev;
} Node;

Node* create_node(const char* str)
{
    Node* node = (Node*)malloc(sizeof(Node));
    if (node == NULL)
        return NULL; // malloc failed

    node->next = node->prev = NULL;
    node->data = (char*)malloc(strlen(str) + 1); // +1 for NUL terminator
    if (node->data == NULL)
    {
        // malloc failed
        free(node);
        return NULL;
    }

    strcpy(node->data, str);

    return node;
}

void destroy_node(Node* node)
{
    if (node == NULL)
        return;

    printf("Deleting node: [addr: %p, data: %s]...\n", (void*)node, node->data);
    free(node->data);
    free(node);
}

typedef struct List
{
    Node* front;
    Node* back;
} List;

List* list_init()
{
    List* ret = (List*)malloc(sizeof(List));
    if (ret == NULL)
        return NULL; // malloc failed

    ret->front = NULL;
    ret->back = NULL;
    return ret;
}

void list_destroy(List* list)
{
    if (list == NULL)
        return;

    while (list->front != NULL)
    {
        Node* cur = list->front;
        list->front = list->front->next;
        destroy_node(cur);
    }

    free(list);
}

List* list_insert_front(List* list, const char* str)
{
    if (list == NULL)
        return NULL;

    Node* node = create_node(str);
    if (node == NULL)
        return list; // TODO: create_node failed

    if (list->front != NULL)
    {
        list->front->prev = node;
        node->next = list->front;
    }

    list->front = node;

    if (list->back == NULL)
        list->back = node;

    return list;
}

List* list_insert_back(List* list, const char* str)
{
    if (list == NULL)
        return NULL;

    Node* node = create_node(str);
    if (node == NULL)
        return list; // TODO: create_node failed

    if (list->back != NULL)
    {
        list->back->next = node;
        node->prev = list->back;
    }

    list->back = node;

    if (list->front == NULL)
        list->front = node;

    return list;
}

List* list_insert_at(List* list, uint32_t index, const char* str)
{
    if (list == NULL)
        return NULL;

    Node* itr = list->front;
    while (index > 0 && itr != NULL)
    {
        itr = itr->next;
        --index;
    }

    if (index > 0)
        return NULL; // Inserting out of bounds

    if (itr != NULL)
    {
        // Create and insert node before itr
        Node* node = create_node(str);
        if (node == NULL)
            return list; // TODO: create_node failed

        if (itr->prev != NULL)
            itr->prev->next = node;
        node->prev = itr->prev;

        node->next = itr;
        itr->prev = node;

        if (itr == list->front)
            list->front = node;
    }

    return list;
}

void list_remove(List* list, uint32_t index)
{
    if (list == NULL)
        return;

    Node* itr = list->front;
    while (index > 0 && itr != NULL)
    {
        itr = itr->next;
        --index;
    }

    if (itr != NULL)
    {
        if (itr->prev != NULL)
            itr->prev->next = itr->next;
        if (itr->next != NULL)
            itr->next->prev = itr->prev;

        if (itr == list->front)
            list->front = itr->next;
        if (itr == list->back)
            list->back = itr->prev;

        destroy_node(itr);
    }
}

int32_t list_search(List* list, const char* str)
{
    if (list == NULL)
        return -1;

    int32_t index = 0;
    for (Node* itr = list->front; itr != NULL; itr = itr->next, ++index)
    {
        if (strcmp(itr->data, str) == 0)
            return index;
    }

    return -1;
}

void list_print_all_forward(List* list)
{
    if (list == NULL)
        return;

    for (Node* itr = list->front; itr != NULL; itr = itr->next)
    {
        printf("addr: %p, data: %s, prev: %p, next: %p\n", (void*)itr, itr->data, (void*)itr->prev, (void*)itr->next);
    }
}

void list_print_all_backward(List* list)
{
    if (list == NULL)
        return;

    for (Node* itr = list->back; itr != NULL; itr = itr->prev)
    {
        printf("addr: %p, data: %s, prev: %p, next: %p\n", (void*)itr, itr->data, (void*)itr->prev, (void*)itr->next);
    }
}

int main(void)
{
    //printf("Hello, world!\n");

    const char* strs[] = {"Alleviate", "Bison", "Cat", "Dyson"};
    List* list = list_init();

    for (int i = 3; i >= 0; --i)
        list_insert_front(list, strs[i]);

    list_print_all_forward(list);
    printf("\n");

    printf("\"%s\" is at index %d (Expected: 2)\n", strs[2], list_search(list, strs[2]));
    printf("\"%s\" is at index %d (Expected: 3)\n", strs[3], list_search(list, strs[3]));
    printf("\"%s\" is at index %d (Expected: 0)\n", strs[0], list_search(list, strs[0]));
    printf("\"%s\" is at index %d (Expected: -1)\n", "asdf", list_search(list, "asdf"));
    printf("\n");

    list_print_all_backward(list);
    printf("\n");

    list_insert_at(list, 2, "Hello");
    list_insert_at(list, 4, "World");

    list_print_all_forward(list);
    printf("\n");

    printf("Removing nodes...\n");
    list_remove(list, 3); // Remove "Cat"
    list_remove(list, 4); // Remove "Dyson"

    list_print_all_forward(list);
    printf("\n");

    printf("Inserting nodes...\n");
    list_insert_at(list, 0, "New Node");
    list_insert_back(list, "back node");

    list_print_all_forward(list);
    printf("\n");

    list_destroy(list);
}
