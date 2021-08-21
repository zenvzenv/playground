grammar Data;
file : group+;
group : INT seq[$INT.int];

seq[int n]
locals [int i = 1;]
    : ({$i <= $n}? INT {$i++;})*//匹配n个整数
    ;
INT : [0-9]+;//匹配整数
WS : [ \n\t\r] -> skip;//丢弃空白字符