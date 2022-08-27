package olox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static olox.ExitCode.COMMAND_LINE_USAGE_ERROR;
import static olox.ExitCode.USER_DATA_INCORRECT;

public class Olox {
    static ErrorReporter errorReporter = new ErrorReporter();
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
        if(errorReporter.hadError) System.exit(USER_DATA_INCORRECT.code);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for(;;) {
            System.out.print(">> ");
            String line = reader.readLine();
            if (line == null) break;    // CTRL-D exits interactive loop (CMD-D for Mac)
            run(line);
            errorReporter.hadError = false;    // Ensure error flag is reset for each interactive command
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);

        List<Token> tokens = scanner.scanTokens();

        tokens.forEach(System.out::println);
    }
}
