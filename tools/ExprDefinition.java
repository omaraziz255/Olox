package tools;

public enum ExprDefinition {
    BASE_EXPR("Expr"),
    BINARY_EXPR("Binary: Expr left, Token operator, Expr right"),
    GROUPING("Grouping: Expr expression"),
    LITERAL("Literal: Object value"),
    UNARY_EXPR("Unary: Token operator, Expr right");
    public final String expr;
    ExprDefinition(String expr) { this.expr = expr; }
}
