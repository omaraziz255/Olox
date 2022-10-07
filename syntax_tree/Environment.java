package syntax_tree;

import lexical_scanner.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    Object get(Token name) {
        if(values.containsKey(name.getLexeme())) {
            return values.get(name.getLexeme());
        }

        if(enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.getLexeme() + "' ");

    }

    void assign(Token name, Object value) {
        if(values.containsKey(name.getLexeme())) {
            values.put(name.getLexeme(), value);
            return;
        }

        if(enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.getLexeme() + "' ");
    }

    void define(String name, Object value) {
        values.put(name, value);
    }
}
