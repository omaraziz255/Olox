package tools;

public enum StmtDefinition {
    BASE_STMT("Stmt"),
    BLOCK_STMT("Block: List<Stmt> statements"),
    BREAK_STMT("Break: "),
    EXPR_STMT("Expression: Expr expression"),
    FUNC_STMT("Function: Token name, List<Token> params, List<Stmt> body"),
    IF_STMT("If: Expr condition, Stmt thenBranch, Stmt elseBranch"),
    PRINT_STMT("Print: Expr expression"),
    RETURN_STMT("Return: Token keyword, Expr value"),
    VARIABLE_STMT("Var: Token name, Expr initializer"),
    WHILE_STMT("While: Expr condition, Stmt body");

    public final String stmt;
    StmtDefinition(String stmt) { this.stmt = stmt; }
}
