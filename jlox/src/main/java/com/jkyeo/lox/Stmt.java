package com.jkyeo.lox;

import java.util.List;

abstract class Stmt{
  interface Visitor<R> {
    R visitPrintStmt(Print stmt);
    R visitExpressionStmt(Expression stmt);
  }

  abstract <R> R accept(Visitor<R> visitor);

  static class Print extends Stmt {
    Print(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }

    final Expr expression;
  }

  static class Expression extends Stmt {
    Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }

    final Expr expression;
  }
}
