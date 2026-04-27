package com.jkyeo.lox;

public class Interpreter implements Expr.Visitor<Object> {
    void interpret(Expr expression) {
        try {
            final var value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError err) {
            Lox.runtimeError(err);
        }
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
                if ((double)rvalue == 0.0)
                    throw new RuntimeError(expr.operator, "Division by zero.");
                yield (double)lvalue / (double)rvalue;
            }
            case STAR -> {
                checkNumberOperands(expr.operator, lvalue, rvalue);
                yield (double)lvalue * (double)rvalue;
            }
            case PLUS -> {
                if (lvalue instanceof Double && rvalue instanceof Double)
                    yield (double)lvalue + (double)rvalue;
                if (lvalue instanceof String)
                    yield (String)lvalue + stringify(rvalue);
                if (rvalue instanceof String)
                    yield stringify(lvalue) + (String)rvalue;
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
