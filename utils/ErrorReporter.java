package utils;

import lexical_scanner.Token;
import lexical_scanner.TokenType;

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

    public void error(Token token, String message) {
        if(token.getType() == TokenType.EOF) {
            report(token.getLine(), " at end", message);
        } else {
            report(token.getLine(), " at '" + token.getLexeme() + "'", message);
        }
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

    private void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        this.hadError = true;
    }
}
