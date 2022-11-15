package Jolox.lexical_scanner;

public class Token {
    final TokenType type;
    final String lexeme;

    final Object literal;

    final int line;

    public Token(TokenType type, String lexeme, Object literal, int line)  {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }

    public String getLexeme() {
        return lexeme;
    }

    public TokenType getType() {
        return type;
    }

    public Object getLiteral() {
        return literal;
    }

    public int getLine() {
        return line;
    }
}
