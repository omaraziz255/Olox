package Jolox.lexical_scanner;

import Jolox.utils.ErrorReporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Jolox.lexical_scanner.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("break", BREAK);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case ';' -> addToken(SEMICOLON);
            case '*' -> addToken(STAR);
            case '?' -> addToken(QUESTION);
            case ':' -> addToken(COLON);
            case '!' -> addToken(match('=') ? BANG_EQUAL: BANG);
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                }
                else if (match('*')) {
                    commentBlock();
                }
                else {
                    addToken(SLASH);
                }
            }
            case ' ', '\r', '\t' -> {
            }
            case '\n' -> line++;
            case '"' -> string();
            default -> {
                if(isDigit(c)) {
                    number();
                }
                else if(isAlpha(c)) {
                    identifier();
                }
                else {
                    ErrorReporter.getInstance().error(line, "Unexpected character.");
                }
            }
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();
        if (peek() == '.' && isDigit((peekNext()))) {
            advance();
            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start,current)));

    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') line++;
            advance();
        }

        if(isAtEnd()) {
            ErrorReporter.getInstance().error(line, "Unterminated String");
            return;
        }

        advance();
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void commentBlock() {
        while (!isEndOfCommentBlock()) {
            if(isAtEnd()) {
                ErrorReporter.getInstance().error(line, "Unterminated Comment Block");
                return;
            }
            if(peek() == '\n') line++;
            if(peek() == '/' && peekNext() == '*') {
                advance();
                advance();
                commentBlock();
            }
            advance();
        }
        advance();
        advance();
    }

    private boolean match(char expected) {
        if(isAtEnd()) return false;
        if(source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current+1);
    }

    private boolean isEndOfCommentBlock() {
        return (peek() == '*' && peekNext() == '/');
    }

    private boolean isAlpha(char c) {
        return ( c >= 'a' && c <= 'z') ||
               ( c >= 'A' && c <= 'Z') ||
                 c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        char current_char = source.charAt(current);
        current++;
        return current_char;
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}