package com.jkyeo.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;

    LoxFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public int arity() { return declaration.params.size(); }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        final var env = new Environment(this.closure);

        for (var i = 0; i < declaration.params.size(); ++i) {
            env.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        interpreter.executeBlock(declaration.body, env);
        return null;
    }

    @Override
    public String toString() {
         return "<fn " + declaration.name.lexeme + ">";
    }
}
