package com.jkyeo.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final String name;
    private final Expr.Lambda lambda;
    private final Environment closure;

    LoxFunction(String name, Expr.Lambda lambda, Environment closure) {
        this.name = name;
        this.lambda = lambda;
        this.closure = closure;
    }

    @Override
    public int arity() { return lambda.params.size(); }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        final var env = new Environment(this.closure);

        for (var i = 0; i < lambda.params.size(); ++i) {
            env.define(lambda.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(lambda.body, env);
        } catch (Return value) {
            return value.value;
        }

        return null;
    }

    @Override
    public String toString() {
        if (name == null)
            return "<lambda>";
         return "<fn " + name + ">";
    }
}
