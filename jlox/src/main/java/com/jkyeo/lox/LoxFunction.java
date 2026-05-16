package com.jkyeo.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    LoxFunction bind(LoxInstance instance) {
        final var env = new Environment(closure);
        env.define("this", instance);
        return new LoxFunction(declaration, env, isInitializer);
    }

    @Override
    public int arity() { return declaration.params.size(); }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        final var env = new Environment(this.closure);

        for (var i = 0; i < declaration.params.size(); ++i) {
            env.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, env);
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
         return "<fn " + declaration.name.lexeme + ">";
    }
}
