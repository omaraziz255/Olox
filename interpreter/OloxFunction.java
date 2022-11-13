package interpreter;

import lexical_scanner.Token;
import lexical_scanner.TokenType;
import parser.Expr;
import exceptions.Return;

import java.util.List;

public class OloxFunction implements OloxCallable {
    private final String name;
    private final Expr.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;
    OloxFunction(String name, Expr.Function declaration, Environment closure, boolean isInitializer) {
        this.name = name;
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    @Override
    public String toString() {
        if(name == null) return "<fn>";
        return "<fn " + name + ">";
    }

    @Override
    public int arity() {
        return declaration.parameters.size();
    }

    OloxFunction bind(OloxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define(instance);
        return new OloxFunction(name, declaration, environment, isInitializer);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        if(declaration.parameters != null) {
            for (int i = 0; i < declaration.parameters.size(); i++) {
                environment.define(arguments.get(i));
            }
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            if(isInitializer) return closure.getAt(0, closure.getSize() - 1);
            return returnValue.getValue();
        }

        if(isInitializer) {
            return closure.getAt(0, closure.getSize() - 1);
        }

        return null;
    }

    public boolean isGetter() {
        return declaration.parameters == null;
    }
}
