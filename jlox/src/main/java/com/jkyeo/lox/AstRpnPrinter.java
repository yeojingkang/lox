package com.jkyeo.lox;

public class AstRpnPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return
                expr.left.accept(this) + " " +
                expr.right.accept(this) + " " +
                expr.operator.lexeme;
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return
            expr.right.accept(this) + " " +
            (expr.operator.type == TokenType.MINUS ? "NEG" : expr.operator.lexeme);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return expr.value == null ? "nil" : expr.value.toString();
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return expr.expression.accept(this);
    }
}
