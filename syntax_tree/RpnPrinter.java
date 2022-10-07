package syntax_tree;

import lexical_scanner.TokenType;

import java.util.Objects;

@SuppressWarnings("unused")
public class RpnPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return expr.name.getLexeme() + " " + expr.value.accept(this) + " = ";
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return expr.left.accept(this) + " " + expr.right.accept(this) + " " + expr.operator.getLexeme();
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return expr.expression.accept(this);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if(expr.value == null) return "nil";
        return expr.value.toString();
    }
    @Override
    public String visitTernaryExpr(Expr.Ternary expr) {
        return expr.condition.accept(this) + " " + expr.left.accept(this) + " " +
                expr.right.accept(this) + " :?";
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return Objects.equals(expr.operator.getLexeme(), TokenType.MINUS.name()) ?
                expr.right.accept(this) + " ~" : expr.right.accept(this) + " " + expr.operator.getLexeme();
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.getLexeme();
    }
}
