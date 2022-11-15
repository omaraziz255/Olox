package Jolox.interpreter;

import Jolox.parser.Expr;
import Jolox.exceptions.BreakException;
import Jolox.exceptions.Return;
import Jolox.exceptions.RuntimeError;
import Jolox.lexical_scanner.Token;
import Jolox.lexical_scanner.TokenType;
import Jolox.utils.ErrorReporter;
import Jolox.utils.RunMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Map<String, Object> globals = new HashMap<>();
    private Environment environment;
    private final Map<Expr, Integer> locals = new HashMap<>();
    private final Map<Expr, Integer> slots = new HashMap<>();

    private RunMode mode = RunMode.FILE;

    private Interpreter(){
        globals.put("clock", new OloxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
    }

    private static final Interpreter instance = new Interpreter();

    public static Interpreter getInstance() {
        return instance;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if(expr.operator.getType() == TokenType.OR) {
            if(isTrue(left)) return left;
        }
        else {
            if(!isTrue(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);

        if(!(object instanceof OloxInstance)) {
            throw new RuntimeError(expr.name, "Only instances have fields");
        }

        Object value = evaluate(expr.value);
        ((OloxInstance) object).set(expr.name, value);
        return value;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        int distance = locals.get(expr);
        OloxClass superclass = (OloxClass)environment.getAt(distance, 0);
        OloxInstance object = (OloxInstance)environment.getAt(distance - 1, 0);

        OloxFunction method = superclass.findMethod(expr.method.getLexeme());
        if(method == null) {
            throw new RuntimeError(expr.method, "Undefined property " + expr.method.getLexeme() + ".");
        }
        return method.bind(object);
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        Object condition = evaluate(expr.condition);

        return (isTrue(condition)? evaluate(expr.left) : evaluate(expr.right));
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariable(expr.keyword, expr);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.getType()) {
            case BANG -> {
                return !isTrue(right);
            }
            case MINUS -> {
                checkNumberOperand(expr.operator, right);
                return - (double)right;
            }
        }

        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.name, expr);
    }

    public Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if(distance != null) {
            return environment.getAt(distance, slots.get(expr));
        } else {
            if(globals.containsKey(name.getLexeme())) {
                return globals.get(name.getLexeme());
            } else {
                throw new RuntimeError(name, "Undefined variable " + name.getLexeme() + " .");
            }
        }
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if(operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number");
    }

    private boolean isTrue(Object object) {
        if(object == null) return false;
        if(object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object left, Object right) {
        if(left == null && right == null) return true;
        if(left == null) return false;

        return left.equals(right);
    }

    private String stringify(Object object) {
        if(object == null) return "nil";

        if(object instanceof Double) {
            String text = object.toString();
            if(text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }

            return text;
        }

        return object.toString();
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    public void resolve(Expr expr, int depth, int slot) {
        locals.put(expr, depth);
        slots.put(expr, slot);
    }

    public void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for(Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new BreakException();
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        Map<String, OloxFunction> methods = new HashMap<>();
        Map<String, OloxFunction> classMethods = new HashMap<>();
        Object superclass = null;
        if(stmt.superclass != null) {
            superclass = evaluate(stmt.superclass);
            if(!(superclass instanceof OloxClass)) {
                throw new RuntimeError(stmt.superclass.name, "Superclass must be a defined class");
            }

            environment = new Environment(environment);
            define(stmt.superclass.name, superclass);
        }


        for(Stmt.Function classMethod: stmt.classMethods) {
            OloxFunction function = new OloxFunction(classMethod.name.getLexeme(), classMethod.function,
                    environment, false);
            classMethods.put(classMethod.name.getLexeme(), function);
        }

        OloxClass metaclass = new OloxClass(null, stmt.name.getLexeme() + " metaclass", (OloxClass) superclass, classMethods);
        for(Stmt.Function method : stmt.methods) {
            OloxFunction function = new OloxFunction(method.name.getLexeme(), method.function, environment,
                    method.name.getLexeme().matches("init"));
            methods.put(method.name.getLexeme(), function);
        }

        OloxClass klass = new OloxClass(metaclass, stmt.name.getLexeme(), (OloxClass)superclass, methods);

        if(superclass != null) environment = environment.enclosing;
        define(stmt.name, klass);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        Object value = evaluate(stmt.expression);
        if(this.mode == RunMode.REPL && value != null) System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        define(stmt.name, new OloxFunction(stmt.name.getLexeme() ,stmt.function, environment, false));
        return null;
    }

    public void define(Token name, Object value) {
        if(environment != null) {
            environment.define(value);
        } else {
            globals.put(name.getLexeme(), value);
        }
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if(isTrue(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if(stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if(this.mode == RunMode.REPL) System.out.println(stmt.name.getLexeme() + " = " + stringify(value));
        }
       define(stmt.name, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        try {
            while (isTrue(evaluate(stmt.condition))) {
                execute(stmt.body);
            }
        } catch (BreakException ignored) {

        }
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);

        Integer distance = locals.get(expr);
        if(distance != null) {
            environment.assignAt(distance, slots.get(expr), value);
        }
        else {
            if(globals.containsKey(expr.name.getLexeme())) {
                globals.put(expr.name.getLexeme(), value);
            } else {
                throw new RuntimeError(expr.name, "Undefined variable " + expr.name.getLexeme() + " .");
            }
        }
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.getType()) {
            case BANG_EQUAL -> {
                return !isEqual(left, right);
            }
            case EQUAL_EQUAL -> {
                return isEqual(left, right);
            }
            case GREATER -> {
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            }
            case LESS -> {
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            }
            case MINUS -> {
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            }
            case PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }

                if (left instanceof String || right instanceof String) {
                    return stringify(left) + stringify(right);
                }

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings");
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                if((double)right == 0) throw new RuntimeError(expr.operator, "Arithmetic Error: Division by Zero");
                return (double)left / (double)right;
            }
            case STAR -> {
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            }
        }

        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for(Expr argument: expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if(!(callee instanceof OloxCallable function)) {
            throw new RuntimeError(expr.paren, "Only functions and classes are callable");
        }

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but got " +
                    arguments.size());
        }
        return function.call(this, arguments);
    }

    @Override
    public Object visitFunctionExpr(Expr.Function expr) {
        return new OloxFunction(null, expr, environment, false);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if(object instanceof OloxInstance) {
            Object result =  ((OloxInstance) object).get(expr.name);
            if(result instanceof OloxFunction && ((OloxFunction)result).isGetter()) {
                result = ((OloxFunction) result).call(this, null);
            }

            return result;
        }

        throw new RuntimeError(expr.name, "Only instances can have properties");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if(left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers");
    }

    public void interpret(List<Stmt> statements, RunMode mode) {
        this.mode = mode;
        try {
            for(Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            ErrorReporter.getInstance().runTimeError(error);
        }
    }
}
