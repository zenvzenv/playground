//专门的词法描述文件
lexer grammar CommonLexerRules;
ID : [a-zA-Z]+;
INT : [0-9]+;
NEWLINE : '\r' ? '\n';
WS : [ \t] -> skip;