package com.jkyeo.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

// Lox's grammar:
/*
program        → declaration* EOF ;

declaration    → varDecl
               | statement ;

varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;

statement      → exprStmt
               | forStmt
               | ifStmt
               | printStmt
               | whileStmt
               | block ;

forStmt        → "for" "(" ( varDecl | exprStmt | ";" )
                 expression? ";"
                 expression? "   )" statement ;
whileStmt      → "while" "(" expression ")" statement ;
ifStmt         → "if" "(" expression ")" statement ( "else" statement )? ;
exprStmt       → expression ";" ;
printStmt      → "print" expression ";" ;
block          → "{" declaration* "}" ;

***************** Expressions *****************
expression     → assignment ;

assignment     → IDENTIFIER "=" assignment
               | logic_or ;
logic_or       → logic_and ( "or" logic_and )* ;
logic_and      → equality ( "and" equality )* ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary | call ;
call           → primary ( "(" arguments? ")" )* ;
primary        → NUMBER | STRING | "true" | "false" | "nil"
               | "(" expression ")" | IDENTIFIER ;

arguments      → expression ( "," expression )* ;
*/



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
        if (match(TokenType.FOR)) return forStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.PRINT)) return printStatement();
        if (match(TokenType.LEFT_BRACE)) return new Stmt.Block(block());

        return exprStatement();
    }

    private Stmt forStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after for.");
        final var initializer =
            match(TokenType.VAR) ? varDeclaration() :
            match(TokenType.SEMICOLON) ? null :
            exprStatement();
        final var condition = !check(TokenType.SEMICOLON) ? expression() : null;
        consume(TokenType.SEMICOLON, "Expected ';' after loop condition");
        final var increment = !check(TokenType.RIGHT_PAREN) ? expression() : null;
        consume(TokenType.RIGHT_PAREN, "Expected ')' after for clauses.");

        var body = statement();

        // Parsed as:
        /*
         * {
         *      init;
         *      while (cond) {
         *          body;
         *          incr;
         *      }
         * }
         */

        if (increment != null)
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
        body = new Stmt.While(
            condition == null ? new Expr.Literal(true) : condition,
            body);
        if (initializer != null)
            body = new Stmt.Block(Arrays.asList(initializer, body));

        return body;
    }

    private Stmt whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after while.");
        final var condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after while condition.");
        final var body = statement();

        return new Stmt.While(condition, body);
    }

    private Stmt ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expected '(' after if.");
        final var condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after if condition.");

        final var thenBranch = statement();
        final var elseBranch = match(TokenType.ELSE) ? statement() : null;

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        final var expr = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after value.");
        return new Stmt.Print(expr);
    }

    private List<Stmt> block() {
        final var statements = new ArrayList<Stmt>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}' after block.");
        return statements;
    }

    private Stmt exprStatement() {
        final var expr = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        final var expr = logicOr();

        if (match(TokenType.EQ)) {
            final var eq = previous();
            final var value = assignment();

            if (expr instanceof Expr.Variable varExpr) {
                return new Expr.Assign(varExpr.name, value);
            }

            error(eq, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr logicOr() {
        return leftAssocLogical(
            new TokenType[]{ TokenType.OR },
            this::logicAnd
        );
    }

    private Expr logicAnd() {
        return leftAssocLogical(
            new TokenType[]{ TokenType.AND },
            this::equality
        );
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

        return call();
    }

    private Expr call() {
        var expr = primary();

        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        final var arguments = new ArrayList<Expr>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255)
                    error(peek(), "Can't have more than 255 arguments.");
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }
        final var paren = consume(TokenType.RIGHT_PAREN, "Expected ')' after arguments");

        return new Expr.Call(callee, paren, arguments);
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

    private Expr leftAssocLogical(TokenType[] types, Supplier<Expr> operand) {
        var expr = operand.get();

        while (match(types)) {
            final var operator = previous();
            final var right = operand.get();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
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
