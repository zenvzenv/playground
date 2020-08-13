package zhengwei.leetcode.daily;

import java.util.*;

/**
 * 133. 克隆图
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/12 13:28
 */
public class LeetCode133CloneGraph {
    private static final class Node {
        public int val;
        public List<Node> neighbors;

        public Node() {
            val = 0;
            neighbors = new ArrayList<>();
        }

        public Node(int _val) {
            val = _val;
            neighbors = new ArrayList<>();
        }

        public Node(int _val, ArrayList<Node> _neighbors) {
            val = _val;
            neighbors = _neighbors;
        }
    }

    private static final Map<Node, Node> map = new HashMap<>();

    public static Node cloneGraphDFS(Node node) {
        if (null == node) return node;
        //查看当前节点是否已经遍历过了
        //如果遍历过了直接返回之前遍历过的节点
        if (map.containsKey(node)) return map.get(node);
        //否则新建一个节点对象
        Node cloneNode = new Node(node.val, new ArrayList<>());
        //将当前节点放到遍历表中
        map.put(node, cloneNode);
        //遍历当前节点的邻接节点，并重复之前的操作
        for (int i = 0; i < node.neighbors.size(); i++) {
            cloneNode.neighbors.add(cloneGraphDFS(node.neighbors.get(i)));
        }
        return cloneNode;
    }

    public static Node cloneGraphBFS(Node node) {
        if (null == node) return node;
        Queue<Node> queue = new LinkedList<>();
        map.put(node, new Node(node.val, new ArrayList<>()));
        queue.add(node);
        while (!queue.isEmpty()) {
            final Node n = queue.poll();
            //遍历当前节点的邻居节点
            for (Node neighbor : n.neighbors) {
                //查看邻居节点是否已经遍历过了
                //没有遍历过的话则加到遍历表中，并添加到队列中
                if (!map.containsKey(neighbor)) {
                    map.put(neighbor, new Node(neighbor.val, new ArrayList<>()));
                    queue.add(new Node(neighbor.val, new ArrayList<>()));
                }
                //添加邻居节点到当前节点的克隆节点的邻居节点中
                map.get(n).neighbors.add(map.get(neighbor));
            }
        }
        return map.get(node);
    }

    public static void main(String[] args) {
        Node n1 = new Node(1);
        Node n2 = new Node(2);
        Node n3 = new Node(3);
        Node n4 = new Node(4);
        n1.neighbors = Arrays.asList(n2, n3);
        n2.neighbors = Arrays.asList(n1, n4);
        n3.neighbors = Arrays.asList(n1, n4);
        n4.neighbors = Arrays.asList(n2, n3);
        System.out.println(cloneGraphBFS(n1));
    }
}
