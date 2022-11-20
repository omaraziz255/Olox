/** Created by Omar Ahmed Hesham Aziz on 20/11/2022 */

#ifndef COLOX_SCANNER_H
#define COLOX_SCANNER_H

#define FOREACH_TYPE(TYPE)              \
        TYPE(TOKEN_LEFT_PAREN)          \
        TYPE(TOKEN_RIGHT_PAREN)         \
        TYPE(TOKEN_LEFT_BRACE)          \
        TYPE(TOKEN_RIGHT_BRACE)         \
        TYPE(TOKEN_COMMA)               \
        TYPE(TOKEN_PLUS)                \
        TYPE(TOKEN_DOT)                 \
        TYPE(TOKEN_MINUS)               \
        TYPE(TOKEN_SEMICOLON)           \
        TYPE(TOKEN_SLASH)               \
        TYPE(TOKEN_STAR)                \
        TYPE(TOKEN_BANG)                \
        TYPE(TOKEN_BANG_EQUAL)          \
        TYPE(TOKEN_EQUAL)               \
        TYPE(TOKEN_EQUAL_EQUAL)         \
        TYPE(TOKEN_GREATER)             \
        TYPE(TOKEN_GREATER_EQUAL)       \
        TYPE(TOKEN_LESS)                \
        TYPE(TOKEN_LESS_EQUAL)          \
        TYPE(TOKEN_IDENTIFIER)          \
        TYPE(TOKEN_STRING)              \
        TYPE(TOKEN_NUMBER)              \
        TYPE(TOKEN_AND)                 \
        TYPE(TOKEN_CLASS)               \
        TYPE(TOKEN_ELSE)                \
        TYPE(TOKEN_FALSE)               \
        TYPE(TOKEN_FOR)                 \
        TYPE(TOKEN_FUN)                 \
        TYPE(TOKEN_IF)                  \
        TYPE(TOKEN_NIL)                 \
        TYPE(TOKEN_OR)                  \
        TYPE(TOKEN_PRINT)               \
        TYPE(TOKEN_RETURN)              \
        TYPE(TOKEN_SUPER)               \
        TYPE(TOKEN_THIS)                \
        TYPE(TOKEN_TRUE)                \
        TYPE(TOKEN_VAR)                 \
        TYPE(TOKEN_WHILE)               \
        TYPE(TOKEN_ERROR)               \
        TYPE(TOKEN_EOF)                 \


#define GENERATE_ENUM(ENUM) ENUM,
#define GENERATE_STRING(STRING) #STRING,
#define str(x) #x
#define xstr(x) str(x)


typedef enum {
    FOREACH_TYPE(GENERATE_ENUM)
}TokenType;

static const char* TYPE_STRING[] = {
        FOREACH_TYPE(GENERATE_STRING)
};

typedef struct {
    TokenType type;
    const char* start;
    size_t length;
    int line;
} Token;

void initScanner(const char* source);
Token scanToken();

#endif //COLOX_SCANNER_H
