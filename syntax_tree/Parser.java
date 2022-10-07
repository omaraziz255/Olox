/**
 * This Parser parses the following grammar for Olox
 * program -> declaration* EOF;
 * declaration -> varDecl | statement;
 * varDecl -> "var" IDENTIFIER ( "=" expression)? ";";
 * statement -> exprStmt | printStmt | block;
 * block -> "{" declaration* "}";
 * exprStmt -> expression ";" ;
 * printStmt -> "print" expression ";"
 * expression → comma;
 * comma -> ternary ( "," ternary )* ;
 * ternary -> assignment ( "?" comma ":" ternary)?
 * assignment -> IDENTIFIER "=" assignment | equality;
 * equality → comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 * term → factor ( ( "-" | "+" ) factor )* ;
 * factor → unary ( ( "/" | "*" ) unary )* ;
 * unary → ( "!" | "-" ) unary | primary ;
 * primary → NUMBER | STRING | IDENTIFIER |"true" | "false" | "nil" | "(" expression ")" | "," comma |
 *           ("?" | ":" ) ternary | ("!=" | "==" ) equality | (">" | ">=" | "<" | "<=" ) comparison | "+" term |
 *           ("/" | "*" ) factor;
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

    private final RunMode mode;

    private int current = 0;

    public Parser(List<Token> tokens, RunMode mode) {
        this.tokens = tokens;
        this.mode = mode;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Expr expression() {
        return comma();
    }

    private Stmt declaration() {
        try {
            if(match(VAR)) return varDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt statement() {
        if(match(PRINT)) return printStatement();
        if(match(LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expected ; after value");
        return new Stmt.Print(value);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expected variable name");
        Expr initializer = null;
        if(match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expected ; after variable declaration");
        return new Stmt.Var(name, initializer);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expected ; after expression");
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while(!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expected } at end of block");
        return statements;
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
        Expr expr = assignment();

        if(match(QUESTION)) {
            Expr left = comma();
            consume(COLON, "Expected : after then branch of ternary expression");
            Expr right = ternary();
            expr = new Expr.Ternary(expr, left, right);
        }

        return expr;
    }

    private Expr assignment() {
        Expr expr = equality();
        if(match(EQUAL)) {
            Token equals = previous();
            Expr value =  assignment();

            if(expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            ErrorReporter.getInstance().error(equals, "Invalid assignment target");
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

        if(match(IDENTIFIER)) return new Expr.Variable(previous());

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
        if(ignoreSemiColon(type) || check(type)) return advance();
        throw parsingError(peek(), message);
    }

    private boolean ignoreSemiColon(TokenType type) {
        return this.mode == RunMode.REPL && type == SEMICOLON;
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