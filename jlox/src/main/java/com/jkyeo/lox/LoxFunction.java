package com.jkyeo.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final String name;
    private final Expr.Lambda lambda;
    private final Environment closure;
    private final boolean isInitializer;

    LoxFunction(String name, Expr.Lambda lambda, Environment closure, boolean isInitializer) {
        this.name = name;
        this.lambda = lambda;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    LoxFunction bind(LoxInstance instance) {
        final var env = new Environment(closure);
        env.define("this", instance);
        return new LoxFunction(name, lambda, env, isInitializer);
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
            return isInitializer
                ? closure.getAt(0, "this")
                : value.value;
        }

        return isInitializer
            ? closure.getAt(0, "this")
            : null;
    }

    @Override
    public String toString() {
        if (name == null)
            return "<lambda>";
         return "<fn " + name + ">";
    }
}
