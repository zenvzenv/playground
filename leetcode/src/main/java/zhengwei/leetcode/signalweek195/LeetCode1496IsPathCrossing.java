package zhengwei.leetcode.signalweek195;

import java.util.HashSet;
import java.util.Set;

/**
 * @author zhengwei AKA Awei
 * @since 2020/7/11 11:45
 */
public class LeetCode1496IsPathCrossing {
    public static boolean isPathCrossing(String path) {
        Set<String> set = new HashSet<>();
        set.add("0,0");
        int x = 0;
        int y = 0;
        for (int i = 0; i < path.length(); i++) {
            switch (path.charAt(i)) {
                case 'N':
                    y++;
                    break;
                case 'S':
                    y--;
                    break;
                case 'W':
                    x--;
                    break;
                case 'E':
                    x++;
                    break;
            }
            final String point = x + "," + y;
            if (set.contains(point)) {
                return true;
            }
            set.add(point);
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(isPathCrossing("NES"));
    }
}
