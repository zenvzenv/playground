grammar test;
INTEGER: DIGIT+
        | '0' [Xx] HEX_DIGIT+
        ;

fragment DIGIT: [0-9]+;
fragment HEX_DIGIT: [0-9A-Fa-f]+;

WS: [ \r\n] -> skip;

ID: [A-Z]+ {log{"match rule"}};
