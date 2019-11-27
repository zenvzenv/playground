# Linux的小技巧
## 数组的声明
array_name=(val1 val2 val3 ... valn)
## 遍历数组
### 1. 标准的for循环
```bash
array=(1 2 3 4 5)
for(( i=0;i<${#array[@]};i++)) do
#${#array[@]}获取数组长度用于循环
echo ${array[i]};
done;
```
### 2. for...in
遍历不带数组下标
```bash
array=(1 2 3 4 5)
for element in ${array[@]}
#也可以写成for element in ${array[*]}
do
echo $element
done
```
遍历（带数组下标）：
```bash
arr=(1 2 3 4 5)
for i in "${!arr[@]}";   
do   
    printf "%s\t%s\n" "$i" "${arr[$i]}"  
done 
```
## 截取字符串
* \# 和 \## 号截取字符串(删左边留右边)
### \#号
1. 语法:${var#*str}，var为变量名，str为需要匹配的字符串
2. 从左边开始，删除第一个str以及左边的所有字符串
3. 例子
```bash
var=http://www.mmm.cn/123.html
echo ${var#*//}  #www.mmm.cn/123.html
```
### \##号
1. 语法:${var##*str}，var为变量，str为需要匹配的字符串
2. 从左边开始，删除最后一个str以及左边的所有字符串
3. 例子
```bash
var=http://www.mmm.cn/123.html
echo ${var##*/}  #123.html
```
* % 和 %% 号截取字符串(删右边留左边)
### %号
1. 语法${var%str*}，var为变量名，str需要匹配的字符串
2. 从右边开始，删除第一个str及其右边的所有字符串 
3. 例子
```bash
var=http://www.mmm.cn/123.html
echo ${var%/*}  #http://www.mmm.cn
```
### %%号
1. 语法${var%%str*}，var为变量名，str为需要匹配的字符串
2. 有右边开始，删除最后一个str汲取右边的所有字符串 
3. 例子
```bash
var=http://www.mmm.cn/123.html
echo ${var%%/*}  #http:
```
* 截取指定个数的字符串
1. ${var:n1:n2}  解释：截取n1和n2之间的字符串  
2. ${var:7}表示：从左边第8个字符开始，一直到结束 
3. ${var:0-7:5}表示：从右边第7个字符开始，截取5个字符
4. ${var:0-5}表示：从右边第5个字符开始，一直到变量结束