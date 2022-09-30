package syntax_tree;

public class AstPrinter implements Expr.Visitor<String> {
        public String print(Expr expr) {
            return expr.accept(this);
        }

        @Override
        public String visitBinaryExpr(Expr.Binary expr) {
            return parenthesize(expr.operator.getLexeme(), expr.left, expr.right);
        }

        @Override
        public String visitGroupingExpr(Expr.Grouping expr) {
            return parenthesize("group", expr.expression);
        }

        @Override
        public String visitLiteralExpr(Expr.Literal expr) {
            if(expr.value == null) return "nil";
            return expr.value.toString();
        }

        @Override
        public String visitTernaryExpr(Expr.Ternary expr) {
            return parenthesizeTernary(expr);
        }

        @Override
        public String visitUnaryExpr(Expr.Unary expr) {
            return parenthesize(expr.operator.getLexeme(), expr.right);
        }

        private String parenthesize(String name, Expr... expressions) {
            StringBuilder builder = new StringBuilder();
            builder.append("(").append(name);
            for(Expr expr : expressions) {
                builder.append(" ");
                builder.append(expr.accept(this));
            }

            builder.append(")");

            return builder.toString();
        }

        private String parenthesizeTernary(Expr.Ternary expr) {
            String name = "condition " + expr.condition.accept(this);
            return parenthesize(name, expr.left, expr.right);
        }
}
