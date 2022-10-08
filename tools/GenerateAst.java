package tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static tools.ExprDefinition.*;
import static tools.StmtDefinition.*;
import static utils.ExitCode.COMMAND_LINE_USAGE_ERROR;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output_directory>");
            System.exit(COMMAND_LINE_USAGE_ERROR.code);
        }

        String outputDir = args[0];
        defineAst(outputDir, BASE_EXPR.expr, Arrays.asList(
                ASSIGN_EXPR.expr,
                BINARY_EXPR.expr,
                GROUPING.expr,
                LITERAL.expr,
                LOGICAL.expr,
                TERNARY.expr,
                UNARY_EXPR.expr,
                VARIABLE_EXPR.expr
        ));

        defineAst(outputDir, BASE_STMT.stmt, Arrays.asList(
                BLOCK_STMT.stmt,
                EXPR_STMT.stmt,
                IF_STMT.stmt,
                PRINT_STMT.stmt,
                VARIABLE_STMT.stmt
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("""
                       /**
                       This file has been generated by the GenerateAst module in the tools package
                       */""");
        writer.println("package syntax_tree;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
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

        writer.println("    " + className + "(" + fieldList + ") {");

        String[] fields = fieldList.split(", ");
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

