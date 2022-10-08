/**
This file has been generated by the GenerateAst module in the tools package
*/
package syntax_tree;


import lexical_scanner.Token;

abstract public class Expr {
    public interface Visitor<R> {
    R visitAssignExpr(Assign expr);
    R visitBinaryExpr(Binary expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitLogicalExpr(Logical expr);
    R visitTernaryExpr(Ternary expr);
    R visitUnaryExpr(Unary expr);
    R visitVariableExpr(Variable expr);
    }
 static public class Assign extends Expr {
    Assign(Token name, Expr value) {
    this.name = name;
    this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitAssignExpr(this);
    }

    public final Token name;
    public final Expr value;

}
 static public class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitBinaryExpr(this);
    }

    public final Expr left;
    public final Token operator;
    public final Expr right;

}
 static public class Grouping extends Expr {
    Grouping(Expr expression) {
    this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitGroupingExpr(this);
    }

    public final Expr expression;

}
 static public class Literal extends Expr {
    Literal(Object value) {
    this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitLiteralExpr(this);
    }

    public final Object value;

}
 static public class Logical extends Expr {
    Logical(Expr left, Token operator, Expr right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitLogicalExpr(this);
    }

    public final Expr left;
    public final Token operator;
    public final Expr right;

}
 static public class Ternary extends Expr {
    Ternary(Expr condition, Expr left, Expr right) {
    this.condition = condition;
    this.left = left;
    this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitTernaryExpr(this);
    }

    public final Expr condition;
    public final Expr left;
    public final Expr right;

}
 static public class Unary extends Expr {
    Unary(Token operator, Expr right) {
    this.operator = operator;
    this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitUnaryExpr(this);
    }

    public final Token operator;
    public final Expr right;

}
 static public class Variable extends Expr {
    Variable(Token name) {
    this.name = name;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitVariableExpr(this);
    }

    public final Token name;

}

    public abstract <R> R accept(Visitor<R> visitor);
}
