package Jolox.utils;

import Jolox.lexical_scanner.Token;
import Jolox.lexical_scanner.TokenType;
import Jolox.exceptions.RuntimeError;

public class ErrorReporter {
    private boolean hadBuildError = false;
    private boolean hadRuntimeError = false;
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

    public boolean hasBuildError() {
        return hadBuildError;
    }

    public boolean hasRuntimeError() {
        return hadRuntimeError;
    }

    public void setBuildErrorStatus(boolean status) {
        hadBuildError = status;
    }

    private void report(int line, String message) {
        System.err.println("[line " + line + "] Error: " + message);
        this.hadBuildError = true;
    }

    private void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        this.hadBuildError = true;
    }

    public void runTimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.getToken().getLine() + "]");
        hadRuntimeError = true;
    }
}
