/** Created by Omar Ahmed Hesham Aziz on 20/11/2022 */

#include <stdio.h>

#include <compiler/compiler.h>
#include <scanner/scanner.h>

void compile(const char* source) {
    initScanner(source);
    int line = -1;
    for(;;) {
        Token token = scanToken();
        if( token.line != line) {
            printf("%4d ", token.line);
            line = token.line;
        } else {
            printf("   | ");
        }
        printf("%s '%.*s'\n", TYPE_STRING[token.type], (int)token.length, token.start);

        if(token.type == TOKEN_EOF) break;
    }
}