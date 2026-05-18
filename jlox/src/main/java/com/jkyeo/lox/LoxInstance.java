package com.jkyeo.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
    private LoxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    Object get(Interpreter interpreter, Token name) {
        if (fields.containsKey(name.lexeme))
            return fields.get(name.lexeme);

        final var method = klass.findMethod(name.lexeme);
        if (method != null) return method.bind(this);

        final var getter = klass.findGetter(name.lexeme);
        if (getter != null) return getter.bind(this).call(interpreter, new ArrayList<>());

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }
}
