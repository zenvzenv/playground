package zhengwei.leetcode.nowcoder;

import java.util.Scanner;

/**
 * 字符串最后一个单词的长度
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/24 21:35
 */
public class Main5 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()) {
            final String line = sc.nextLine();
            final String[] words = line.split(" ");
            System.out.println(words[words.length - 1].length());
        }
    }
}
