grammar CSV;
@header {package zhengwei.antlr.csv;}

file : hdr row+;
hdr : row;
row : field (',' field)* '\r'? '\n';

field : TEXT   #text
      | STRING #string
      |        #empty
      ;

TEXT : ~[,\n\r]+;
STRING : '"' ('""'|~'"')* '"';