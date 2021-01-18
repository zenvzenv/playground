package zhengwei.leetcode.common;

/**
 * 并查集模板
 */
public final class UnionFind {
    //记录节点的根
    private final int[] parent;
    //记录节点的深度(用于优化)
    private final int[] rank;

    public UnionFind(int[] parent, int[] rank) {
        this.parent = parent;
        this.rank = rank;
        for (int i = 0; i < parent.length; i++) {
            parent[i] = i;
        }
    }

    public int find(int i) {
        if (i != parent[i]) {
            parent[i] = find(parent[i]);
        }
        return parent[i];
    }

    public void union(int i1, int i2) {
        int rootA = find(i1);
        int rootB = find(i2);
        if (rootA != rootB) {
            if (rank[rootA] < rank[rootB]) {
                rootA ^= rootB;
                rootB ^= rootA;
                rootA ^= rootB;
            }
            parent[rootB] = rootA;
            if (rank[rootA] == rank[rootB]) rank[rootA] += 1;
        }
    }

}