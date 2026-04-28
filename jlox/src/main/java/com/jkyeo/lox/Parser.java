package com.jkyeo.lox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// Lox's grammar:
// program        → declaration* EOF ;

// declaration    → varDecl
//                | statement ;

// varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;

// statement      → exprStmt
//                | printStmt ;

// exprStmt       → expression ";" ;
// printStmt      → "print" expression ";" ;

/***************** Expressions *****************/
// expression     → equality ;
// equality       → comparison ( ( "!=" | "==" ) comparison )* ;
// comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
// term           → factor ( ( "-" | "+" ) factor )* ;
// factor         → unary ( ( "/" | "*" ) unary )* ;
// unary          → ( "!" | "-" ) unary | primary ;
// primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | IDENTIFIER ;



// Recursive Descent parser
public class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(TokenType.VAR)) return varDeclaration();
            return statement();
        } catch (ParseError err) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        final var name = consume(TokenType.IDENTIFIER, "Expected variable name.");

        final var init = match(TokenType.EQ)
                ? expression()
                : null;

        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration.");

        return new Stmt.Var(name, init);
    }

    private Stmt statement() {
        if (match(TokenType.PRINT)) return printStatement();

        return exprStatement();
    }

    private Stmt printStatement() {
        final var expr = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after value.");
        return new Stmt.Print(expr);
    }

    private Stmt exprStatement() {
        final var expr = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        return leftAssocBinary(
                new TokenType[]{ TokenType.BANG_EQ, TokenType.EQ_EQ },
                this::comparison
        );
    }

    private Expr comparison() {
        return leftAssocBinary(
                new TokenType[]{ TokenType.GREATER, TokenType.GREATER_EQ, TokenType.LESS, TokenType.LESS_EQ },
                this::term
        );
    }

    private Expr term() {
        return leftAssocBinary(
                new TokenType[]{ TokenType.MINUS, TokenType.PLUS },
                this::factor
        );
    }

    private Expr factor() {
        return leftAssocBinary(
                new TokenType[]{ TokenType.STAR, TokenType.SLASH },
                this::unary
        );
    }

    private Expr unary() {
        if (match(TokenType.MINUS, TokenType.BANG)) {
            final var operator = previous();
            final var right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(TokenType.FALSE)) return new Expr.Literal(false);
        if (match(TokenType.TRUE)) return new Expr.Literal(true);
        if (match(TokenType.NIL)) return new Expr.Literal(null);

        if (match(TokenType.NUMBER, TokenType.STRING))
            return new Expr.Literal(previous().literal);

        if (match(TokenType.IDENTIFIER))
            return new Expr.Variable(previous());

        if (match(TokenType.LEFT_PAREN)) {
            final var expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expected an expression.");
    }

    private Expr leftAssocBinary(TokenType[] types, Supplier<Expr> operand) {
        var expr = operand.get();

        while (match(types)) {
            final var operator = previous();
            final var right = operand.get();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private boolean match(TokenType... types) {
        for (final var type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) ++current;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
