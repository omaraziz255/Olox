package interpreter;

import java.util.List;
import java.util.Map;

public class OloxClass implements OloxCallable {
    final String name;
    private final Map<String, OloxFunction> methods;

    OloxClass(String name, Map<String, OloxFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    OloxFunction findMethod(String name) {
        if(methods.containsKey(name)) {
            return methods.get(name);
        }

        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        OloxFunction initializer = findMethod("init");
        if(initializer == null) return 0;

        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        OloxInstance instance  = new OloxInstance(this);
        OloxFunction initializer = findMethod("init");
        if(initializer!= null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }
}
