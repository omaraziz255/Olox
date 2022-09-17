package utils;

public class ErrorReporter {
    private boolean hadError = false;
    private ErrorReporter(){}

    private static final ErrorReporter instance = new ErrorReporter();

    public static ErrorReporter getInstance() {
        return instance;
    }

    public void error(int line, String message) {
        report(line, message);
    }

    public boolean getErrorStatus() {
        return hadError;
    }

    public void setErrorStatus(boolean status) {
        hadError = status;
    }

    private void report(int line, String message) {
        System.err.println("[line " + line + "] Error: " + message);
        this.hadError = true;
    }
}
