package tools;

public enum ExprDefinition {

    ASSIGN_EXPR("Assign: Token name, Expr value"),
    BASE_EXPR("Expr"),
    BINARY_EXPR("Binary: Expr left, Token operator, Expr right"),
    CALL_EXPR("Call: Expr callee, Token paren, List<Expr> arguments"),
    FUNC_EXPR("Function: List<Token> parameters, List<Stmt> body"),
    GET_EXPR("Get: Expr object, Token name"),
    GROUPING_EXPR("Grouping: Expr expression"),
    LITERAL_EXPR("Literal: Object value"),
    LOGICAL_EXPR("Logical: Expr left, Token operator, Expr right"),
    SET_EXPR("Set: Expr object, Token name, Expr value"),
    SUPER_EXPR("Super: Token keyword, Token method"),
    TERNARY_EXPR("Ternary: Expr condition, Expr left, Expr right"),
    THIS_EXPR("This: Token keyword"),
    UNARY_EXPR("Unary: Token operator, Expr right"),

    VARIABLE_EXPR("Variable: Token name");

    public final String expr;
    ExprDefinition(String expr) { this.expr = expr; }
}
