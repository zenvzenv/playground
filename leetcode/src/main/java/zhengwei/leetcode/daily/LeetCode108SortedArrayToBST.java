package zhengwei.leetcode.daily;

/**
 * 108. 将有序数组转换为二叉搜索树
 *
 * @author zhengwei AKA Awei
 * @since 2020/8/18 18:56
 */
public class LeetCode108SortedArrayToBST {
    public static TreeNode sortedArrayToBST(int[] nums) {
        return buildBST(nums, 0, nums.length - 1);
    }

    private static TreeNode buildBST(int[] nums, int left, int right) {
        if (left > right) return null;
        int middle = (left + right) >>> 1;
        TreeNode root = new TreeNode(nums[middle]);
        root.left = buildBST(nums, left, middle - 1);
        root.right = buildBST(nums, middle + 1, right);
        return root;
    }

    public static void main(String[] args) {
        System.out.println(sortedArrayToBST(new int[]{-10, -3, 0, 5, 9}));
    }
}
