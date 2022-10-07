package tools;

public enum StmtDefinition {
    BASE_STMT("Stmt"),

    EXPR_STMT("Expression: Expr expression"),

    PRINT_STMT("Print: Expr expression");

    public final String stmt;
    StmtDefinition(String stmt) { this.stmt = stmt; }
}
