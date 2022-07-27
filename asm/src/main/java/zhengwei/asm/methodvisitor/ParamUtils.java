package zhengwei.asm.methodvisitor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/21 20:10
 */
public final class ParamUtils {
    private static final DateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void printValueOnStack(boolean value) {
        System.out.println("    " + value);
    }

    public static void printValueOnStack(byte value) {
        System.out.println("    " + value);
    }

    public static void printValueOnStack(char value) {
        System.out.println("    " + value);
    }

    public static void printValueOnStack(short value) {
        System.out.println("    " + value);
    }

    public static void printValueOnStack(int value) {
        System.out.println("    " + value);
    }

    public static void printValueOnStack(float value) {
        System.out.println("    " + value);
    }

    public static void printValueOnStack(long value) {
        System.out.println("    " + value);
    }

    public static void printValueOnStack(double value) {
        System.out.println("    " + value);
    }

    public static void printValueOnStack(Object value) {
        if (value == null) {
            System.out.println("    " + value);
        } else if (value instanceof String) {
            System.out.println("    " + value);
        } else if (value instanceof Date) {
            System.out.println("    " + fm.format(value));
        } else if (value instanceof char[]) {
            System.out.println("    " + Arrays.toString((char[]) value));
        } else {
            System.out.println("    " + value.getClass() + ": " + value.toString());
        }
    }

    public static void printText(String str) {
        System.out.println(str);
    }
}
