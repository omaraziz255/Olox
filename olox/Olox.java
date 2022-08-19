package olox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.stream.Stream;

public class Olox {
    public static void main(String[] args) throws IOException {
        if(args.length >  1) {
            System.out.println("Usage: jolox [script_name.lx]");
            System.exit(64);        //Error Code 64 indicates Command Line Usage Error
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
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for(;;) {
            System.out.print(">> ");
            String line = reader.readLine();
            if (line == null) break;    // CTRL-D exits interactive loop (CMD-D for Mac)
            run(line);
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        Stream<String> tokens = scanner.tokens();

        tokens.forEach(System.out::println);
    }
}
