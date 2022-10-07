package olox;

import lexical_scanner.Scanner;
import lexical_scanner.Token;
import syntax_tree.Expr;
import syntax_tree.Interpreter;
import syntax_tree.Parser;
import syntax_tree.Stmt;
import utils.ErrorReporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static utils.ExitCode.*;

public class Olox {
    static ErrorReporter errorReporter = ErrorReporter.getInstance();
    static Interpreter interpreter = Interpreter.getInstance();
    public static void main(String[] args) throws IOException {
        if(args.length >  1) {
            System.out.println("Usage: jolox [script_name.lx]");
            System.exit(COMMAND_LINE_USAGE_ERROR.code);
        }
        else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Path.of(path));
        run(new String(bytes, Charset.defaultCharset()));
        if(errorReporter.hasBuildError()) System.exit(USER_DATA_INCORRECT.code);
        if(errorReporter.hasRuntimeError()) System.exit(RUNTIME_ERROR.code);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for(;;) {
            System.out.print(">> ");
            String line = reader.readLine();
            if (line == null) break;    // CTRL-D exits interactive loop (CMD-D for Mac)
            run(line);
            errorReporter.setBuildErrorStatus(false);    // Ensure error flag is reset for each interactive command
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if(errorReporter.hasBuildError()) return;

        interpreter.interpret(statements);
    }
}
