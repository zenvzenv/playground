package zhengwei.leetcode.daily;

/**
 * 684. 冗余连接
 *
 * @author zhengwei AKA zenv
 * @since 2021/1/13 9:28
 */
public class LeetCode684FindRedundantConnection {
    public int[] findRedundantConnection(int[][] edges) {
        final int length = edges.length;
        final int[] parent = new int[length + 1];
        for (int i = 0; i <= length; i++) {
            parent[i] = i;
        }
        for (int[] edge : edges) {
            final int node1 = edge[0];
            final int node2 = edge[1];
            if (find(parent, node1) != find(parent, node2)) {
                union(parent, node1, node2);
            } else {
                return edge;
            }
        }
        return new int[0];
    }

    //查找祖先节点
    private int find(int[] parent, int i) {
        if (parent[i] != i) {
            parent[i] = find(parent, parent[i]);
        }
        return parent[i];
    }

    //合并两个连通分量
    private void union(int[] parent, int i1, int i2) {
        final int a = find(parent, i1);
        final int b = find(parent, i2);
        parent[a] = b;
    }
}
