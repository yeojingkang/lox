#ifndef clox_line_info_h
#define clox_line_info_h

typedef struct {
    int line;
    int length;
} LineInfo;

typedef struct {
    int count;
    int capacity;
    LineInfo* lines;
} LineInfoArray;

void initLineInfoArray(LineInfoArray* array);
void freeLineInfoArray(LineInfoArray* array);
void writeLineInfoArray(LineInfoArray* array, int line);

#endif
