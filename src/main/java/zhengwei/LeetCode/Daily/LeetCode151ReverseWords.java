package zhengwei.LeetCode.Daily;

/**
 * LeetCode第151题，反转文字
 *
 * @author zhengwei AKA Awei
 * @since 2020/4/10 16:30
 */
public class LeetCode151ReverseWords {
    public static void main(String[] args) {
        System.out.println(reverseWords("the sky is blue"));
        System.out.println(reverseWords("  hello world!  "));
        System.out.println(reverseWords("a good   example"));
        System.out.println(reverseWords(" "));
    }

    private static String reverseWords(String s) {
        if (s.length() == 0) return "";
        final String[] strings = s.split(" ");
        if (strings.length == 0) return "";
        StringBuilder res = new StringBuilder();
        for (int i = strings.length - 1; i >= 0; i--) {
            if (strings[i].equals("")) continue;
            res.append(strings[i]).append(" ");
        }
        return res.toString().substring(0, res.length() - 1);
    }

}
