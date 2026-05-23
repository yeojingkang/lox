package com.jkyeo.lox;

public class AstPrinter implements Expr.Visitor<String>{
    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitSuperExpr(Expr.Super expr) {
        return "super";
    }

    @Override
    public String visitThisExpr(Expr.This expr) {
        return "this";
    }

    @Override
    public String visitSetExpr(Expr.Set expr) {
        return "(set " + expr.object.accept(this) + "." + expr.name.lexeme + " " + expr.value.accept(this) + ")";
    }

    @Override
    public String visitGetExpr(Expr.Get expr) {
        return "(get " + expr.object.accept(this) + " " + expr.name.lexeme + ")";
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        return parenthesize("call", expr.callee)
            + parenthesize("args", expr.arguments.toArray(Expr[]::new));
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return parenthesize(expr.name.lexeme + " <-", expr.value);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return "(var " + expr.name.lexeme + ")";
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return expr.value == null ? "nil" : expr.value.toString();
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    private String parenthesize(String name, Expr... exprs) {
        final var builder = new StringBuilder();

        builder.append("(").append(name);
        for (final var expr : exprs) {
            builder.append(" ").append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}
