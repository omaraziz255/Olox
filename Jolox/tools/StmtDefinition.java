package Jolox.tools;

public enum StmtDefinition {
    BASE_STMT("Stmt"),
    BLOCK_STMT("Block: List<Stmt> statements"),
    BREAK_STMT("Break: "),
    CLASS_STMT("Class: Token name, Expr.Variable superclass, List<Stmt.Function> methods, " +
            "List<Stmt.Function> classMethods"),
    EXPR_STMT("Expression: Expr expression"),
    FUNC_STMT("Function: Token name, Expr.Function function"),
    IF_STMT("If: Expr condition, Stmt thenBranch, Stmt elseBranch"),
    PRINT_STMT("Print: Expr expression"),
    RETURN_STMT("Return: Token keyword, Expr value"),
    VARIABLE_STMT("Var: Token name, Expr initializer"),
    WHILE_STMT("While: Expr condition, Stmt body");

    public final String stmt;
    StmtDefinition(String stmt) { this.stmt = stmt; }
}
