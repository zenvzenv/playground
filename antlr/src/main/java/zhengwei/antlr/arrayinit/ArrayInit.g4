grammar ArrayInit;
@header{package zhengwei.antlr.arrayinit;}
init : '{' value (',' value)* '}';
value : init
      | INT
      ;

INT : [0-9]+;
WS : [ \t\n\r] -> skip;