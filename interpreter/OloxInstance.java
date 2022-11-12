package interpreter;

import exceptions.RuntimeError;
import lexical_scanner.Token;

import java.util.HashMap;
import java.util.Map;

public class OloxInstance {
    private final OloxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    OloxInstance(OloxClass klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }

    Object get(Token name) {
        if(fields.containsKey(name.getLexeme())) {
            return fields.get(name.getLexeme());
        }

        OloxFunction method = klass.findMethod(name.getLexeme());
        if(method != null) return method.bind(this);

        throw new RuntimeError(name, "Undefined property " + name.getLexeme());
    }

    void set(Token name, Object value) {
        fields.put(name.getLexeme(), value);
    }
}
