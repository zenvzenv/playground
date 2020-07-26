package zhengwei.leetcode.nowcoder;

import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * 合并表记录
 *
 * @author zhengwei AKA Awei
 * @since 2020/7/24 21:43
 */
public class Main6 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()) {
            final int count = sc.nextInt();
            Map<Integer, Integer> map = new TreeMap<>();
            for (int i = 0; i < count; i++) {
                final int key = sc.nextInt();
                final int value = sc.nextInt();
                if (map.containsKey(key)) {
                    final int newValue = map.get(key) + value;
                    map.put(key, newValue);
                } else {
                    map.put(key, value);
                }
            }
            map.forEach((k, v) -> System.out.println(k + " " + v));
        }
    }
}
