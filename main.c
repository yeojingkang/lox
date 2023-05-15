#include <stdio.h>
#include <string.h>
#include <stdlib.h>

typedef struct Node
{
    char* data;
    struct Node* next;
    struct Node* prev;
} Node;

Node* create_node(const char* str)
{
    Node* node = (Node*)malloc(sizeof(Node));
    node->next = node->prev = NULL;
    node->data = (char*)malloc(strlen(str) + 1); // +1 for NUL terminator
    strcpy(node->data, str);

    return node;
}

void destroy_node(Node** node)
{
    if (node == NULL)
        return;

    if ((*node)->data != NULL)
        free((*node)->data);
    free(*node);
}

typedef struct List
{
    Node* front;
    Node* back;
} List;

List* list_init()
{
    List* ret = (List*)malloc(sizeof(List));
    ret->front = NULL;
    ret->back = NULL;
    return ret;
}

void list_destruct(List* list)
{
    if (list == NULL)
        return;

    for (Node *itr = list->front, *nItr = list->front->next; itr != NULL; itr = nItr)
    {
        nItr = itr->next;

        //printf("Deleting \"%p\"...\n", itr);

        destroy_node(&itr);
    }

    free(list);
}

List* list_insert_front(List* list, const char* str)
{
    if (list == NULL)
        return NULL;

    Node* node = create_node(str);

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

List* list_insert_at(List* list, int index, const char* str)
{
    if (list == NULL)
        return NULL;

    Node* itr = list->front;
    while (index > 0 && itr != NULL)
    {
        itr = itr->next;
        --index;
    }

    if (itr != NULL)
    {
        // Create and insert node before itr
        Node* node = create_node(str);

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

void list_remove(List* list, int index)
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

        destroy_node(&itr);
    }
}

int list_search(List* list, const char* str)
{
    if (list == NULL)
        return -1;

    int index = 0;
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
        printf("addr: %p, data: %s, prev: %p, next: %p\n", itr, itr->data, itr->prev, itr->next);
    }
}

void list_print_all_backward(List* list)
{
    if (list == NULL)
        return;

    for (Node* itr = list->back; itr != NULL; itr = itr->prev)
    {
        printf("addr: %p, data: %s, prev: %p, next: %p\n", itr, itr->data, itr->prev, itr->next);
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

    printf("\"%s\" is at index %d\n", strs[2], list_search(list, strs[2]));
    printf("\"%s\" is at index %d\n", strs[3], list_search(list, strs[3]));
    printf("\"%s\" is at index %d\n", strs[0], list_search(list, strs[0]));
    printf("\"%s\" is at index %d\n", "asdf", list_search(list, "asdf"));
    printf("\n");

    list_print_all_backward(list);
    printf("\n");

    list_insert_at(list, 2, "Hello");
    list_insert_at(list, 4, "World");

    list_print_all_forward(list);
    printf("\n");

    printf("Removing nodes...\n");
    list_remove(list, 3);
    list_remove(list, 4);

    list_print_all_forward(list);
    printf("\n");

    printf("Inserting nodes...\n");
    list_insert_at(list, 0, "New Node");
    list_insert_back(list, "back node");

    list_print_all_forward(list);
    printf("\n");

    list_destruct(list);
}
