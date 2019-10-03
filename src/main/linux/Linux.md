# Linux(更偏向于开发与使用技巧)
_笔记内容主要参考鸟哥的Linux私房菜_
# 基础篇
## 一、图形界面与文字界面
### 1. 从图形界面切换到文字界面(terminal)
通常Linux预设了6个Terminal来让使用者登入，切换方式是： `ctrl + alt + f1~f6` 的组合按钮。  
那这六个终端介面如何命名呢，系统会将[F1] ~ [F6]命名为tty1 ~ tty6的操作介面环境。也就是说，当你按下[ctrl] + [Alt] + [F1]这三个组合按钮时(按着[ctrl]与[Alt]不放，再按下[F1]功能键)，就会进入到tty1的terminal介面中了。同样的[F2]就是tty2啰！那么如何回到刚刚的X视窗介面呢？很简单啊！按下[Ctrl] + [Alt] + [F1]就可以了！我们整理一下登入的环境如下：  
* [Ctrl] + [Alt] + [F2] ~ [F6] ：文字介面登入tty2 ~ tty6 终端机；
* [Ctrl] + [Alt] + [F1] ：图形介面桌面。  
由于系统预设的登入界面不同，因此你想要进入X 的终端机名称也可能会有些许差异。以CentOS 7 为例，由于我们这次安装的练习机，预设是启动图形界面的，因此这个X 视窗将会出现在tty1 界面中。如果你的Linux 预设使用纯文字界面，那么tty1~tty6 就会被文字界面占用。  
> 在CentOS 7 环境下，当开机完成之后，预设系统只会提供给你一个tty 而已，因此无论是文字界面还是图形界面，都是会出现在tty1喔！tty2~tty6其实一开始是不存在的！但是当你要切换时(按下[ctrl]+[alt]+[F2])，系统才产生出额外的tty2, tty3...  
若你在纯文字环境中启动X视窗，那么图形界面就会出现在当时的那个tty 上面。举例来说，你在tty3 登入系统，然后输入startx 启动个人的图形界面，那么这个图形界面就会产生在tty3上面！这样说可以理解吗？
```bash
#纯文字界面下(不能有X存在)启动视窗界面的作法 
[zhengwei@study ~]$ startx	
```
不过startx这个指令并非万灵丹，你要让startx生效至少需要底下这几件事情的配合：
```text
* 并没有其他的X window 被启用；
* 你必须要已经安装了X Window system，并且X server是能够顺利启动的；
* 你最好要有视窗管理员，例如GNOME/KDE或者是TWM等；
```
其实，所谓的视窗环境，就是：**文字界面加上X 视窗软件**的组合！因此，文字界面是一定会存在的，只是视窗界面软体就看你要不要启动而已。所以，我们才有办法在纯文字环境下启动一个个人化的X视窗啊！因为这个startx 是任何人都可以执行的喔！并不一定需要管理员身份的。所以，是否预设要使用图形界面，只要在后续管理服务的程序中，将**graphical.target**这个目标服务设定为预设，就能够预设使用图形界面啰！

### 2. 在文字界面中登陆Linux
刚刚你如果有按下[Ctrl] + [Alt] + [F2]就可以来到tty2的登入画面，而如果你并没有启用图形视窗界面的话， 那么预设就是会来到tty1这个环境中。这个纯文字环境的登入的画面有点像这样：
```bash
CentOS Linux 7 (Core)
Kernel 3.10.0-229.el7.x86_64 on an x​​86_64
study login: zhengwei 
Password: #这里输入你的密码 
Last login: Fri May 29 11:55:05 on tty1 #上次登入的情况 
[zhengwei@study ~]$ _ #游标闪烁，等待你的指令输入
```
上面显示的内容是这样的：  
1. CentOS Linux 7 (Core)：显示Linux distribution的名称(CentOS)与版本(7)；
2. Kernel 3.10.0-229.el7.x86_64 on an x86_64：显示Linux核心的版本为3.10.0-229.el7.x86_64，且目前这部主机的硬体等级为x86_64。
3. study login:：那个study是你的主机名称。你也可以使用root这个帐号来登入的。不过**root**这个帐号代表在Linux系统下无穷的权力，所以尽量不要使用root帐号来登入啦！
4. Password:：账号的密码，请注意，在输入密码的时候，屏幕上面**不会显示任何的字样！**，所以不要以为你的键盘坏掉去！很多初学者一开始到这里都会拼命的问！啊我的键盘怎么不能用...
5. Last login: Fri May 29 11:55:05 on tty1：当使用者登入系统后，系统会列出上一次这个帐号登入系统的时间与终端机名称！建议大家还是得要看看这个资讯，是否真的是自己的登入所致喔！
6. [zhengwei@study ~]$ _ ：这一行则是正确登入之后才显示的讯息，最左边的zhengwei显示的是**目前使用者的帐号**，而@之后接的study则是**主机名称**，至于最右边的~则指的是**目前所在的目录**，那个$则是我们常常讲的**提示字符**啦！

### 3. 文字界面下命令的下达
其实我们都是透过**程式**在跟系统作沟通的，本章上面提到的视窗管理员或文字模式都是一组或一只程式在负责我们所想要完成的任务。文字模式登入后所取得的程式被称为壳(Shell)，这是因为这支程式负责最外面跟使用者(我们)沟通，所以才被戏称为壳程式！我们Linux的壳程式就是厉害的bash这一支！

#### 3.1 命令的下达
其实整个指令下达的方式很简单，你只要记得几个重要的概念就可以了。举例来说，你可以这样下达指令的：
```bash
[zhengwei@study ~]$ command [-options] parameter1 parameter2 ... 
                     指令选项参数(1)参数(2)
```
上述指令详细说明如下：  
0. 行指令中第一个输入的部分绝对是**指令(command)**或**可执行文件(例如批次脚本,script)**
1. command为指令的名称，例如变换工作目录的指令为 `cd` 等等；
2. 中刮号[]并不存在于实际的指令中，而加入选项设定时，通常选项前会带 `-` 号，例如 `-h` ；有时候会使用选项的完整全名，则选项前带有 `--` 符号，例如 `--help` ；
3. parameter1 parameter2.. 为依附在选项后面的参数，或者是command的参数；
4. **在指令,选项,参数等中间以空格来区分，不论空几格shell都视为一格。所以空格是很重要的特殊字元！；**
5. 按下[Enter]按键后，该指令就立即执行。[Enter]按键代表着一行指令的开始启动。
6. 指令太长的时候，可以使用反斜线 `\`  来跳脱[Enter]符号，使指令连续到下一行。注意！反斜线后就立刻接特殊字符，才能跳脱！
7. 其他：
	* Linux系统是大小写敏感的，英文大小写字母是不一样的。举例来说， cd与CD并不同。
	* 。。。
注意到上面的说明当中，**第一个被输入的资料绝对是指令或者是可执行的档案**！这个是很重要的概念喔！还有，按下[Enter]键表示要开始执行此一命令的意思。我们来实际操作一下：以ls这个**指令**列出**自己家目录(~)**下的**所有隐藏档与相关的档案属性**，要达成上述的要求需要加入-al这样的选项，所以：
```bash
[zhengwei@study ~]$ ls -al ~ 
[zhengwei@study ~]$ ls -al ~ 
[zhengwei@study ~]$ ls -a -l ~
```
上面这三个指令的下达方式是一模一样的执行结果喔！为什么？请参考上面的说明吧！关于更详细的文字模式使用方式.此外，请特别留意，在Linux的环境中， 『大小写字母是不一样的东西！』也就是说，在Linux底下， VBird与vbird这两个档案是『完全不一样的』档案呢！所以，你在下达指令的时候千万要注意到指令是大写还是小写。例如当输入底下这个指令的时候，看看有什么现象：
```bash
[zhengwei@localhost zhengwei]$ date
Sat Sep 21 15:04:20 CST 2019
[zhengwei@localhost zhengwei]$ Date
-bash: Date: command not found
[zhengwei@localhost zhengwei]$ DATE
-bash: DATE: command not found
```
很好玩吧！只是改变小写成为大写而已，该指令就变的不存在了！ 因此，请千万记得这个状态呦！

#### 3.2 语系的支持
另外，很多时候你会发现，咦！怎么我输入指令之后显示的结果的是乱码？ 不要紧张～我们前面提到过，Linux是可以支援多国语系的，若可能的话，萤幕的讯息是会以该支援语系来输出的。但是，我们的终端机介面(terminal)在预设的情况下，无法支援以中文编码输出资料的。这个时候，我们就得将支援语系改为英文，才能够以英文显示出正确的讯息。那怎么做呢？你可以这样做：
```bash
1.显示目前所支援的语系 
[linkage@localhost zhengwei]$ locale
LANG=en_US.UTF-8						#语言语系的输出
LC_CTYPE="en_US.UTF-8"					#底下为许多资讯的输出使用的特别语系
LC_NUMERIC="en_US.UTF-8"
LC_TIME="en_US.UTF-8"					#时间方面的语系资料
LC_COLLATE="en_US.UTF-8"
LC_MONETARY="en_US.UTF-8"
LC_MESSAGES="en_US.UTF-8"
LC_PAPER="en_US.UTF-8"
LC_NAME="en_US.UTF-8"
LC_ADDRESS="en_US.UTF-8"
LC_TELEPHONE="en_US.UTF-8"
LC_MEASUREMENT="en_US.UTF-8"
LC_IDENTIFICATION="en_US.UTF-8"
LC_ALL=en_US.UTF-8						#全部的资料同步更新的设定值
[linkage@localhost zhengwei]$ date
Sat Sep 21 15:04:20 CST 2019

2.修改语系成为英文语系 
[dmtsai@study ~]$ LANG=en_US.utf8 
[dmtsai@study ~]$ export LC_ALL=en_US.utf8
# LANG只与输出讯息有关，若需要更改其他不同的资讯，要同步更新LC_ALL才行！
```
注意一下，那个『LANG=en_US.utf8』是连续输入的，等号两边并没有空白字符喔！这样一来，就能够在『这次的登入』查看英文讯息啰！为什么说是『这次的登入』呢？因为，如果你登出Linux后，刚刚下达的指令就没有用啦！

#### 3.3 基础指令的操作
底下我们立刻来操作几个简单的指令看看啰！同时请注意，我们已经使用了英文语系作为预设输出的语言喔
* 显示日期与时间的指令： date
* 显示日历的指令： cal
* 简单好用的计算机： bc

##### 3.3.1 显示日期的命令：date
如果在文字介面中想要知道目前Linux系统的时间，那么就直接在指令列模式输入date即可显示：
```bash
[zhengwei@localhost zhengwei]$ date
Sat Sep 21 15:04:20 CST 2019
```
格式化输出date信息，如下：
```bash
[dmtsai@study ~]$ date +%Y/%m/%d
2019/09/21
[dmtsai@study ~]$ date +%H:%M
15:33
```
那个『+%Y%m%d』就是date指令的一些参数功能啦！很好玩吧！这些参数可以通过Linux自带的 `man` 命令查看。  
**从上面的例子当中我们也可以知道，指令之后的选项除了前面带有减号『-』之外，某些特殊情况下，选项或参数前面也会带有正号『+』的情况！**这部份可不要轻易的忘记了呢！
##### 3.3.2 显示日历的命令：cal
那如果我想要列出目前这个月份的月历呢？直接给他下达cal即可！
```bash
[linkage@localhost zhengwei]$ cal
   September 2019   
Su Mo Tu We Th Fr Sa
 1  2  3  4  5  6  7
 8  9 10 11 12 13 14
15 16 17 18 19 20 21
22 23 24 25 26 27 28
29 30
```
基本上cal这个指令可以接的语法为：
```bash
cal [month] [year]
```
所以，如果想要知道2019年09月的日历可以直接下达
```bash
cal 09 2019

cal 13 2019
cal: illegal month value: use 1-12
```
如果输入了错误的年或月的话，那么cal将会有错误提示。通过这个提示我们可以借以了解指令下达错误之处。
##### 3.3.3 简单好用的计算器：bc
如果在文字模式当中，突然想要作一些简单的加减乘除，偏偏手边又没有计算机！这个时候要笔算吗？不需要啦！我们的Linux有提供一支计算程式，那就是bc喔。你在指令列输入bc后，萤幕会显示出版本资讯， 之后就进入到等待指示的阶段。
```bash
[zhengwei@localhost zhengwei]$ bc
bc 1.06.95
Copyright 1991-1994, 1997, 1998, 2000, 2004, 2006 Free Software Foundation, Inc.
This is free software with ABSOLUTELY NO WARRANTY.
For details type `warranty'.
```
事实上，我们是『进入到bc这个软件的工作环境当中』了！就好像我们在Windows里面使用『小算盘』一样！所以，我们底下尝试输入的资料，都是在bc程式当中在进行运算的动作。所以啰，你输入的资料当然就得要符合bc的要求才行！在基本的bc计算机操作之前，先告知几个使用的运算子好了：
```text
* + 加法
* - 减法
* * 乘法
* / 除法
* ^ 指数
* % 余数
```
由于bc这个软件是预设仅输出整数的，如果想要进行小数计算的话，那么就必须要执行scale=number ，那个number就是小数点位数，例如：
```bash
[dmtsai@study ~]$ bc
bc 1.06.95
Copyright 1991-1994, 1997, 1998, 2000, 2004, 2006 Free Software Foundation, Inc.
This is free software with ABSOLUTELY NO WARRANTY.
For details type `warranty'.
scale=3      #没错！就是这里！！
1/3
.333
340/2349
.144
quit
```
注意！要离开bc回到命令提示字符时，务必要输入『quit』来离开bc的软体环境喔！好了！就是这样子啦！简单的很吧！以后你可以轻轻松松的进行加减乘除啦！
##### 3.3.4 命令运行的几种模式
从上面的练习我们大概可以知道在指令列模式里面下达指令时，会有两种主要的情况：
* 一种是该指令会直接显示结果然后回到命令提示字元等待下一个指令的输入；
* 一种是进入到该指令的环境，直到结束该指令才回到命令提示字元的环境。

#### 3.3 重要的几个热键[Tab], [ctrl]-c, [ctrl]-d
在后续学习之前，这里很需要跟大家再来报告一件事，那就是我们的文字模式里头具有很多的功能组合键， 这些按键可以辅助我们进行指令的编写与程式的中断呢！这几个按键请大家务必要记住的！很重要喔！
##### 3.3.1 [Tab]按键
[Tab]按键就是在键盘的大写灯切换按键([Caps Lock])上面的那个按键！在各种Unix-Like的Shell当中，这个[Tab]按键算是Linux的Bash shell最棒的功能之一了！他具有『命令补全』与『档案补齐』的功能喔！重点是，可以避免我们打错指令或档案名称呢！很棒吧！但是[Tab]按键在不同的地方输入，会有不一样的结果喔！我们举下面的例子来说明。上一小节我们不是提到cal 这个指令吗？如果我在指令列输入 `ca` 再按两次[tab]按键，会出现什么讯息？
```bash
[linkage@localhost zhengwei]$ ca[tab][tab]     #[tab]按键是紧接在a字母后面！
cacertdir_rehash     cache_dump           cache_repair         cache_writeback      cal                  calibrate_ppa        canberra-boot        cancel               capsh                card_eventmgr        case                 catchsegv
cache_check          cache_metadata_size  cache_restore        cairo-sphinx         ca-legacy            caller               canberra-gtk-play    cancel.cups          captoinfo            carte.sh             cat                  catman

```
发现什么事？所有以ca为开头的指令都被显示出来啦！很不错吧！那如果你输入『ls -al ~/.bash』再加两个[tab]会出现什么？
```bash
[linkage@localhost zhengwei]$ ls -al ~/.bash_[tab][tab]
.bash_history  .bash_logout   .bash_profile
```
我们按[tab]按键的地方如果是在command(第一个输入的资料)后面时，他就代表着『命令补全』，如果是接在第二个字以后的，就会变成『档案补齐』的功能了！
总结一下：
* [Tab] 接在一串指令的第一个字的后面，则为『命令补全』；
* [Tab] 接在一串指令的第二个字以后时，则为『档案补齐』！
* 若安装bash-completion软件，则在某些指令后面使用[tab] 按键时，可以进行『选项/参数的补齐』功能！
善用[tab]按键真的是个很好的习惯！可以让你避免掉很多输入错误的机会！

##### 3.3.2 [Ctrl]-c 按键
如果你在Linux底下输入了错误的指令或参数，有的时候这个指令或程式会在系统底下『跑不停』这个时候怎么办？别担心，如果你想让当前的程式『停掉』的话，可以输入：[Ctrl]与c按键( 先按着[Ctrl]不放，且再按下c按键，是组合按键 )，那就是中断目前程式的按键啦！举例来说，如果你输入了『find /』这个指令时，系统会开始跑一些东西(先不要理会这个指令串的意义)，此时你给他按下[Ctrl]-c组合按键，嘿嘿！是否立刻发现这个指令串被终止了！就是这样的意思啦！
```bash
[dmtsai@study ~]$ find / 
....(一堆东西都省略)....
#此时萤幕会很花，你看不到命令提示字元的！直接按下[ctrl]-c即可！
[dmtsai@study ~]$ #此时提示字元就会回来了！find程式就被中断！
```
不过你应该要注意的是，这个组合键是可以将正在运作中的指令中断的， 如果你正在运作比较重要的指令，可别急着使用这个组合按键喔！
##### 3.3.3 [Ctrl]-d 按键
那么[Ctrl]-d是什么呢？就是[Ctrl]与d按键的组合啊！这个组合按键通常代表着： 『键盘输入结束(End Of File, EOF或End Of Input)』的意思！另外，他也可以用来取代exit的输入呢！例如你想要直接离开文字介面，可以直接按下[Ctrl]-d就能够直接离开了(相当于输入exit啊！)。
##### 3.3.4 [shift]+{[PageUP]|[Page Down]}按键
如果你在纯文字的画面中执行某些指令，这个指令的输出讯息相当长啊！所以导致前面的部份已经不在目前的萤幕画面中， 所以你想要回头去瞧一瞧输出的讯息，那怎办？其实，你可以使用[Shift]+[Page Up] 来往前翻页，也能够使用[Shift]+[Page Down] 来往后翻页！这两个组合键也是可以稍微记忆一下，在你要稍微往前翻画面时，相当有帮助！
#### 3.4 错误信息的查看
万一我下达了错误的指令怎么办？你可以借由屏幕上面显示的错误讯息来了解你的问题点，那就很容易知道如何改善这个错误讯息啰！举个例子来说，假如想执行date却因为大小写打错成为DATE时，这个错误的讯息是这样显示的：
```bash
[dmtsai@study ~]$ DATE 
bash: DATE: command not found...   #这里显示错误的讯息 
Similar command is: 'date'         #这里竟然给你一个可能的解决方案耶！
```
上面那个bash:表示的是我们的Shell的名称，本小节一开始就谈到过Linux的预设壳程式就是bash啰！那么上面的例子说明了bash有错误，什么错误呢？bash告诉你： `DATE: command not found`  
字面上的意思是说『指令找不到』，那个指令呢？就是DATE这个指令啦！所以说，系统上面可能并没有DATE这个指令啰！就是这么简单！通常出现『command not found』的可能原因为：
```text
* 这个指令不存在，因为该软体没有安装之故。解决方法就是安装该软体；
* 这个指令所在的目录目前的用户并没有将他加入指令搜寻路径中，请参考第十章bash的PATH说明；
* 很简单！因为你打错字！
```
从CentOS 7 开始，bash 竟然会尝试帮我们找解答！看一下上面输出的第二行『Similar command is: 'date'』，他说，相似的指令是date，我们就是输入错误的大小写而已～这就已经帮我们找到答案了！看了输出，你也应该知道如何解决问题了吧？
### 4. Linux系统的线上求助man page和info page
#### 4.1 命令--help的使用
事实上，几乎Linux 上面的指令，在开发的时候，开发者就将可以使用的指令语法与参数写入指令操作过程中了！你只要使用『--help』这个选项， 就能够将该指令的用法作一个大致的理解喔！举例来说，我们来瞧瞧date 这个指令的基本用法与选项参数的介绍：
```bash
[zhengwei@study ~]# date --help 
Usage: date [OPTION]... [+FORMAT]                            #这里有基本语法 
  or: date [-u|--utc|--universal] [MMDDhhmm[[CC] YY][.ss]]   #这是设定时间的语法 
Display the current time in the given FORMAT, or set the system date. #底下是主要的选项说明 
Mandatory arguments to long options are mandatory for short options too. 
  -d , --date=STRING display time described by STRING, not 'now' 
  -f, --file=DATEFILE like --date once for each line of DATEFILE ....(中间省略).... 
  -u, - -utc, --universal print or set Coordinated Universal Time (UTC) 
      --help显示此求助说明并离开
      --version显示版本资讯并离开#底下则是重要的格式(FORMAT)的主要项目
FORMAT controls the output. Interpreted sequences are: 
  %% a literal % 
  %a locale's abbreviated weekday name (eg, Sun) 
  %A locale's full weekday name (eg, Sunday) ....(中间省略).... #底下是几个重要的范例(Example) 
Examples: 
Convert seconds since the epoch (1970-01-01 UTC) to a date 
  $ date --date='@2147483647' ....(底下省略)....
```
看一下上面的显示，首先一开始是下达语法的方式(Usage)，这个date 有两种基本语法，一种是直接下达并且取得日期回传值，且可以+FORAMAT 的方式来显示。至于另一种方式，则是加上MMDDhhmmCCYY 的方式来设定日期时间。他的格式是『月月日日时时分分西元年』的格式！再往下看， 会说明主要的选项，例如-d 的意义等等，后续又会出现+FORMAT 的用法！从里面你可以查到我们之前曾经用过得『 date +%Y%m%d 』这个指令与选项的说明。  
基本上，如果是指令，那么透过这个简单的--help 就可以很快速的取得你所需要的选项、参数的说明了！这很重要！我们说过，在linux 底下你需要学习『任务达成』的方式， 不用硬背指令参数。不过常用的指令你还是得要记忆一下，而选项就透过--help 来快速查询即可。  
同样的，透过cal --help 你也可以取得相同的解释！相当好用！不过，如果你使用bc --help 的话，虽然也有简单的解释，但是就没有类似scale 的用法说明， 同时也不会有+ , - , * , / , % 等运算子的说明了！因此，虽然--help 已经相当好用，不过，通常--help 用在协助你查询『你曾经用过的指令所具备的选项与参数』而已， 如果你要使用的是从来没有用过得指令，或者是你要查询的根本就不是指令，而是档案的『格式』时，那就得要通过man page！  
#### 4.2 man page
date --help没有告诉你STRING是什么？除了--help之外，我们Linux上面的其他线上求助系统已经都帮你想好要怎么办了，所以你只要使用简单的方法去寻找一下说明的内容，马上就清清楚楚的知道该指令的用法了！	man是manual(操作说明)的简写啦！只要下达：『man date』马上就会有清楚的说明出现在你面前！如下所示：
```bash
[dmtsai@study ~]$ man date 
DATE (1)                           User Commands DATE(1)
 #请注意上面这个括号内的数字 
NAME   <==这个指令的完整全名，如下所示为date且说明简单用途为设定与显示日期/时间 
       date - print or set the system date and time 
SYNOPSIS   <==这个指令的基本语法如下所示 
       date [OPTION]... [+FORMAT]                             <==第一种单纯显示的用法 
       date [ -u|--utc|--universal] [MMDDhhmm[[CC]YY][.ss]]    <==这种可以设定系统时间的用法
DESCRIPTION   <==详细说明刚刚语法谈到的选项与参数的用法
       Display the current time in the given FORMAT, or set the system date. 
       Mandatory arguments to long options are mandatory for short options too. 
       -d, --date=STRING   <==左边-d为短选项名称，右边--date为完整选项名称 
              display time described by STRING, not 'now' 
       -f, --file=DATEFILE 
              like --date once for each line of DATEFILE 
       -I[TIMESPEC], --iso-8601[=TIMESPEC] 
              output date/ time in ISO 8601 format. TIMESPEC='date' for date only (the 
              default), 'hours', 'minutes', 'seconds', or 'ns' for date and time to the 
              indicated precision. ....(中间省略).... #找到了！底下就是格式化输出的详细资料！
       FORMAT controls the output. Interpreted sequences are: 
       %% a literal % 
       %a locale's abbreviated weekday name (eg, Sun) 
       %A locale's full weekday name (eg, Sunday) ....(中间省略).... 
ENVIRONMENT   < ==与这个指令相关的环境参数有如下的说明 
       TZ Specifies the timezone, unless overridden by command line parameters.   
              If neither is specified, the setting from /etc/localtime is used. 
EXAMPLES      <==一堆可用的范本 
       Convert seconds since the epoch (1970-01-01 UTC) to a date 
              $ date --date='@2147483647' ....(中间省略).... 
DATE STRING   <==上面曾提到的--date的格式说明！
       The --date=STRING is a mostly free format human readable date string such as "Sun, 29 
       Feb 2004 16:21:42 -0800" or "2004-02-29 16:21:42" or even "next Thursday" . A date 
       string may contain items indicating calendar date, time of day, time zone, day of 
AUTHOR   <==这个指令的作者啦！
       Written by David MacKenzie. 
COPYRIGHT   <==受到著作权法的保护！用的就是GPL了！
       Copyright © 2013 Free Software Foundation, Inc. License GPLv3+: GNU GPL version 3 or 
       later <http://gnu.org/licenses/gpl.html>. 
       This is free software: you are free to change and redistribute it. There is NO WAR‐ 
       RANTY, to the extent permitted by law. 
SEE ALSO  <==这个重要，你还可以从哪里查到与date相关的说明文件之意 
       The full documentation for date is maintained as a Texinfo manual. If the info and 
       date programs are properly installed at your site, the command 
              info coreutils 'date invocation' 
       should give you access to the complete manual. 
GNU coreutils 8.22 June 2014 DATE(1)
```
> 进入man指令的功能后，你可以按下『空白键』往下翻页，可以按下『 q 』按键来离开man的环境。更多在man指令下的功能，本小节后面会谈到的！
首先，在上个表格的第一行，你可以看到的是：『DATE(1)』，DATE我们知道是指令的名称， 那么(1)代表什么呢？他代表的是『一般使用者可使用的指令』的意思！咦！还有这个用意啊！呵呵！没错～在查询资料的后面的数字是有意义的喔！他可以帮助我们了解或者是直接查询相关的资料。常见的几个数字的意义是这样的：
```text
代号		代表内容
1		使用者在shell环境中可以操作的指令或可执行档								*
2		系统核心可呼叫的函数与工具等
3		一些常用的函数(function)与函式库(library)，大部分为C的函式库(libc)
4		装置档案的说明，通常在/dev下的档案
5		设定档或者是某些档案的格式												*
6		游戏(games)
7		惯例与协定等，例如Linux档案系统、网路协定、ASCII code等等的说明
8		系统管理员可用的管理指令												*
9		跟kernel有关的文件
* : 比较重要
```
上述的表格内容可以使用『man man』来更详细的取得说明。透过这张表格的说明，未来你如果使用man page在察看某些资料时，就会知道该指令/档案所代表的基本意义是什么了。举例来说，如果你下达了『man null』时，会出现的第一行是：『NULL(4)』，对照一下上面的数字意义，原来null这个玩意儿竟然是一个『装置档案』呢！很容易了解了吧！  
man page的内容也分成好几个部分来加以介绍该指令呢！就是上头man date那个表格内， 以NAME作为开始介绍，最后还有个SEE ALSO来作为结束。基本上，man page大致分成底下这几个部分：
```text
代号			内容说明
NAME		简短的指令、资料名称说明
SYNOPSIS	简短的指令下达语法(syntax)简介
DESCRIPTION	较为完整的说明，这部分最好仔细看看！
OPTIONS		针对SYNOPSIS 部分中，有列举的所有可用的选项说明
COMMANDS	当这个程式(软体)在执行的时候，可以在此程式(软体)中下达的指令
FILES		这个程式或资料所使用或参考或连结到的某些档案
SEE ALSO	可以参考的，跟这个指令或资料有相关的其他说明！
EXAMPLE		一些可以参考的范例
```
## 二、Linux的文件权限与目录配置
### 1. 使用者和群组
#### 1.1 文件拥有者；群组；其他人
Linux是多人多任务的操作系统，为了考虑多个每个人的隐私权以及每个人的个人喜好，因此这个文件拥有者就显得非常重要了。如果你不想其他人看到你的文件里面的内容的话，可以把文件的权限设置成仅自己可读，其他人不可读。每个单独的用户可以属于不通的群组，一个群组中的用户对于同一个文件/文件夹具有相同的权限；其他人的权限也是可以设置的。root在Linux是无敌的存在，所有的权限限制对root用户不起作用。
### 2. 文件权限的概念
大致了解了Linux的使用者与群组之后，接着下来，我们要来谈一谈，这个档案的权限要如何针对这些所谓的『使用者』与『群组』来设定呢？这个部分是相当重要的，尤其对于初学者来说，因为档案的权限与属性是学习Linux的一个相当重要的关卡， 如果没有这部份的概念，那么你将老是听不懂别人在讲什么呢！尤其是当你在你的萤幕前面出现了『Permission deny』的时候，不要担心，『肯定是权限设定错误』
#### 2.1 Linux档案属性
当我们下达下达`ls -al`命令的时候，屏幕上会出现如下内容
```bash
total 420
drwxrwxr-x.  7 linkage linkage   4096 Sep 23 09:47 .
drwx------. 23 linkage linkage   4096 Sep 24 10:50 ..
drwxr-xr-x.  9 linkage linkage   4096 Aug  8 13:22 kafka
drwxrwxr-x. 11 linkage linkage   4096 Aug  9 16:54 netcat
-rw-r--r--.  1 linkage linkage 398872 Aug  6 09:50 netcat-0.7.1.tar.gz
drwxr-xr-x. 24 linkage linkage   8192 Aug  6 18:39 nmap
drwxrwxr-x.  2 linkage linkage     32 Sep 23 09:47 practice
drwxrwxr-x.  3 linkage linkage    102 Dec 26  2018 springboot-gdtu

-rw-r--r--.  1    linkage linkage   398872      Aug  6 09:50    netcat-0.7.1.tar.gz
[1]         [2]     [3]     [4]       [5]            [6]              [7]
[权限]     [连结] [拥有者]  [群组]   [档案容量]      [修改日期]          [档名]
```
##### 2.1.1 第一个栏位表示文件的类型与权限
这个地方最需要注意了！仔细看的话，你应该可以发现这一栏其实共有十个字符:
`-   rwx     rwx     rwx`
1. 第一个字符表示这个文件是『目录、文件或引用等等』：
    * 当为[ d ]则是目录，例如上表档名为『kafka』的那一行
    * 当为[ - ]则是档案，例如上表档名为『netcat-0.7.1.tar.gz』那一行；
    * 若是[ l ]则表示为引用文件(link file)；
    * 若是[ b ]则表示为装置档里面的可供储存的周边设备(可随机存取装置)；
    * 若是[ c ]则表示为装置档里面的序列埠设备，例如键盘、鼠标(一次性读取装置)。
2. 接下来是每三个一组，且均为`rwx`的三个参数的组合。其中，[ r ]代表可读(read)、[ w ]代表可写(write)、[ x ]代表可执行(execute)。要注意的是，这三个权限的位置不会改变，如果没有权限，就会出现减号[ - ]而已。
    * 第一组为『档案拥有者可具备的权限』；
    * 第二组为『加入此群组之帐号的权限』；
    * 
##### 2.1.2 第二个栏位表示多少文件连结到此节点(i-node)
每个文件都会将他的权限与属性记录到档案系统的i-node中，不过，我们使用的目录树却是使用档名来记录，因此每个档名就会连结到一个i-node啰！这个属性记录的，就是有多少不同的档名连结到相同的一个i-node号码去就是了
##### 2.1.3 第三个栏位表示这个文件或文件夹的拥有者账号
##### 2.1.4 第四个栏位表示这个文件或文件夹所属群组
在Linux系统下，你的帐号会加入于一个或多个的群组中。
##### 2.1.5 第五个栏位表示这个文件的大小，单位默认为bytes
##### 2.1.6 第六个栏位表示这个文件的最近修改时间或建立时间
这一栏的内容分别为日期(月/日)及时间。如果这个档案被修改的时间距离现在太久了，那么时间部分会仅显示年份而已。如下所示：
```bash
[root@study ~]# ll /etc/services /root/initial-setup-ks.cfg 
-rw-r--r--. 1 root root 670293 Jun 7 2013 /etc/services
-rw-r--r--. 1 root root 1864 May 4 18:01 /root/initial-setup-ks.cfg
# 如上所示，/etc/services为2013年所修改过的档案，离现在太远之故，所以只显示年份；
# 至于/root/initial-setup-ks.cfg 是今年(2015) 所建立的，所以就显示完整的时间了。
```
##### 2.1.7 第七个栏位表示文件的文件名
这个栏位就是文件名了，比较特殊的是：如果在文件名的开头有`.`的话，则代表这个文件是隐藏文件，例如`.config`，该文件就是隐藏文件。你可以使用『ls』及『ls -a』这两个指令去感受一下什么是隐藏文件。
#### 2.2 Linux的文件权限的重要性
与Windows系统不一样的是，在Linux系统当中，每一个文件都多加了很多的属性进来，尤其是群组的概念，这样有什么用途呢？其实，最大的用途是在『资料安全性』上面的。
1. 系统保护的功能：  
举个简单的例子，在你的系统中，关于系统服务的档案通常只有root才能读写或者是执行，例如/etc/shadow这一个帐号管理的档案，由于该档案记录了你系统中所有帐号的资料，因此是很重要的一个设定档，当然不能让任何人读取(否则密码会被窃取)，只有root才能够来读取啰！所以该档案的权限就会成为[ ---------- ]！所有人都不能使用？没关系，root基本上是不受系统的权限所限制的，所以无论档案权限为何，预设root都可以存取喔！
2. 团队开发软体或资料共用的功能：  
此外，如果你有一个软件开发团队，在你的团队中，你希望每个人都可以使用某一些目录下的档案，而非你的团队的其他人则不予以开放呢？以上面的例子来说，testgroup的团队共有三个人，分别是test1, test2, test3，那么我就可以将团队所需的档案权限订为[ -rwxrws--- ]来提供给testgroup的工作团队使用啰！(怎么会有s呢？没关系，这个我们在后续章节将会有介绍！)
3. 未将权限设定妥当的危害：  
再举个例子来说，如果你的目录权限没有作好的话，可能造成其他人都可以在你的系统上面乱搞啰！例如本来只有root才能做的开关机、ADSL的拨接程式、新增或删除使用者等等的指令，若被你改成任何人都可以执行的话，那么如果使用者不小心给你重新开机啦！重新拨接啦！等等的！那么你的系统不就会常常莫名其妙的挂掉啰！而且万一你的使用者的密码被其他不明人士取得的话，只要他登入你的系统就可以轻而易举的执行一些root的工作！
#### 2.3 如何修改文件的属性与权限
我们现在知道档案权限对于一个系统的安全重要性了，也知道档案的权限对于使用者与群组的相关性， 那么如何修改一个档案的属性与权限呢？又！有多少档案的权限我们可以修改呢？其实一个档案的属性与权限有很多！我们先介绍几个常用于群组、拥有者、各种身份的权限之修改的指令，如下所示：
* chgrp：改变文件所属的群组
* chown：改变文件的拥有者
* chmod：改变文件的权限，SUID, SGID, SBIT等等的特性
##### 2.3.1 chgrp：修改文件所属的群组
改变一个档案的群组真是很简单的，直接以chgrp来改变即可。这个指令其实是change group的缩写，要被改变的群组名称必须要在/etc/group档案内存在才行，否则就会显示错误！  
chgrp的语法如下：
```bash
chgrp [-R] group_name filename/dirname...

```
##### 2.3.2 chown：修改文件拥有者
##### 2.3.3 chmod：修改文件的权限
# 附录：常用命令
### 
# 附录：常用小技巧
