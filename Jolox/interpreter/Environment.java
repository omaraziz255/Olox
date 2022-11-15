package Jolox.interpreter;

import java.util.ArrayList;
import java.util.List;

public class Environment {
    final Environment enclosing;
    private final List<Object> values = new ArrayList<>();

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void define(Object value) {
        values.add(value);
    }

    public int getSize() { return values.size(); }

    public Environment ancestor(int distance) {
        Environment environment = this;
        for(int i = 0; i < distance; i++) {
            assert environment != null;
            environment = environment.enclosing;
        }

        return environment;
    }

    public Object getAt(int distance, int slot) {
        return ancestor(distance).values.get(slot);
    }

    public void assignAt(int distance, int slot, Object value) {
        ancestor(distance).values.set(slot, value);
    }

    @Override
    public String toString() {
        String result = values.toString();
        if(enclosing != null) {
            result += "->" + enclosing;
        }

        return result;
    }
}
