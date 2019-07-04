# Shell脚本专栏
## 1 三剑客之awk
### 1.1 awk基础
1. awk的语法如下
```bash
awk [options] 'program' file1 , file2 , ...
#对于上述的program来说可以细分为pattern和action，及awk语法如下
awk [option] 'pattern {action}' {file}
```
2. 先从最简单的入手
```bash
$ echo ddd > test.txt
$ awk '{print $0}' test
ddd
```
上面的awk只是执行了一个打印操作，原样输出test中的内容<br/>
3. 开始深入
awk是逐行处理的，在处理一个文本时，会一行一行处理，awk默认会以换行符作为标记，来识别一行数据<br/>
awk中使用 `$0` 来表示一整行记录，`$NF` 表示这一行的最后一列， `$1~$n` 来表示一行中对应的被分割出的对应序号的字段<br/>
awk中默认使用连续的"空白符"和"\t"作为分隔符，用户可以自己指定换行符，awk会按照用户指定的换行符去分割一行数据<br/>
当然，用户可以一次输出多列的数据
```bash
$ last -n 5 | awk '{print $1" "$2}'
linkage pts/0
linkage pts/7
linkage pts/6
linkage pts/2
linkage pts/5
```
除了文本中原本就有的列，我们也可以自己加上指定的新列加入到原来的列中，
```bash
$ last -n 5 | awk '{print $1" "$2" ""zhengwei"}'
linkage pts/0 zhengwei
linkage pts/7 zhengwei
linkage pts/6 zhengwei
linkage pts/2 zhengwei
linkage pts/5 zhengwei
  zhengwei
```
4. 特殊的模式BEGIN和END
BEGIN模式指定了处理文本之前需要执行的操作
```bash
$ awk 'BEGIN{print "zhengwei"} {print $0}' awk_test.txt 
zhengwei
a:b:c:d
a:b:c:d
b:c:d:e
b:c:d:e
c:d:e:f
c:d:e:f
```
虽然指定了数据源awk_test.txt，但是在开始处理文本之前，需要先去执行 `BEGIN` 的语句块<br/>
END模式指定了处理完文本所有行之后需要执行的操作<br/>
```bash
$ awk '{print $0} END{print "zhengwei","end"}' awk_test.txt 
a:b:c:d
a:b:c:d
b:c:d:e
b:c:d:e
c:d:e:f
c:d:e:f
zhengwei end
```