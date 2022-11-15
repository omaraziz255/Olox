/**
 * This Parser parses the following grammar for Olox
 * program -> declaration* EOF;
 * declaration -> classDecl | funDecl | varDecl | statement;
 * classDecl -> "class" IDENTIFIER ( "<" IDENTIFIER) ? "{" function* "}";
 * funDecl -> "fun" function;
 * function -> IDENTIFIER "(" parameters? ")" block;
 * parameters -> IDENTIFIER ( "," IDENTIFIER)* ;
 * varDecl -> "var" IDENTIFIER ( "=" expression)? ";";
 * statement -> exprStmt | forStmt | ifStmt | printStmt | returnStmt | whileStmt | block;
 * returnStmt -> "return" expression? ";" ;
 * block -> "{" declaration* "}";
 * exprStmt -> expression ";" ;
 * forStmt -> "for" "(" ( varDecl | exprStmt | ";" ) expression? ";" expression? ")" statement;
 * ifStmt -> "if" "(" expression ")" statement ( "else" statement )? ;
 * printStmt -> "print" expression ";"
 * whileStmt -> "while" "(" expression ")" statement;
 * expression → comma;
 * comma -> ternary ( "," ternary )* ;
 * ternary -> assignment ( "?" comma ":" ternary)?
 * assignment -> (call "." )? IDENTIFIER "=" assignment | logic_or;
 * logic_or -> logic_and ( "or" logic_and)*;
 * logic_and -> equality ( "and" equality)*;
 * equality → comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 * term → factor ( ( "-" | "+" ) factor )* ;
 * factor → unary ( ( "/" | "*" ) unary )* ;
 * unary → ( "!" | "-" ) unary | call ;
 * call -> primary ( "(" equality? ")"  | "." IDENTIFIER )* ;
 * primary → NUMBER | STRING | IDENTIFIER |"true" | "false" | "nil" | "(" expression ")" | "," comma |
 *           ("?" | ":" ) ternary | ("!=" | "==" ) equality | (">" | ">=" | "<" | "<=" ) comparison | "+" term |
 *           ("/" | "*" ) factor | "super" "." IDENTIFIER;
 */

package Jolox.parser;

import Jolox.lexical_scanner.Token;
import Jolox.lexical_scanner.TokenType;
import Jolox.utils.FunctionType;
import Jolox.utils.RunMode;
import Jolox.interpreter.Stmt;
import Jolox.utils.ErrorReporter;

import static Jolox.lexical_scanner.TokenType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {

    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;

    private final RunMode mode;

    private int current = 0;
    private int loopDepth = 0;

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
            if(match(CLASS)) return classDeclaration();
            if(check(FUN) && checkNext()) {
                consume(FUN, null);
                return function(FunctionType.FUNCTION);
            }
            if(match(VAR)) return varDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(IDENTIFIER, "Expected class name");
        Expr.Variable superclass = null;

        if(match(LESS)) {
            consume(IDENTIFIER, "Expected superclass name");
            superclass = new Expr.Variable(previous());
        }

        consume(LEFT_BRACE, "Expected { before class body");

        List<Stmt.Function> methods = new ArrayList<>();
        List<Stmt.Function> classMethods = new ArrayList<>();
        while(!check(RIGHT_BRACE) && !isAtEnd()) {
            boolean isClassMethod = match(CLASS);
            (isClassMethod?  classMethods: methods).add(function(FunctionType.METHOD));
        }

        consume(RIGHT_BRACE, "Expected } after class body");
        return new Stmt.Class(name, superclass, methods, classMethods);
    }

    private Stmt statement() {
        if(match(BREAK)) return breakStatement();
        if(match(FOR)) return forStatement();
        if(match(IF)) return ifStatement();
        if(match(PRINT)) return printStatement();
        if(match(RETURN)) return returnStatement();
        if(match(WHILE)) return whileStatement();
        if(match(LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }

    private Stmt breakStatement() {
        if(loopDepth == 0) error(previous(), "Break statements can only be used within looping constructs");
        consume(SEMICOLON, "Expected ; after break statement");
        return new Stmt.Break();
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expected ( after for");
        Stmt initializer;
        if(match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if(!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expected ; after loop condition");

        Expr increment = null;
        if(!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expected ) after for clauses");

        try {
            loopDepth++;
            Stmt body = statement();

            if(increment != null) {
                body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
            }

            if(condition == null) condition = new Expr.Literal(true);
            body = new Stmt.While(condition, body);

            if(initializer != null) {
                body = new Stmt.Block(Arrays.asList(initializer, body));
            }

            return body;
        } finally {
            loopDepth--;
        }
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expected ( after if");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ) after if condition");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if(match(ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expected ; after value");
        return new Stmt.Print(value);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if(!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON,  "Expected ; after return statement");
        return new Stmt.Return(keyword, value);
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

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expected ( after while");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ) after while condition");
        try {
            Stmt body = statement();
            loopDepth++;
            return new Stmt.While(condition, body);
        } finally {
            loopDepth--;
        }
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expected ; after expression");
        return new Stmt.Expression(expr);
    }

    private Stmt.Function function(FunctionType type) {
        Token name = consume(IDENTIFIER, "Expected " + type.type + " name");
        return new Stmt.Function(name, functionBody(type));
    }

    private Expr.Function functionBody(FunctionType type) {
        List<Token> parameters = null;
        if(type != FunctionType.METHOD || check(LEFT_PAREN)) {
            consume(LEFT_PAREN, "Expected ( after " + type.type + " name");
            parameters = new ArrayList<>();
            if(!check(RIGHT_PAREN)) {
                do {
                    if(parameters.size() >= 255) {
                        error(peek(), "Can't have more than 255 parameters");
                    }

                    parameters.add(consume(IDENTIFIER, "Expected parameter name"));
                } while (match(COMMA));
            }

            consume(RIGHT_PAREN, "Expected ) after parameter list");

        }

        consume(LEFT_BRACE, "Expected { before " + type.type + " body");
        List<Stmt> body = block();
        return new Expr.Function(parameters, body);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while(!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expected } at end of block");
        return statements;
    }

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
        Expr expr = or();
        if(match(EQUAL)) {
            Token equals = previous();
            Expr value =  assignment();

            if(expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }
            else if(expr instanceof Expr.Get get) {
                return new Expr.Set(get.object, get.name, value);
            }

            ErrorReporter.getInstance().error(equals, "Invalid assignment target");
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();
        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();
        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
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

        return call();
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Can't have more than 255 arguments to a function");
                }
                arguments.add(equality());
            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Expected ) after function call");
        return new Expr.Call(callee, paren, arguments);
    }

    private Expr call() {
        Expr expr = primary();

        while(true) {
            if(match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT)){
                Token name = consume(IDENTIFIER, "Expected property name after .");
                expr = new Expr.Get(expr, name);
            }
            else {
                break;
            }
        }

        return expr;
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

        if(match(SUPER)) {
            Token keyword = previous();
            consume(DOT, "Expected . after super keyword");
            Token method = consume(IDENTIFIER, "Expected superclass method name");
            return new Expr.Super(keyword, method);
        }

        if(match(THIS)) return new Expr.This(previous());

        if(match(FUN)) {
            return functionBody(FunctionType.FUNCTION);
        }

        throw parsingError(peek(), "Expected Expression.");
    }

    private Token consume(TokenType type, String message) {
        if(ignoreSemiColon(type) || check(type)) return advance();
        throw parsingError(peek(), message);
    }

    private boolean checkNext() {
        if(isAtEnd()) return false;
        if(tokens.get(current + 1).getType() == EOF) return false;
        return tokens.get(current + 1).getType() == TokenType.IDENTIFIER;
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