package com.jkyeo.lox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Environment {
    final Environment enclosing;
    private final Set<String> definedVars = new HashSet<>();
    private final Map<String, Object> values = new HashMap<>();

    Environment() { enclosing = null; }
    Environment(Environment enclosing) { this.enclosing = enclosing; }

    void define(String name) {
        definedVars.add(name);
    }
    void define(String name, Object value) {
        definedVars.add(name);
        values.put(name, value);
    }

    Object get(Token name) {
        if (definedVars.contains(name.lexeme)) {
            if (values.containsKey(name.lexeme))
                return values.get(name.lexeme);
            throw new RuntimeError(name, "Unassigned variable '" + name.lexeme + "'.");
        }

        if (enclosing != null)
            return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void assign(Token name, Object value) {
        if (definedVars.contains(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}
