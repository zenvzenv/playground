grammar Calc;
//paser
start: input ;
input: setvariable NL input
       | expression NL? EOF
       ;
setvariable : ID '=' expression
       ;
expression : expression POW expression          #pow
        | expression (MUL | DIV) expression     #muldiv
        | expression (ADD | SUB) expression     #addsub
        | '(' expression ')'                    #expr
        | ID                                    #id
        | NUMBER                                #num
        ;

//lexer
POW : '^' ;
ADD : '+' ;
SUB: '-' ;
MUL: '*' ;
DIV : '/' ;
NL : '\r' ? '\n' ;
ID : [a-zA-Z_]+ ;
NUMBER : [0-9]+ ;