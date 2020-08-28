package zhengwei.leetcode.daily;

/**
 * 657. 机器人能否返回原点
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/28 9:24
 */
public class LeetCode657JudgeCircle {
    public boolean judgeCircle(String moves) {
        int x = 0, y = 0;
        for (int i = 0; i < moves.length(); i++) {
            if (moves.charAt(i) == 'U') y++;
            else if (moves.charAt(i) == 'D') y--;
            else if (moves.charAt(i) == 'R') x++;
            else if (moves.charAt(i) == 'L') x--;
        }
        return x == 0 && y == 0;
    }
}
