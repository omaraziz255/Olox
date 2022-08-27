package olox;

public class ErrorReporter {
    boolean hadError = false;

    void error(int line, String message) {
        report(line, message);
    }

    private void report(int line, String message) {
        System.err.println("[line " + line + "] Error: " + message);
        this.hadError = true;
    }
}
