grammar Expr;
@header{package zhengwei.antlr.expr;}
//导入外部语法文件
import CommonLexerRules;
paog : stat+;
stat : expr NEWLINE     #printExpr//标签的作用是在生成java代码的时候会生成对应名字的方法
     | ID '=' expr      #assign
     | NEWLINE          #blank
     | 'clear'          #clear
     ;
//左递归
expr : expr op=(MUL|DIV) expr       #MulDiv
     | expr op=(ADD|SUB) expr       #AddSub
     | INT                          #int
     | ID                           #id
     | '(' expr ')'                 #parents
     ;
//ID : [a-zA-Z]+;
//INT : [0-9]+;
//NEWLINE : '\r' ? '\n';
//WS : [ \t]+ -> skip;

MUL : '*';
DIV : '/';
ADD : '+';
SUB : '-';
