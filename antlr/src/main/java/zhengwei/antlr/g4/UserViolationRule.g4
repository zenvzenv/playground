grammar UserViolationRule;
@header{package zhengwei.antlr.userviolation;}
//lexer
//词法定义存在前后优先级关系
WINDOWTIME : 'windowTime';
ALLDAY : 'allDay';
INT : [0-9]+;
FLOAT : INT+ '.' INT*
      | '.' INT+
      ;
UNIT : ('DAYS'|'HOURS'|'MINUTES'|'SECONDS');
DO : ('login'|'burn');
SOMETHING : ('system'|'CD');
RANGETIME : 'range';
SINGLE : 'single';
WHO : [a-zA-Z0-9]+;

//grammer
rule : who when what frequency;

who : WHO;
when : RANGETIME INT INT     # rangeTime
       | WINDOWTIME INT UNIT # windowTime
       | SINGLE              # single
       | ALLDAY              # allDay
       ;
what : DO SOMETHING;
frequency : INT;

WS : [ \t\n\r] -> skip;