package com.jkyeo.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usages: generate_ast <output_directory>");
            System.exit(64); // EX_USAGE
        }

        final var outputDir = args[0];

        defineAst(outputDir, "Expr", Map.ofEntries(
            Map.entry("Binary", List.of(
                "Expr left",
                "Token operator",
                "Expr right"
            )),
            Map.entry("Grouping", List.of("Expr expression")),
            Map.entry("Literal", List.of("Object value")),
            Map.entry("Unary", List.of(
                "Token operator",
                "Expr right"
            )),
            Map.entry("Variable", List.of("Token name")),
            Map.entry("Assign", List.of(
                "Token name",
                "Expr value"
            )),
            Map.entry("Logical", List.of(
                "Expr left",
                "Token operator",
                "Expr right"
            )),
            Map.entry("Call", List.of(
                "Expr callee",
                "Token paren",
                "List<Expr> arguments"
            ))
        ));

        defineAst(outputDir, "Stmt", Map.ofEntries(
            Map.entry("Expression", List.of("Expr expression")),
            Map.entry("Print", List.of("Expr expression")),
            Map.entry("Var", List.of(
                "Token name",
                "Expr init"
            )),
            Map.entry("Block", List.of("List<Stmt> statements")),
            Map.entry("If", List.of(
                "Expr condition",
                "Stmt thenBranch",
                "Stmt elseBranch"
            )),
            Map.entry("While", List.of(
                "Expr condition",
                "Stmt body"
            )),
            Map.entry("Function", List.of(
                "Token name",
                "List<Token> params",
                "List<Stmt> body"
            ))
        ));
    }

    private static void defineAst(String outputDir, String baseName, Map<String, List<String>> types) throws IOException {
        final var path = outputDir + "/" + baseName + ".java";
        final var writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package com.jkyeo.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + "{");

        defineVisitor(writer, baseName, types);

        writer.println();
        writer.println("  abstract <R> R accept(Visitor<R> visitor);");

        // AST types
        for (final var type : types.entrySet()) {
            final var className = type.getKey();
            final var fields = type.getValue();
            writer.println();
            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, Map<String, List<String>> types) {
        writer.println("  interface Visitor<R> {");

        for (final var typeName : types.keySet())
            writer.println("    R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");

        writer.println("  }");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, List<String> fields) {
        writer.println("  static class " + className + " extends " + baseName + " {");

        // Constructor
        writer.println("    " + className + "(" + String.join(", ", fields) + ") {");
        for (final var field : fields) {
            final var name = field.split(" ")[1].trim();
            writer.println("      this." + name + " = " + name + ";");
        }
        writer.println("    }");

        // Visitor pattern
        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" + className + baseName + "(this);");
        writer.println("    }");

        // Fields
        writer.println();
        for (final var field : fields)
            writer.println("    final " + field + ";");

        writer.println("  }");
    }
}
