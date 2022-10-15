package tools;

public enum ExprDefinition {

    ASSIGN_EXPR("Assign: Token name, Expr value"),
    BASE_EXPR("Expr"),
    BINARY_EXPR("Binary: Expr left, Token operator, Expr right"),
    CALL_EXPR("Call: Expr callee, Token paren, List<Expr> arguments"),
    FUNC_EXPR("Function: List<Token> parameters, List<Stmt> body"),
    GROUPING("Grouping: Expr expression"),
    LITERAL("Literal: Object value"),
    LOGICAL("Logical: Expr left, Token operator, Expr right"),
    TERNARY("Ternary: Expr condition, Expr left, Expr right"),
    UNARY_EXPR("Unary: Token operator, Expr right"),
    VARIABLE_EXPR("Variable: Token name");

    public final String expr;
    ExprDefinition(String expr) { this.expr = expr; }
}
