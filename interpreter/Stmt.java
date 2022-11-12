/**
This file has been generated by the GenerateAst module in the tools package
*/
package interpreter;

import java.util.List;

import parser.Expr;
import lexical_scanner.Token;

abstract public class Stmt {
    public interface Visitor<R> {
    R visitBlockStmt(Block stmt);
    R visitBreakStmt(Break stmt);
    R visitClassStmt(Class stmt);
    R visitExpressionStmt(Expression stmt);
    R visitFunctionStmt(Function stmt);
    R visitIfStmt(If stmt);
    R visitPrintStmt(Print stmt);
    R visitReturnStmt(Return stmt);
    R visitVarStmt(Var stmt);
    R visitWhileStmt(While stmt);
    }
 static public class Block extends Stmt {
    public Block(List<Stmt> statements) {
    this.statements = statements;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitBlockStmt(this);
    }

    public final List<Stmt> statements;

}
 static public class Break extends Stmt {
    public Break() {
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitBreakStmt(this);
    }


}
 static public class Class extends Stmt {
    public Class(Token name, List<Stmt.Function> methods) {
    this.name = name;
    this.methods = methods;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitClassStmt(this);
    }

    public final Token name;
    public final List<Stmt.Function> methods;

}
 static public class Expression extends Stmt {
    public Expression(Expr expression) {
    this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitExpressionStmt(this);
    }

    public final Expr expression;

}
 static public class Function extends Stmt {
    public Function(Token name, Expr.Function function) {
    this.name = name;
    this.function = function;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitFunctionStmt(this);
    }

    public final Token name;
    public final Expr.Function function;

}
 static public class If extends Stmt {
    public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
    this.condition = condition;
    this.thenBranch = thenBranch;
    this.elseBranch = elseBranch;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitIfStmt(this);
    }

    public final Expr condition;
    public final Stmt thenBranch;
    public final Stmt elseBranch;

}
 static public class Print extends Stmt {
    public Print(Expr expression) {
    this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitPrintStmt(this);
    }

    public final Expr expression;

}
 static public class Return extends Stmt {
    public Return(Token keyword, Expr value) {
    this.keyword = keyword;
    this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitReturnStmt(this);
    }

    public final Token keyword;
    public final Expr value;

}
 static public class Var extends Stmt {
    public Var(Token name, Expr initializer) {
    this.name = name;
    this.initializer = initializer;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitVarStmt(this);
    }

    public final Token name;
    public final Expr initializer;

}
 static public class While extends Stmt {
    public While(Expr condition, Stmt body) {
    this.condition = condition;
    this.body = body;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitWhileStmt(this);
    }

    public final Expr condition;
    public final Stmt body;

}

    public abstract <R> R accept(Visitor<R> visitor);
}
