/**
 * This Parser parses the following grammar for Olox
 * program -> statement* EOF;
 * statement -> exprStmt | printStmt;
 * exprStmt -> expression ";" ;
 * printStmt -> "print" expression ";"
 * expression → comma;
 * comma -> ternary ( "," ternary )* ;
 * ternary -> equality ( "?" comma ":" ternary)?
 * equality → comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 * term → factor ( ( "-" | "+" ) factor )* ;
 * factor → unary ( ( "/" | "*" ) unary )* ;
 * unary → ( "!" | "-" ) unary | primary ;
 * primary → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | "," comma | ("?" | ":" ) ternary |
 *           ("!=" | "==" ) equality | (">" | ">=" | "<" | "<=" ) comparison | "+" term | ("/" | "*" ) factor;
 */

package syntax_tree;

import lexical_scanner.Token;
import lexical_scanner.TokenType;
import utils.ErrorReporter;

import static lexical_scanner.TokenType.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;

    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()) {
            statements.add(statement());
        }

        return statements;
    }

    private Expr expression() {
        return comma();
    }

    private Stmt statement() {
        if(match(PRINT)) return printStatement();
        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expected ; after value");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expected ; after expression");
        return new Stmt.Expression(expr);
    }


    /*TODO Handle commas during function argument parsing */
    private Expr comma() {
        Expr expr = ternary();

        while(match(COMMA)) {
            Token comma = previous();
            Expr right = ternary();
            expr = new Expr.Binary(expr, comma, right);
        }

        return expr;
    }

    private Expr ternary() {
        Expr expr = equality();

        if(match(QUESTION)) {
            Expr left = comma();
            consume(COLON, "Expected : after then branch of ternary expression");
            Expr right = ternary();
            expr = new Expr.Ternary(expr, left, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while(match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private boolean match(TokenType... types) {
        for(TokenType type : types) {
            if(check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if(isAtEnd()) return false;
        return peek().getType() == type;
    }

    private Token advance() {
        if(!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(STAR, SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if(match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if(match(FALSE)) return new Expr.Literal(false);
        if(match(TRUE)) return new Expr.Literal(true);
        if(match(NIL)) return new Expr.Literal(null);

        if(match(NUMBER, STRING)) return new Expr.Literal(previous().getLiteral());

        if(match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected ')' after expression.");
            return new Expr.Grouping(expr);
        }

        if(match(COMMA)) {
            error(previous(), "Missing left hand expression.");
            comma();
            return null;
        }

        if(match(QUESTION, COLON)) {
            error(previous(), "Missing expression in ternary condition.");
            ternary();
            return null;
        }

        if(match(BANG_EQUAL, EQUAL_EQUAL)) {
            error(previous(), "Missing left hand expression in equality");
            equality();
            return null;
        }

        if(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            error(previous(), "Missing left hand expression in comparison");
            comparison();
            return null;
        }

        if(match(PLUS)) {
            error(previous(), "Missing left hand expression in addition");
            term();
            return null;
        }

        if(match(SLASH, STAR)) {
            error(previous(), "Missing left hand expression in multiplication/division");
            factor();
            return null;
        }

        throw parsingError(peek(), "Expected Expression.");
    }

    private Token consume(TokenType type, String message) {
        if(check(type)) return advance();
        throw parsingError(peek(), message);
    }

    private ParseError parsingError(Token token, String message) {
        ErrorReporter.getInstance().error(token, message);
        return new ParseError();
    }

    private void error(Token token, String message) {

        ErrorReporter.getInstance().error(token, message);
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if(previous().getType() == SEMICOLON) return;

            switch (peek().getType()) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}