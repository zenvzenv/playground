grammar Math;

@header{package zhengwei.antlr.math;}

prog : stat+;

stat: expr NEWLINE
    | ID '=' expr NEWLINE
    | NEWLINE
    ;

expr: expr op=('*'|'/') expr
| expr op=('+'|'-') expr
| INT
| ID
| '(' expr ')'
;

MUL : '*' ;
DIV : '/' ;
ADD : '+' ;
SUB : '-' ;
ID : [a-zA-Z]+ ;
INT : [0-9]+ ;
NEWLINE : '\r' ? '\n' ;
WS : [ \t]+ -> skip ;