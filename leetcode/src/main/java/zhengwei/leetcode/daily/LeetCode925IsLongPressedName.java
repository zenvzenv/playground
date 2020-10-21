package zhengwei.leetcode.daily;

/**
 * 925. 长按键入
 *
 * @author zhengwei AKA Awei
 * @since 2020/10/21 10:21
 */
public class LeetCode925IsLongPressedName {
    public static boolean isLongPressedName(String name, String typed) {
        if ("".equals(name) || "".equals(typed)) return false;
        int i = 0, j = 0;
        while (j < typed.length()) {
            if (i < name.length() && name.charAt(i) == typed.charAt(j)) {
                i++;
                j++;
            } else if (j > 0 && typed.charAt(j - 1) == typed.charAt(j)) {
                j++;
            } else {
                return false;
            }
        }
        return i == name.length();
    }

    public static void main(String[] args) {
        System.out.println(isLongPressedName("pyplrz", "ppyypllr"));
    }
}
