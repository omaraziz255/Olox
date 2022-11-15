package Jolox.resolver;

import Jolox.interpreter.Interpreter;
import Jolox.interpreter.Stmt;
import Jolox.lexical_scanner.Token;
import Jolox.lexical_scanner.TokenType;
import Jolox.parser.Expr;
import Jolox.utils.ClassType;
import Jolox.utils.ErrorReporter;
import Jolox.utils.FunctionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Variable>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;

    private ClassType currentClass = ClassType.NONE;

    private static class Variable {
        final Token name;
        VariableState state;
        final int slot;

        private Variable(Token name, VariableState state, int slot) {
            this.name = name;
            this.state = state;
            this.slot = slot;
        }
    }

    private enum VariableState {
        DECLARED,
        DEFINED,
        READ
    }

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public void resolve(List<Stmt> statements) {
        for(Stmt statement : statements) {
            resolve(statement);
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(stmt.name);
        define(stmt.name);

        if(stmt.superclass != null) {
            if(stmt.name.getLexeme().equals(stmt.superclass.name.getLexeme())) {
                ErrorReporter.getInstance().error(stmt.superclass.name, "A class can't inherit from itself");
            }
            currentClass = ClassType.SUBCLASS;
            resolve(stmt.superclass);
            Token superToken = new Token(TokenType.SUPER, "super", null, stmt.superclass.name.getLine());
            beginScope();
            scopes.peek().put(superToken.getLexeme(),
                    new Variable(superToken, VariableState.READ, scopes.peek().size()));
        }

        beginScope();
        Token thisToken = new Token(TokenType.THIS, "this", null, stmt.name.getLine());
        scopes.peek().put(thisToken.getLexeme(), new Variable(thisToken, VariableState.READ, scopes.peek().size()));
        for(Stmt.Function method : stmt.methods) {
            FunctionType declaration = FunctionType.METHOD;
            if(method.name.getLexeme().equals("init")) {
                declaration = FunctionType.INITIALIZER;
            }
            resolveFunction(method, declaration);
        }

        for(Stmt.Function classMethod : stmt.classMethods) {
            beginScope();
            scopes.peek().put(thisToken.getLexeme(), new Variable(thisToken, VariableState.READ, scopes.peek().size()));
            resolveFunction(classMethod, FunctionType.METHOD);
            endScope();
        }

        if(stmt.superclass != null) endScope();
        endScope();
        currentClass = enclosingClass;
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.METHOD);
        return null;
    }

    private void resolveFunction(Stmt.Function stmt, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        stmt.function.accept(this);
        currentFunction = enclosingFunction;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if(stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if(currentFunction == FunctionType.NONE) {
            ErrorReporter.getInstance().error(stmt.keyword, "Can't return from top-level code");
        }

        if(stmt.value != null) {
            if(currentFunction == FunctionType.INITIALIZER) {
                ErrorReporter.getInstance().error(stmt.keyword, "Can't return a value from an initializer");
            }
            resolve(stmt.value);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if(stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name, false);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for(Expr argument : expr.arguments) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitFunctionExpr(Expr.Function expr) {
        beginScope();
        if(expr.parameters != null) {
            for (Token param : expr.parameters) {
                declare(param);
                define(param);
            }
        }
        resolve(expr.body);
        endScope();

        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        if(currentClass == ClassType.NONE) {
            ErrorReporter.getInstance().error(expr.keyword, "Can't use super outside class definition");
        }
        else if(currentClass == ClassType.CLASS) {
            ErrorReporter.getInstance().error(expr.keyword, "Can't use super in class with no superclass");
        }
        resolveLocal(expr, expr.keyword, true);
        return null;
    }

    @Override
    public Void visitTernaryExpr(Expr.Ternary expr) {
        resolve(expr.condition);
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        if(currentClass == ClassType.NONE) {
            ErrorReporter.getInstance().error(expr.keyword, "Can't use keyword 'this' outside a class");
        }
        resolveLocal(expr, expr.keyword, true);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if(!scopes.isEmpty() && scopes.peek().containsKey(expr.name.getLexeme()) &&
            scopes.peek().get(expr.name.getLexeme()).state == VariableState.DECLARED ) {
            ErrorReporter.getInstance().error(expr.name, "Can't read local variable in its own initializer");
        }

        resolveLocal(expr, expr.name, true);
        return null;
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        Map<String,Variable> scope = scopes.pop();
        for(Map.Entry<String, Variable> entry : scope.entrySet()) {
            if(entry.getValue().state == VariableState.DEFINED) {
                ErrorReporter.getInstance().error(entry.getValue().name, "Local variable is never used");
            }
        }
    }

    private void declare(Token name) {
        if(scopes.isEmpty()) return;

        Map<String, Variable> scope = scopes.peek();
        if(scope.containsKey(name.getLexeme())) {
            ErrorReporter.getInstance().error(name, "Variable with this name already declared in this scope");
        }
        scope.put(name.getLexeme(), new Variable(name, VariableState.DECLARED, scope.size()));
    }

    private void define(Token name) {
        if(scopes.isEmpty()) return;
        scopes.peek().get(name.getLexeme()).state = VariableState.DEFINED;
    }

    private void resolveLocal(Expr expr, Token name, boolean isRead) {
        for(int i = scopes.size() - 1; i >= 0; i--) {
            if(scopes.get(i).containsKey(name.getLexeme())) {
                interpreter.resolve(expr, scopes.size() - 1 - i, scopes.get(i).get(name.getLexeme()).slot);

                if(isRead) {
                    scopes.get(i).get(name.getLexeme()).state = VariableState.READ;
                }
                return;
            }
        }
    }




}
