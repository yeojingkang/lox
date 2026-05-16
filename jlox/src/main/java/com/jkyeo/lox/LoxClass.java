package com.jkyeo.lox;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    final String name;
    private final Map<String, LoxFunction> methods;

    LoxClass(String name, Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    LoxFunction findMethod(String name) {
        return methods.getOrDefault(name, null);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        final var instance = new LoxInstance(this);

        // Run the initializer if there is any
        final var initializer = findMethod("init");
        if (initializer != null)
            initializer.bind(instance).call(interpreter, arguments);

        return instance;
    }

    @Override
    public int arity() {
        final var initializer = findMethod("init");
        return initializer != null ? initializer.arity() : 0;
    }

    @Override
    public String toString() { return name; }
}
