# Java8专栏
## 一、Lambda表达式
1. 基本形式<br/>
    `(param1, param2, param3) -> {...}`
2. 关于函数式接口<br/>
    1. 如果一个接口只有一个抽象方法，那么该接口就是函数式接口
    2. 如果我们在某个接口上声明了@FunctionalInterface，那么编译器将会用函数式接口的要求来要求被标注的类
        1. 如果被标注的类不是interface，而是class,enum或者annotation，那么编译器将会报错
        2. 如果被@FunctionalInterface标注的interface不符合函数式接口的要求，那么编译器也会报错
    3. 如果某个接口只有一个抽象方法，但我们并没有该接口声明@FunctionalInterface注解，那么编译器依旧会将该接口看作是函数式接口