package com.jkyeo.lox;

enum TokenType {
    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, SEMICOLON,
    MINUS, PLUS, STAR, SLASH,

    BANG, BANG_EQ,
    EQ, EQ_EQ,
    GREATER, GREATER_EQ,
    LESS, LESS_EQ,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    AND, OR,
    IF, ELSE,
    FOR, WHILE, RETURN,
    TRUE, FALSE, NIL,
    CLASS, THIS, SUPER,
    VAR, FUN, PRINT,

    EOF
}
