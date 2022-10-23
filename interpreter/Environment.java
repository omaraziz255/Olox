package interpreter;

import exceptions.RuntimeError;
import lexical_scanner.Token;

import java.util.ArrayList;
import java.util.List;

public class Environment {
    final Environment enclosing;
    private final List<Object> values = new ArrayList<>();

    public Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    void define(Object value) {
        values.add(value);
    }

    Environment ancestor(int distance) {
        Environment environment = this;
        for(int i = 0; i < distance; i++) {
            assert environment != null;
            environment = environment.enclosing;
        }

        return environment;
    }

    Object getAt(int distance, int slot) {
        return ancestor(distance).values.get(slot);
    }

    void assignAt(int distance, int slot, Object value) {
        ancestor(distance).values.set(slot, value);
    }

    @Override
    public String toString() {
        String result = values.toString();
        if(enclosing != null) {
            result += "->" + enclosing.toString();
        }

        return result;
    }
}
