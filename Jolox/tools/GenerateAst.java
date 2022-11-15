package Jolox.tools;

import Jolox.utils.PackageType;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static Jolox.tools.ExprDefinition.*;
import static Jolox.tools.StmtDefinition.*;
import static Jolox.utils.ExitCode.COMMAND_LINE_USAGE_ERROR;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: generate_ast <expression_dir> <statement_dir>");
            System.exit(COMMAND_LINE_USAGE_ERROR.code);
        }

        String expressionDir = args[0];
        String stmtDir = args[1];
        defineAst(expressionDir, BASE_EXPR.expr, Arrays.asList(
                ASSIGN_EXPR.expr,
                BINARY_EXPR.expr,
                CALL_EXPR.expr,
                FUNC_EXPR.expr,
                GET_EXPR.expr,
                GROUPING_EXPR.expr,
                LITERAL_EXPR.expr,
                LOGICAL_EXPR.expr,
                SET_EXPR.expr,
                SUPER_EXPR.expr,
                TERNARY_EXPR.expr,
                THIS_EXPR.expr,
                UNARY_EXPR.expr,
                VARIABLE_EXPR.expr
        ), PackageType.EXPRESSION);

        defineAst(stmtDir, BASE_STMT.stmt, Arrays.asList(
                BLOCK_STMT.stmt,
                BREAK_STMT.stmt,
                CLASS_STMT.stmt,
                EXPR_STMT.stmt,
                FUNC_STMT.stmt,
                IF_STMT.stmt,
                PRINT_STMT.stmt,
                RETURN_STMT.stmt,
                VARIABLE_STMT.stmt,
                WHILE_STMT.stmt
        ), PackageType.STMT);
    }

    private static void defineAst(String outputDir, String baseName, List<String> types, PackageType pkg) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("""
                       /**
                       This file has been generated by the GenerateAst module in the tools package
                       */""");
        writer.println("package " + pkg.type + ";");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println(Objects.equals(pkg.type, PackageType.STMT.type) ?
                "import " + PackageType.EXPRESSION.type + ".Expr;" : "import " + PackageType.STMT.type + ".Stmt;");
        writer.println("import lexical_scanner.Token;");
        writer.println();
        writer.println("abstract public class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println();
        writer.println("    public abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println(" static public class " + className + " extends " + baseName + " {");

        writer.println("    public " + className + "(" + fieldList + ") {");

        String[] fields = fieldList.isEmpty() ? new String[0] : fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("    this." + name + " = " + name + ";");
        }

        writer.println("    }");

        writer.println();
        writer.println("    @Override");
        writer.println("    public <R> R accept(Visitor<R> visitor) {");
        writer.println("        return visitor.visit" + className + baseName + "(this);");
        writer.println("    }");

        writer.println();
        for (String field : fields) {
            writer.println("    public final " + field + ";");
        }

        writer.println();
        writer.println("}");
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    public interface Visitor<R> {");

        for(String type: types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("    }");
    }
}
