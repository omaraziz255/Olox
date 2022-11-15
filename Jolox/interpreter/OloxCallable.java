package Jolox.interpreter;

import java.util.List;

public interface OloxCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
