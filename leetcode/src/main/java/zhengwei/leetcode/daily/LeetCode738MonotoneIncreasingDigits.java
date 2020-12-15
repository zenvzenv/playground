package zhengwei.leetcode.daily;

/**
 * 738. 单调递增的数字
 *
 * @author zhengwei AKA zenv
 * @since 2020/12/15 15:51
 */
public class LeetCode738MonotoneIncreasingDigits {
    //贪心算法
    //从后往前，遇到一对前一个比后一个大的数，就把大的数减一，如果有多对也没有关系，反正最后会把 flag 之后的所有数字全变为9
    public int monotoneIncreasingDigits(int N) {
        final char[] strNs = Integer.toString(N).toCharArray();
        int flag = strNs.length;
        for (int i = strNs.length - 1; i > 0; i--) {
            //找到前一个数比后一个数大的情况
            if (strNs[i - 1] > strNs[i]) {
                //标记 flag
                flag = i;
                //把大的数减一
                strNs[i - 1]--;
            }
        }
        //将最后一个匹配到的大数之后的所有数都变成9
        for (int i = flag; i < strNs.length - 1; i++) {
            strNs[i] = '9';
        }
        return Integer.parseInt(new String(strNs));
    }
}
