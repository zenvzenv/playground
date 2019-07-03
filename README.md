# playground
## **目的在于平时的学习而写的代码**
### 1.common包
* HttpAPIClient-http请求工具包
* IpUtil-IP格式的互转和IP地址的一些判断
* SysUtils-判断一个对象是否为空
### 2.jvm目录
* 详情参见[zhengwei.jvm.JVM.md]

### 3.hdfs包
* HDFSClient-hdfs的客户端
* HDFSIO-hdfs的IO操作，包含往云平台上传文件和下载文件等操作
### 4.LeetCode包
* 是一些平时的LeetCode的刷题代码
* Daily-LeetCode01TwoSum-[两数之和](https://leetcode-cn.com/problems/two-sum/)
* Daily-LeetCode02AddTwoNumbers-[两数相加](https://leetcode-cn.com/problems/add-two-numbers/)
### 5.spark包
* 详情参见[zhengwei.spark.Spark.md](https://github.com/zw030301/playground/blob/master/src/main/java/zhengwei/spark/Spark.md)

### 6.unsafe包
* 闲暇时看一些Java关于unsafe的文章时写的一些代码
### 7.algorithm包-算法
#### 自己的一些白话理解
* 1.选择排序，时间复杂度是O(n^2),空间复杂度是O(1),不稳定的一种排序算法，
主要思想是：遍历整个要排序的数组，要是前一个元素大于后一个元素，则调换元素位置，知道数组有序位置
* 2.冒泡排序，时间复杂度是O(n^2),空间复杂度是O(1)，稳定的一种排序，
主要思想：有两层循环，外循环控制着要排序的数组的长度，每完成一次比较，下次要比较的数组长度就会减小一；
内循环控制着数组中元素的比较，如果后一个数大于前一个数就交换位置，大的数就像泡泡一样慢慢的往数组的后面跑去，随着要比较的数组长度减少，数组也逐渐有序。
* 3.插入排序，时间复杂度O(n^2),空间复杂度O(1)，稳定的一种排序算法，
主要思想：插入排序就像玩扑克时往手上接牌一样，刚开始牌比较少，随着牌的增多，我们把牌插入到大小合适的地方。
插入排序同样是有两层循环，第一层循环控制着要排序数组的个数，每循环一次要比较的数组长度加一，每次加的一个元素就是要就进行比较的元素，每次新进的元素要和之前的元素进行比较，直到新进的元素大于前一个元素为止。
插入排序适合样本较小和基本有序的元素排序，理论上效率要比选择排序和冒泡排序要高。
### 8.设计模式
#### 8.1 单例模式
* 这里写了4钟单例模式的写法。
* 1.饿汉式，线程安全，也是生产环境中比较常用的一种写法，比较简单的一种方式。这种方式主要是利用JVM只会加载一个类一次的特性来保证线程安全，当类被加载时，静态变量也会被加载；
有一个缺点：就是不论我们是否用到了这个类的对象，JVM都会去加载这个类的实例。
* 2.懒加载，线程不安全和线程不安全的版本都有，主要时利用synchronized关键字去加锁代码块以达到同步效果。但是效率比较低。
* 3.静态内部类的方式，线程安全，这既达到了懒加载也时线程安全的，主要还是利用JVM只加载一次类的这个特性来保证线程安全。
* 4.枚举的方式，真正意义上的单例模式，线程安全，不会被反序列化，这种方法还要继续深入了解下...
#### 8.2 责任链模式
* 可以参阅JavaEE中的过滤器

#### 8.3 观察者模式
* 观察者模式与责任链模式有些相像，都是利用面向对象的多态的这个特性；都是把每个观察者串联起来执行，但是责任链模式中可以中断链的继续执行，而观察者模式一般不中断链的执行。
* 观察者模式有三个主要角色，1.被观察者，2.观察者，3.事件类，还有一些观察者中对于各种事件的处理逻辑的方法。
### 9.thread线程
* 详情参见[zhengwei.thread.Thread.md](https://github.com/zw030301/playground/blob/master/src/main/java/zhengwei/thread/Thread.md)

**平时学习和工作的一些总结**
---
**_zhengwei AKA Sherlock_**
---
**闲暇之余会更新哦**
