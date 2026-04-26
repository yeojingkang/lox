package com.jkyeo.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Lox {
    private static final Interpreter interpreter = new Interpreter();

    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64); // EX_USAGE in Unix's sysexits
        }

        if (args.length == 1)
            runFile(args[0]);
        else
            runPrompt();
    }

    public static void runFile(String path) throws IOException {
        final var bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) System.exit(65); // EX_DATAERR
        if (hadRuntimeError) System.exit(70); // EX_SOFTWARE
    }

    public static void runPrompt() throws IOException {
        final var input = new InputStreamReader(System.in);
        final var reader = new BufferedReader(input);

        while (true) {
            System.out.print("> ");
            final var line = reader.readLine(); // TODO: Read until empty line (or something to denote end of expr/stmt) before run()
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    public static void run(String source) {
        final var scanner = new Scanner(source);
        final var tokens = scanner.scanTokens();

        final var parser = new Parser(tokens);
        final var expr = parser.parse();

        if (hadError) return;

        interpreter.interpret(expr);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    // TODO: Enhance location with character position
    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
