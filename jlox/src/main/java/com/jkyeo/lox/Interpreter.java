package com.jkyeo.lox;

import java.util.List;

public class Interpreter implements
    Expr.Visitor<Object>,
    Stmt.Visitor<Void> {
    final Environment globals = new Environment();
    private Environment env = globals;

    Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() { return 0; }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
    }

    void interpret(List<Stmt> statements) {
        try {
            for (final var statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError err) {
            Lox.runtimeError(err);
        }
    }

    // Statements

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        env.define(stmt.name.lexeme, new LoxFunction(stmt, env));
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition)))
            execute(stmt.body);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition)))
            execute(stmt.thenBranch);
        else if (stmt.elseBranch != null)
            execute(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        env.define(
                stmt.name.lexeme,
                stmt.init != null ? evaluate(stmt.init) : null);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        final var value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(env));
        return null;
    }

    // Expressions

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        final var callee = evaluate(expr.callee);
        final var arguments = expr.arguments.stream().map(this::evaluate);

        if (!(callee instanceof LoxCallable fn))
            throw new RuntimeError(expr.paren, "Can only call functions and classes");

        if (fn.arity() != expr.arguments.size())
            throw new RuntimeError(expr.paren, "Expected " + fn.arity() + " arguments but got " + expr.arguments.size() + ".");
        return fn.call(this, arguments.toList());
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        final var left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        final var value = evaluate(expr.value);
        this.env.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return env.get(expr.name);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        final var value = evaluate(expr.right);

        return switch (expr.operator.type) {
            case MINUS -> {
                checkNumberOperand(expr.operator, value);
                yield -(double)value;
            }
            case BANG -> !isTruthy(value);
            default -> null;
        };
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        final var lvalue = evaluate(expr.left);
        final var rvalue = evaluate(expr.right);

        return switch (expr.operator.type) {
            case MINUS -> {
                checkNumberOperands(expr.operator, lvalue, rvalue);
                yield (double)lvalue - (double)rvalue;
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, lvalue, rvalue);
                yield (double)lvalue / (double)rvalue;
            }
            case STAR -> {
                checkNumberOperands(expr.operator, lvalue, rvalue);
                yield (double)lvalue * (double)rvalue;
            }
            case PLUS -> {
                if (lvalue instanceof Double && rvalue instanceof Double)
                    yield (double)lvalue + (double)rvalue;
                if (lvalue instanceof String && rvalue instanceof String)
                    yield (String)lvalue + (String)rvalue;
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            }

            case GREATER -> {
                checkNumberOperands(expr.operator, lvalue, rvalue);
                yield (double)lvalue > (double)rvalue;
            }
            case GREATER_EQ -> {
                checkNumberOperands(expr.operator, lvalue, rvalue);
                yield (double)lvalue >= (double)rvalue;
            }
            case LESS -> {
                checkNumberOperands(expr.operator, lvalue, rvalue);
                yield (double)lvalue < (double)rvalue;
            }
            case LESS_EQ -> {
                checkNumberOperands(expr.operator, lvalue, rvalue);
                yield (double)lvalue <= (double)rvalue;
            }
            case EQ_EQ -> isEqual(lvalue, rvalue);
            case BANG_EQ -> !isEqual(lvalue, rvalue);

            default -> null;
        };
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (!(operand instanceof Double))
            throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (!(left instanceof Double && right instanceof Double))
            throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private void execute(Stmt statement) {
        statement.accept(this);
    }

    // TODO: Change visit pattern to take an Environment context to avoid mutating env
    void executeBlock(List<Stmt> statements, Environment env) {
        final var prevEnv = this.env;
        try {
            this.env = env;
            for (final var statement : statements) {
                execute(statement);
            }
        } finally {
            this.env = prevEnv;
        }
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (boolean)value;
        return true;
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null) return true;
        if (left == null) return false;
        return left.equals(right);
    }

    private String stringify(Object value) {
        if (value == null) return "nil";

        if (value instanceof Double) {
            var valueStr = value.toString();
            if (valueStr.endsWith(".0")) // Cull ".0" for whole number values
                valueStr = valueStr.substring(0, valueStr.length() - 2);
            return valueStr;
        }

        return value.toString();
    }
}
