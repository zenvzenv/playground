package zhengwei.leetcode.daily;

/**
 * 1791. 找出星型图的中心节点
 *
 * @author zhengwei AKA zenv
 * @since 2022/2/18
 */
public class LeetCode1791FindCenter {
    public int findCenter(int[][] edges) {
        return edges[0][0] == edges[1][0] || edges[0][0] == edges[1][1] ? edges[0][0] : edges[0][1];
    }
}
