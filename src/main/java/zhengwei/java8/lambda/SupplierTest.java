package zhengwei.java8.lambda;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

/**
 * @author zhengwei AKA Awei
 * @since 2019/10/17 20:02
 */
public class SupplierTest {
    private static Supplier<String> supplier = () -> "hello world";

    @Test
    void testSupplier() {
        System.out.println(supplier.get());
    }

    @Test
    void testCreateStudent() {
        Supplier<Student> studentSupplier1 = () -> new Student();
        System.out.println(studentSupplier1.get().getName());
        //构造方法引用
        Supplier<Student> studentSupplier2 = Student::new;
        System.out.println(studentSupplier2.get().getName());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Student {
        private String name = "zhengwei";
        private int age = 25;
    }
}
